package com.apkspectrum.core.permissionmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.xpath.XPathException;

import com.apkspectrum.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.plugin.NetworkException;
import com.apkspectrum.plugin.NetworkNotFoundException;
import com.apkspectrum.plugin.NetworkSetting;
import com.apkspectrum.util.Base64;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.XmlPath;

public class PermissionRecorder {

	private XmlPath histories;

	public PermissionRecorder() { }

	public void record(PermissionRepository repo) {
		histories = new XmlPath();
		try {
			histories.createXPath("/permission-history/sources");
			histories.createXPath("/permission-history/permissions");
			histories.createXPath("/permission-history/permission-groups");
			histories.createXPath("/permission-history/resources/resource");
		} catch (XPathException e1) {
			e1.printStackTrace();
		}

		if(repo == null) {
			Log.e("No have information of sources.");
			return;
		}

		if(repo.url == null) {
			Log.e("Repository url is null");
			return;
		}
		boolean isAOSP = repo.url.startsWith("https://android.googlesource.com/");
		String manifestPath = null;
		if(repo.manifestPath == null) {
			if(!isAOSP) {
				Log.e("manifestPath is null\n");
				return;
			} else {
				manifestPath = "/core/res/AndroidManifest.xml?format=TEXT";
			}
		} else if(isAOSP && repo.manifestPath.endsWith(".xml")) {
			manifestPath = repo.manifestPath + "?format=TEXT";
		} else {
			manifestPath = repo.manifestPath;
		}
		String resourcePath = null;
		if(repo.resourcePath == null) {
			if(!isAOSP) {
				Log.e("resourcePath is null\n");
				return;
			} else {
				resourcePath = "/core/res/res/values${config}/strings.xml?format=TEXT";
			}
		} else if(isAOSP && repo.resourcePath.endsWith(".xml")) {
			resourcePath = repo.resourcePath + "?format=TEXT";
		} else {
			resourcePath = repo.resourcePath;
		}

		recordRepositoryInfo(repo);

		String baseUrl = isAOSP ? repo.url + "/+/" : repo.url;
		Base64.Decoder decoder = isAOSP ? Base64.getDecorder() : null;

		HashMap<String, PermissionInfoExt> map = new HashMap<>();
		HashMap<String, PermissionGroupInfoExt> groupMap = new HashMap<>();
		ArrayList<String> preSdkPermissionList = new ArrayList<>();
		ArrayList<String> preSdkPermissionGroupList = new ArrayList<>();

		boolean isLatestSdk = true;
		int preSdkVersion = 0;
		for(int i=repo.sources.size()-1; i>=0; --i) {
			int sdk = repo.sources.get(i).sdk;
			String commit = repo.sources.get(i).commit.trim();

			if(commit.isEmpty()) {
				Log.e("No have information of commit for SDK" + sdk);
				continue;
			}

			String url = baseUrl + commit + manifestPath;
			Log.v("SDK" + sdk + ", " + url);

			XmlPath sourcePath = null;
			XmlPath[] resPath = new XmlPath[repo.config.length];

			String rawXml = null;
			String[] rawResXml = new String[repo.config.length];
			try {
				rawXml = getSource(url);
				if(rawXml != null) {
					if(isAOSP) rawXml = new String(decoder.decode(rawXml));
					sourcePath = new XmlPath(rawXml);
				}

				for(int j=0; j<repo.config.length; j++) {
					String resUrl = baseUrl + commit +
							resourcePath.replace("${config}", !repo.config[j].equals("default") ? "-" + repo.config[j] : "");
					Log.v(resUrl);
					rawResXml[j] = getSource(resUrl);
					if(rawResXml[j] != null) {
						if(isAOSP) rawResXml[j] = new String(decoder.decode(rawResXml[j]));
						resPath[j] = new XmlPath(rawResXml[j]);
					} else {
						Log.w("rawResXml["+j+"] is null, " + resUrl );
					}
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}

			if(rawXml == null) {
				Log.e("Access fail");
				continue;
			}

			ArrayList<String> permissionList = new ArrayList<>();
			ArrayList<String> permissionGroupList = new ArrayList<>();

			XmlPath permList = sourcePath.getNodeList("/manifest/permission");
			for(int j=permList.getCount()-1; j>=0; --j) {
				PermissionInfoExt info = makePermissionInfo(permList.getNode(j));
				info.sdk = sdk;
				info.labels = getResource(info.label, repo.config, resPath);
				info.descriptions = getResource(info.description, repo.config, resPath);

				PermissionInfoExt preInfo = map.get(info.name);
				if(preInfo == null) {
					PermissionInfoExt recordInfo = info;
					if(!isLatestSdk) {
						// deleted permission
						//Log.d("deleted permission " + info.name + ", sdk " + info.sdk);
						recordInfo = new PermissionInfoExt(info);
						recordInfo.comment = " @Removed" + (info.comment != null ? info.comment : "");
					}
					recordPermission(recordInfo);
				} else if(info.sdk == preInfo.sdk) {
					Log.w("Duplicated declared : " + info.name);
					continue;
				} else if(!info.equals(preInfo)) {
					// compare pre
					recordPermissionPatch(info, preInfo);
					//Log.d("diff to pre info");
				}
				map.put(info.name, info);
				permissionList.add(info.name);
				if(preSdkPermissionList.contains(info.name)) {
					preSdkPermissionList.remove(info.name);
				}
			}

			for(String perm: preSdkPermissionList) {
				//Log.d("Add perm " + perm + " from "+ preSdkVersion);
				map.get(perm).name = "";
				try {
					XmlPath preNode = histories.getNode("/permission-history/permissions/permission[@name='" + perm + "']/patch[@sdk='" + preSdkVersion + "']");
					if(preNode != null) {
						preNode.setAttribute("action", "added");
					} else {
						histories.createXPath("/permission-history/permissions/permission[@name='" + perm + "']/patch[@action='added' and @sdk='" + preSdkVersion + "']");
					}
				} catch (XPathException e) {
					e.printStackTrace();
				}
			}
			preSdkPermissionList.clear();
			preSdkPermissionList = permissionList;

			XmlPath groupList = sourcePath.getNodeList("/manifest/permission-group");
			Log.d("sdk " + sdk + ", permList " + permList.getCount() + ", groupList " + groupList.getCount());
			for(int j=groupList.getCount()-1; j>=0; --j) {
				PermissionGroupInfoExt info = makePermissionGroupInfo(groupList.getNode(j));;
				info.sdk = sdk;
				info.labels = getResource(info.label, repo.config, resPath);
				info.descriptions = getResource(info.description, repo.config, resPath);
				info.requests = getResource(info.request, repo.config, resPath);

				PermissionGroupInfoExt preInfo = groupMap.get(info.name);
				if(preInfo == null) {
					PermissionGroupInfoExt recordInfo = info;
					if(!isLatestSdk) {
						// deleted permission
						//Log.d("deleted permission " + info.name + ", sdk " + info.sdk);
						recordInfo = new PermissionGroupInfoExt(info);
						recordInfo.comment = " @Removed" + (info.comment != null ? info.comment : "");
					}
					recordPermissionGroup(recordInfo);
				} else if(info.sdk == preInfo.sdk) {
					Log.w("Duplicated declared : " + info.name);
					continue;
				} else if(!info.equals(preInfo)) {
					// compare pre
					recordPermissionGroupPatch(info, preInfo);
					//Log.d("diff to pre info");
				}
				groupMap.put(info.name, info);
				permissionGroupList.add(info.name);
				if(preSdkPermissionGroupList.contains(info.name)) {
					preSdkPermissionGroupList.remove(info.name);
				}
			}

			for(String group: preSdkPermissionGroupList) {
				//Log.d("Add perm " + perm + " from "+ preSdkVersion);
				groupMap.get(group).name = "";
				try {
					XmlPath preNode = histories.getNode("/permission-history/permission-groups/permission-group[@name='" + group + "']/patch[@sdk='" + preSdkVersion + "']");
					if(preNode != null) {
						preNode.setAttribute("action", "added");
					} else {
						histories.createXPath("/permission-history/permission-groups/permission-group[@name='" + group + "']/patch[@action='added' and @sdk='" + preSdkVersion + "']");
					}
				} catch (XPathException e) {
					e.printStackTrace();
				}
			}
			preSdkPermissionGroupList.clear();
			preSdkPermissionGroupList = permissionGroupList;

			isLatestSdk = false;
			preSdkVersion = sdk;

		}
	}

	private void recordRepositoryInfo(PermissionRepository repo) {
		try {
			XmlPath node = histories.createXPath("/permission-history/sources");

			node.setAttribute("repository", repo.url);
			if(repo.manifestPath != null && !repo.manifestPath.isEmpty()) {
				node.setAttribute("manifest", repo.manifestPath);
			}
			if(repo.resourcePath != null && !repo.resourcePath.isEmpty()) {
				node.setAttribute("resources", repo.resourcePath);
			}
			String config = repo.config[0];
			for(int i=1; i<repo.config.length; i++) {
				config += ";" + repo.config[i];
			}
			node.setAttribute("config", config);

			for(SourceCommit src: repo.sources) {
				node.createXPath("source[@sdk='" + src.sdk + "' and @commit='" + src.commit + "']");
			}

		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	private void recordPermission(PermissionInfoExt info) {
		try {
			XmlPath node = histories.createXPath("/permission-history/permissions/permission[@name='" + info.name + "']");
			if(info.label != null) {
				node.setAttribute("label", info.label);
				recordResource(info.label, info.labels, -1);
			}
			if(info.description != null) {
				node.setAttribute("description", info.description);
				recordResource(info.description, info.descriptions, -1);
			}
			if(info.icon != null) node.setAttribute("icon", info.icon);
			if(info.permissionGroup != null) node.setAttribute("permissionGroup", info.permissionGroup);
			if(info.protectionLevel != null) node.setAttribute("protectionLevel", info.protectionLevel);
			if(info.permissionFlags != null) node.setAttribute("permissionFlags", info.permissionFlags);
			if(info.comment != null) {
				node.setComment(info.comment);
				if(info.comment.contains("@Removed")) {
					node.createXPath("patch[@action='removed' and @sdk='"+ (info.sdk+1) +"']");
				}
			}
		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	private void recordPermissionGroup(PermissionGroupInfoExt info) {
		try {
			XmlPath node = histories.createXPath("/permission-history/permission-groups/permission-group[@name='" + info.name + "']");
			if(info.label != null) {
				node.setAttribute("label", info.label);
				recordResource(info.label, info.labels, -1);
			}
			if(info.description != null) {
				node.setAttribute("description", info.description);
				recordResource(info.description, info.descriptions, -1);
			}
			if(info.request != null) {
				node.setAttribute("request", info.request);
				recordResource(info.request, info.requests, -1);
			}
			if(info.priority != null) node.setAttribute("priority", info.priority.toString());
			if(info.icon != null) node.setAttribute("icon", info.icon);
			if(info.comment != null) {
				node.setComment(info.comment);
				if(info.comment.contains("@Removed")) {
					node.createXPath("patch[@action='removed' and @sdk='"+ (info.sdk+1) +"']");
				}
			}
		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	private void recordPermissionPatch(PermissionInfoExt lowerInfo, PermissionInfoExt higherInfo) {
		XmlPath node = histories.getNode("/permission-history/permissions/permission[@name='" + lowerInfo.name + "']");
		if(node == null) {
			Log.w("No such permission node");
			if(higherInfo == null) {
				Log.e("permission information of higher sdk is null");
				return;
			}
			recordPermission(higherInfo);
			node = histories.getNode("/permission-history/permissions/permission[@name='" + lowerInfo.name + "']");
			if(node == null) {
				Log.e("fail make permission node");
				return;
			}
		}
		try {
			if(higherInfo.name.isEmpty()) {
				higherInfo.name = lowerInfo.name;
				node.createXPath("patch[@action='removed' and @sdk='" + (lowerInfo.sdk + 1) + "']");
			}
			if(!lowerInfo.equals(higherInfo)) {
 				node = node.createXPath("patch[@sdk='" + lowerInfo.sdk + "']");
				if(!objEquals(lowerInfo.comment, higherInfo.comment)) {
					node.setComment(lowerInfo.comment);
				}
				if(!objEquals(lowerInfo.permissionGroup, higherInfo.permissionGroup)) {
					node.setAttribute("permissionGroup", lowerInfo.permissionGroup);
				}
				if(!objEquals(lowerInfo.protectionLevel, higherInfo.protectionLevel)) {
					node.setAttribute("protectionLevel", lowerInfo.protectionLevel);
				}
				if(!objEquals(lowerInfo.permissionFlags, higherInfo.permissionFlags)) {
					node.setAttribute("permissionFlags", lowerInfo.permissionFlags);
				}
				if(!objEquals(lowerInfo.label, higherInfo.label) ||
						!Arrays.deepEquals(lowerInfo.labels, higherInfo.labels)) {
					node.setAttribute("label", lowerInfo.label);
					recordResource(lowerInfo.label, lowerInfo.labels, lowerInfo.sdk);
				}
				if(!objEquals(lowerInfo.description, higherInfo.description) ||
						!Arrays.deepEquals(lowerInfo.descriptions, higherInfo.descriptions)) {
					node.setAttribute("description", lowerInfo.description);
					recordResource(lowerInfo.description, lowerInfo.descriptions, lowerInfo.sdk);
				}
				if(!objEquals(lowerInfo.icon, higherInfo.icon) ||
						!Arrays.deepEquals(lowerInfo.icons, higherInfo.icons)) {
					node.setAttribute("icon", lowerInfo.icon);
				}
			}
		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	private void recordPermissionGroupPatch(PermissionGroupInfoExt lowerInfo, PermissionGroupInfoExt higherInfo) {
		XmlPath node = histories.getNode("/permission-history/permission-groups/permission-group[@name='" + lowerInfo.name + "']");
		if(node == null) {
			Log.w("No such permission-group node");
			if(higherInfo == null) {
				Log.e("permission group information of higher sdk is null");
				return;
			}
			recordPermissionGroup(higherInfo);
			node = histories.getNode("/permission-history/permission-groups/permission-group[@name='" + lowerInfo.name + "']");
			if(node == null) {
				Log.e("fail make permission-group node");
				return;
			}
		}
		try {
			if(higherInfo.name.isEmpty()) {
				higherInfo.name = lowerInfo.name;
				node.createXPath("patch[@action='removed' and @sdk='" + (lowerInfo.sdk + 1) + "']");
			}
			if(!lowerInfo.equals(higherInfo)) {
 				node = node.createXPath("patch[@sdk='" + lowerInfo.sdk + "']");
				if(!objEquals(lowerInfo.comment, higherInfo.comment)) {
					node.setComment(lowerInfo.comment);
				}
				if(!objEquals(lowerInfo.label, higherInfo.label) ||
						!Arrays.deepEquals(lowerInfo.labels, higherInfo.labels)) {
					node.setAttribute("label", lowerInfo.label);
					recordResource(lowerInfo.label, lowerInfo.labels, lowerInfo.sdk);
				}
				if(!objEquals(lowerInfo.description, higherInfo.description) ||
						!Arrays.deepEquals(lowerInfo.descriptions, higherInfo.descriptions)) {
					node.setAttribute("description", lowerInfo.description);
					recordResource(lowerInfo.description, lowerInfo.descriptions, lowerInfo.sdk);
				}
				if(!objEquals(lowerInfo.request, higherInfo.request) ||
						!Arrays.deepEquals(lowerInfo.requests, higherInfo.requests)) {
					node.setAttribute("request", lowerInfo.request);
					recordResource(lowerInfo.request, lowerInfo.requests, lowerInfo.sdk);
				}
				if(!objEquals(lowerInfo.priority, higherInfo.priority)) {
					node.setAttribute("priority", lowerInfo.priority != null ? lowerInfo.priority.toString() : null);
				}
				if(!objEquals(lowerInfo.icon, higherInfo.icon) ||
						!Arrays.deepEquals(lowerInfo.icons, higherInfo.icons)) {
					node.setAttribute("icon", lowerInfo.icon);
				}
			}
		} catch (XPathException e) {
			e.printStackTrace();
		}
	}

	private void recordResource(String id, ResourceInfo[] resInfo, int sdk) {
		if(resInfo == null) {
			Log.e("resource info is null");
			return;
		}
		if(id == null || !id.startsWith("@string/")) {
			Log.e("id is no resource id type or null");
			return;
		}
		id = id.substring(8);
		if(!histories.isNodeExisted("/permission-history/resources/resource")) {
			try {
				histories.createXPath("/permission-history/resources/resource");
			} catch (XPathException e) {
				e.printStackTrace();
			}
		}
		for(ResourceInfo info: resInfo) {
			if(info == null) continue;
			String config = info.configuration.isEmpty() ? "[not(@config)]" : "[@config='"+info.configuration+"']";
			try {
				String sdkVer = sdk > 0 ? " and @sdk='"+sdk+"'" : "";
				XmlPath node = histories.createXPath("/permission-history/resources/resource" + config + "/string[@name='" + id + "'" + sdkVer + "]");
				node.setTextContent(info.name);
			} catch (XPathException e) {
				e.printStackTrace();
			}
		}
	}

	private PermissionInfoExt makePermissionInfo(XmlPath node) {
		PermissionInfoExt info = new PermissionInfoExt();
		info.name = node.getAttribute("android:name");
		info.label = node.getAttribute("android:label");
		info.description = node.getAttribute("android:description");
		info.icon = node.getAttribute("android:icon");
		info.permissionGroup = node.getAttribute("android:permissionGroup");
		info.protectionLevel = node.getAttribute("android:protectionLevel");
		info.permissionFlags = node.getAttribute("android:permissionFlags");
		info.comment = node.getComment();
		return info;
	}

	private PermissionGroupInfoExt makePermissionGroupInfo(XmlPath node) {
		PermissionGroupInfoExt info = new PermissionGroupInfoExt();
		info.name = node.getAttribute("android:name");
		info.label = node.getAttribute("android:label");
		info.description = node.getAttribute("android:description");
		info.icon = node.getAttribute("android:icon");
		info.request = node.getAttribute("android:request");
		info.comment = node.getComment();
		String priority = node.getAttribute("android:priority");
		if(priority != null && !priority.isEmpty())
			info.priority = Integer.parseInt(priority);
		return info;
	}

	private ResourceInfo[] getResource(String id, String[] config, XmlPath[] resPaths) {
		if(id == null || !id.startsWith("@string/")) {
			return new ResourceInfo[] { new ResourceInfo(id) };
		}
		id = id.substring(8);
		ResourceInfo[] res = new ResourceInfo[config.length];
		for(int i=0; i<config.length && i<resPaths.length; i++) {
			XmlPath node = resPaths[i].getNode("/resources/string[@name='" + id + "']");
			if(node != null) {
				//Log.v(id + " = " + node.getTextContent());
				res[i] = new ResourceInfo(node.getTextContent(), !"default".equals(config[i]) ? config[i] : "" );
			} else {
				Log.w("No such id " + id + ", " + config[i]);
			}
		}
		return res;
	}

	protected boolean objEquals(Object a, Object b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}

	public String getSource(String url) throws NetworkException {
		if(!NetworkSetting.isEnabledNetworkInterface()) {
			Log.w("No such network interface");
			throw makeNetworkException(new NetworkNotFoundException("No such network interface"));
		}

		System.setProperty("http.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		NetworkSetting networkSetting = new NetworkSetting(null);
		HttpURLConnection request = null;
		boolean isSetTruststore = false;
		try {
			URL targetURL = new URL(url);
			networkSetting.setProxyServer(targetURL.toURI());
			isSetTruststore = networkSetting.setSSLTrustStore();
			//NetworkSetting.isIgnoreSSLCert();
			request = (HttpURLConnection) targetURL.openConnection();
			request.setRequestMethod("GET");
		} catch (MalformedURLException | URISyntaxException e) {
			throw makeNetworkException(e);
		} catch (IOException e) {
			throw makeNetworkException(e);
		} finally {
			if(isSetTruststore) networkSetting.restoreSSLTrustStore();
		}

		request.setUseCaches(false);
		request.setDefaultUseCaches(false);
		request.setDoOutput(false);
		request.setDoInput(true);
		request.setInstanceFollowRedirects(false);
		request.setConnectTimeout(15000);
		request.setReadTimeout(15000);

		// customizing information
		request.setRequestProperty("User-Agent","");
		request.setRequestProperty("Referer","");
		request.setRequestProperty("Cookie","");
		request.setRequestProperty("Origin","");
		request.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
		request.setRequestProperty("Pragma", "no-cache");
		request.setRequestProperty("Expires", "0");

		String rawData = null;
		try ( InputStream is = request.getInputStream();
			  InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			  BufferedReader br = new BufferedReader(isr) ) {
			String buffer = null;
			StringBuffer sb = new StringBuffer();
			while ((buffer = br.readLine()) != null) {
				sb.append(buffer);
			}
			rawData = sb.toString();
		} catch (IOException e) {
			throw makeNetworkException(e);
		} finally {
			request.disconnect();
		}
		return rawData;
	}

	static NetworkException makeNetworkException(Exception e) {
		return new NetworkException(e);
	}

	public void saveXmlFile(File file) {
		//histories.printXml(System.out);;
		histories.saveXmlFile(file);
	}

	public static void main(final String[] args) {
		PermissionRepository repository = PermissionManager.getPermissionRepository();
		PermissionRecorder recorder = new PermissionRecorder();
		recorder.record(repository);
		recorder.saveXmlFile(new File("data/PermissionsHistory.xml"));
	}

}
