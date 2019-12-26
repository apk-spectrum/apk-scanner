package com.apkscanner.core.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.apkscanner.data.apkinfo.ActionInfo;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.CategoryInfo;
import com.apkscanner.data.apkinfo.CompatibleScreensInfo;
import com.apkscanner.data.apkinfo.DataInfo;
import com.apkscanner.data.apkinfo.IntentFilterInfo;
import com.apkscanner.data.apkinfo.ManifestInfo;
import com.apkscanner.data.apkinfo.MetaDataInfo;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.PermissionTreeInfo;
import com.apkscanner.data.apkinfo.ProviderInfo;
import com.apkscanner.data.apkinfo.ReceiverInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.data.apkinfo.ServiceInfo;
import com.apkscanner.data.apkinfo.SupportsGlTextureInfo;
import com.apkscanner.data.apkinfo.SupportsScreensInfo;
import com.apkscanner.data.apkinfo.UsesConfigurationInfo;
import com.apkscanner.data.apkinfo.UsesFeatureInfo;
import com.apkscanner.data.apkinfo.UsesLibraryInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.data.apkinfo.UsesPermissionSdk23Info;
import com.apkscanner.data.apkinfo.WidgetInfo;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreeNode;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.util.Log;

public class AaptManifestReader
{
	private AaptXmlTreePath manifestPath;
	private String namespace;
	private ManifestInfo manifestInfo;
	private AaptNativeScanner resourceScanner;
	private AxmlToXml a2xml;

	public AaptManifestReader()
	{
		this(null);
	}

	public AaptManifestReader(AaptXmlTreePath manifestPath)
	{
		this(manifestPath, null);
	}

	public AaptManifestReader(AaptXmlTreePath manifestPath, ManifestInfo targetManifest)
	{
		setManifestPath(manifestPath);
		manifestInfo = targetManifest != null
				? targetManifest : new ManifestInfo();
		resourceScanner = null;
	}

	public ManifestInfo getManifestInfo()
	{
		return manifestInfo;
	}

	public void setManifestPath(AaptXmlTreePath manifestPath)
	{
		this.manifestPath = manifestPath;
		if(manifestPath != null) {
			namespace = manifestPath.getAndroidNamespaceTag() + ":";
		}
	}

	public void setResourceScanner(AaptNativeScanner scanner) {
		this.resourceScanner = scanner;
		a2xml = new AxmlToXml(resourceScanner);
		a2xml.setMultiLinePrint(RProp.B.PRINT_MULTILINE_ATTR.get());
	}

	public void readBasicInfo()
	{
		if(manifestPath == null) return;

		AaptXmlTreeNode tagNode = manifestPath.getNode("/manifest");
		if(tagNode == null) {
			Log.e("error: node not existed : /manifest");
			return;
		}

		// package
		manifestInfo.packageName = getAttrValue(tagNode , "package");
		manifestInfo.versionCode = getAttrIntegerValue(tagNode, "versionCode");
		manifestInfo.versionName = getAttrValue(tagNode, "versionName");
		manifestInfo.sharedUserId = getAttrValue(tagNode, "sharedUserId");
		manifestInfo.sharedUserLabels = getAttrResourceValues(tagNode, "sharedUserLabels");

		if(manifestInfo.versionName != null && manifestInfo.versionName.startsWith("@")) {
			ResourceInfo[] res = getAttrResourceValues(tagNode, "versionName");
			if(res != null && res[0].name != null) {
				manifestInfo.versionName = res[0].name;
			}
		}

		String installLocation = getAttrValue(tagNode, "installLocation");
		if(installLocation == null || installLocation.equals("1")) {
			manifestInfo.installLocation = "internalOnly";
		} else if(installLocation.equals("0")) {
			manifestInfo.installLocation = "auto";
		} else if(installLocation.equals("2")) {
			manifestInfo.installLocation = "preferExternal";
		}

		// label & icon
		tagNode = manifestPath.getNode("/manifest/application");
		if(tagNode == null) {
			Log.e("error: node not existed : /manifest/application");
			return;
		}
		manifestInfo.application.labels = getAttrResourceValues(tagNode, "label");
		manifestInfo.application.icons = getAttrResourceValues(tagNode, "icon");
		manifestInfo.application.debuggable = getAttrBooleanValue(tagNode, "debuggable");

		tagNode = manifestPath.getNode("/manifest/uses-sdk");
		if(tagNode != null) {
			manifestInfo.usesSdk.targetSdkVersion = getAttrIntegerValue(tagNode, "targetSdkVersion");
			manifestInfo.usesSdk.minSdkVersion = getAttrIntegerValue(tagNode, "minSdkVersion");
			manifestInfo.usesSdk.maxSdkVersion = getAttrIntegerValue(tagNode, "maxSdkVersion");
		}
		manifestInfo.compatibleScreens = getCompatibleScreens(manifestPath.getNodeList("/manifest/compatible-screens"));
		manifestInfo.supportsScreens = getSupportsScreens(manifestPath.getNodeList("/manifest/supports-screens"));
		manifestInfo.supportsGlTexture = getSupportsGlTexture(manifestPath.getNodeList("/manifest/supports-gl-texture"));
		manifestInfo.usesConfiguration = getUsesConfiguration(manifestPath.getNodeList("/manifest/uses-configuration"));
		manifestInfo.usesFeature = getUsesFeature(manifestPath.getNodeList("/manifest/uses-feature"));
		manifestInfo.usesLibrary = getUsesLibrary(manifestPath.getNodeList("/manifest/uses-library"));

        // display to launchur
		AaptXmlTreeNode[] launchers = manifestPath.getNodeList("/manifest/application/activity/intent-filter/"
				+ "category[@" + namespace + "name='android.intent.category.LAUNCHER']/../"
				+ "action[@" + namespace + "name='android.intent.action.MAIN']");

        if(launchers != null && launchers.length > 0) {
        	manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_LAUNCHUR;
        } else {
	        launchers = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/"
					+ "category[@" + namespace + "name='android.intent.category.LAUNCHER']/../"
					+ "action[@" + namespace + "name='android.intent.action.MAIN']");
	        if(launchers != null && launchers.length > 0) {
	        	manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_LAUNCHUR;
	        }
        }

        // startup
        if(manifestPath.getNode("/manifest/uses-permission[@"+namespace+"name='android.permission.RECEIVE_BOOT_COMPLETED']") != null) {
        	manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_STARTUP;
        }

        if(manifestPath.getNode("/manifest/instrumentation") != null) {
			manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_INSTRUMENTATION;
		}
	}

	public void readPermissions()
	{
		// permission
        Log.i("read uses-permission");
        List<Object> usesPermissionList = new ArrayList<>();
        AaptXmlTreeNode[] permTag = manifestPath.getNodeList("/manifest/uses-permission");
        if(permTag != null && permTag.length > 0) {
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	UsesPermissionInfo info = new UsesPermissionInfo();
	        	info.name = getAttrValue(permTag[idx], "name");
        		info.maxSdkVersion = getAttrIntegerValue(permTag[idx], "maxSdkVersion");
	        	usesPermissionList.add(info);
	        }
	        manifestInfo.usesPermission = usesPermissionList.toArray(new UsesPermissionInfo[usesPermissionList.size()]);
	        usesPermissionList.clear();
        }

        Log.i("read uses-permission-sdk-23");
        permTag = manifestPath.getNodeList("/manifest/uses-permission-sdk-23");
        if(permTag != null && permTag.length > 0) {
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	UsesPermissionInfo info = new UsesPermissionSdk23Info();
	        	info.name = getAttrValue(permTag[idx], "name");
        		info.maxSdkVersion = getAttrIntegerValue(permTag[idx], "maxSdkVersion");
	        	usesPermissionList.add(info);
	        }
	        manifestInfo.usesPermissionSdk23 = usesPermissionList.toArray(new UsesPermissionSdk23Info[0]);
        }

        Log.i("read permission");
        permTag = manifestPath.getNodeList("/manifest/permission");
        if(permTag != null && permTag.length > 0) {
        	ArrayList<PermissionInfo> permissionList = new ArrayList<PermissionInfo>();
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	PermissionInfo info = new PermissionInfo();
	        	if(!"android".equals(manifestInfo.packageName)) {
		        	info.descriptions = getAttrResourceValues(permTag[idx], "description");
		        	info.icons = getAttrResourceValues(permTag[idx], "icon");
		        	info.labels = getAttrResourceValues(permTag[idx], "label");
	        	}
	        	info.name = getAttrValue(permTag[idx], "name");
	        	info.permissionGroup = getAttrValue(permTag[idx], "permissionGroup");
	        	String protectionLevel = getAttrValue(permTag[idx], "protectionLevel");
	        	if(protectionLevel != null) {
	        		try {
	        			int level;
		        		if(protectionLevel.startsWith("0x")) {
		        			level = Integer.parseInt(protectionLevel.substring(2), 16);
		        		} else {
		        			level = Integer.parseInt(protectionLevel);
		        		}
		        		protectionLevel = PermissionInfo.protectionToString(level);
	        		} catch (NumberFormatException e) { }
	        		info.protectionLevel = protectionLevel;
	        	}
	        	permissionList.add(info);
	        }
	        manifestInfo.permission = permissionList.toArray(new PermissionInfo[permissionList.size()]);
        }

        Log.i("read permission-tree");
        permTag = manifestPath.getNodeList("/manifest/permission-tree");
        if(permTag != null && permTag.length > 0) {
        	ArrayList<PermissionTreeInfo> permissionList = new ArrayList<PermissionTreeInfo>();
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	PermissionTreeInfo info = new PermissionTreeInfo();
	        	//info.icons = getAttrResourceValues(permTag[idx], "icon");
	        	//info.labels = getAttrResourceValues(permTag[idx], "label");
	        	info.name = getAttrValue(permTag[idx], "name");
	        	permissionList.add(info);
	        }
	        manifestInfo.permissionTree = permissionList.toArray(new PermissionTreeInfo[permissionList.size()]);
        }
	}

	public WidgetInfo[] getWidgetList(String apkFilePath)
	{
        ArrayList<WidgetInfo> widgetList = new ArrayList<WidgetInfo>();

		AaptXmlTreeNode[] widgetTag = manifestPath.getNodeList("/manifest/application/receiver/"
        		+ "meta-data[@" + namespace + "name='android.appwidget.provider']/..");

        for( int idx=0; idx < widgetTag.length; idx++ ){
        	WidgetInfo widget = new WidgetInfo();
        	widget.icons = manifestInfo.application.icons;
        	widget.type = "Normal";
        	widget.lables = getAttrResourceValues(widgetTag[idx], "label");
        	widget.name = getAttrValue(widgetTag[idx], "name");
        	if(widget.name != null && widget.name.startsWith(".")) {
        		widget.name = manifestInfo.packageName + widget.name;
        	}
        	widget.enabled = getAttrBooleanValue(widgetTag[idx], "enabled");

        	AaptXmlTreeNode[] meta = widgetTag[idx].getNodeList("meta-data");
        	String resource = null;
        	for(AaptXmlTreeNode m: meta) {
        		if("android.appwidget.provider".equals(getAttrValue(m, "name"))) {
        			resource = m.getAttribute(namespace + "resource");
        			if(resource != null) break;
        		}
        	}

        	if(resource != null) {
	        	Object[] extraInfo = getWidgetInfo(apkFilePath, getResourceValues(resource));
	        	if(extraInfo[0] != null) {
	        		widget.icons = (ResourceInfo[]) extraInfo[0];
	        	}
	        	if(extraInfo[1] != null) {
	        		widget.size = (String) extraInfo[1];
	        	}
        	} else {
        		widget.size = "Unknown";
        	}
        	widgetList.add(widget);
        }

        widgetTag = manifestPath.getNodeList("/manifest/application/activity/intent-filter/"
        		+ "category[@" + namespace + "name='android.intent.category.LAUNCHER']/../"
        		+ "action[@" + namespace + "name='android.intent.action.MAIN']/../../"
        		+ "meta-data[@" + namespace + "name='android.app.shortcuts']");
        for(AaptXmlTreeNode node: widgetTag) {
        	WidgetInfo[] shorcuts = getLuncherShortCuts(apkFilePath, getAttrResourceValues(node, "resource"));
        	if(shorcuts != null) {
        		for(WidgetInfo w: shorcuts) {
        			if(w.name == null || w.name.isEmpty()) {
        				w.name = getAttrValue(node.getParent(), "name");
        			}
        			widgetList.add(w);
        		}
        	}
        }

        widgetTag = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/"
        		+ "category[@" + namespace + "name='android.intent.category.LAUNCHER']/../"
        		+ "action[@" + namespace + "name='android.intent.action.MAIN']/../../"
        		+ "meta-data[@" + namespace + "name='android.app.shortcuts']");
        for(AaptXmlTreeNode node: widgetTag) {
        	WidgetInfo[] shorcuts = getLuncherShortCuts(apkFilePath, getAttrResourceValues(node, "resource"));
        	if(shorcuts != null) {
        		for(WidgetInfo w: shorcuts) {
        			if(w.name == null || w.name.isEmpty()) {
        				w.name = getAttrValue(node.getParent(), "name");
        			}
        			widgetList.add(w);
        		}
        	}
        }

        widgetTag = manifestPath.getNodeList("/manifest/application/activity/intent-filter/"
        		+ "action[@" + namespace + "name='android.intent.action.CREATE_SHORTCUT']/../..");
        for(AaptXmlTreeNode node: widgetTag) {
        	widgetList.add(getActivityShortCut(node));
        }

        widgetTag = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/"
        		+ "action[@" + namespace + "name='android.intent.action.CREATE_SHORTCUT']/../..");
        for(AaptXmlTreeNode node: widgetTag) {
        	widgetList.add(getActivityShortCut(node));
        }

		return widgetList.toArray(new WidgetInfo[0]);
	}

	private Object[] getWidgetInfo(String apkFilePath, ResourceInfo[] widgetRes)
	{
		List<ResourceInfo> iconResList = new ArrayList<>();
		List<String> previewIds = new ArrayList<>();
		String widgetSize = null;

		if(widgetRes == null || widgetRes.length <= 0
				|| apkFilePath == null || !(new File(apkFilePath)).exists()) {
			return new Object[] { null, null };
		}

		for(ResourceInfo r: widgetRes) {
			if(r.name == null || r.name.isEmpty()) continue;

			String[] wdgXml = AaptNativeWrapper.Dump.getXmltree(apkFilePath, new String[] { r.name });
			AaptXmlTreePath widgetTree = new AaptXmlTreePath(wdgXml);
			String widgetNamespace = widgetTree.getAndroidNamespaceTag() + ":";
			Log.v("widgetNamespace : " + widgetNamespace);

			AaptXmlTreeNode widgetNode = widgetTree.getNode("/appwidget-provider");
			if(widgetNode == null) {
				Log.w("appwidget-provider is null in " + r.name);
				continue;
			}

			String width = null, height = null;

			ResourceInfo[] res = getAttrResourceValues(widgetNode, "minWidth", widgetNamespace);
			if(res != null && res.length > 0) {
				if(res[0].name.startsWith("0x")) {
					Log.w("Unknown widget width : " + res[0].name);
				} else {
					width = res[0].name.replaceAll("^([0-9]+).*", "$1");
				}
			}

			res = getAttrResourceValues(widgetNode, "minHeight", widgetNamespace);
			if(res != null && res.length > 0) {
				if(res[0].name.startsWith("0x")) {
					Log.w("Unknown widget height " + height);
				} else {
					height = res[0].name.replaceAll("^([0-9]+).*", "$1");
				}
			}

			if(width != null && height != null) {
				widgetSize = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X "
						+ (int)Math.ceil((Float.parseFloat(height) - 40) / 76 + 1);
				widgetSize += "\n(" + width + " X " + height + ")";

				String ReSizeMode = getAttrValue(widgetNode, "resizeMode", widgetNamespace);
				if("0x0".equals(ReSizeMode)) {
					ReSizeMode = null;
				} else if("0x1".equals(ReSizeMode)) {
					ReSizeMode = "horizontal";
				} else if("0x2".equals(ReSizeMode)) {
					ReSizeMode = "vertical";
				} else if("0x3".equals(ReSizeMode)) {
					ReSizeMode = "horizontal|vertical";
				}
				if(ReSizeMode != null) {
					widgetSize += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
				}
			}

			String preview = getAttrValue(widgetNode, "previewImage", widgetNamespace);
			if(preview != null && !previewIds.contains(preview)) {
				previewIds.add(preview);
				ResourceInfo[] iconPaths = getResourceValues(preview);
				if(iconPaths != null) {
					String urlFilePath = apkFilePath.replaceAll("#", "%23");
					String jarPath = "jar:file:" + urlFilePath + "!/";
					for(ResourceInfo icon: iconPaths) {
						if(icon.name != null && !icon.name.isEmpty()) {
							if(icon.name.endsWith("qmg")) {
								icon.name = RImg.QMG_IMAGE_ICON.getPath();
							} else {
								icon.name = jarPath + icon.name;
							}
							iconResList.add(icon);
						}
					}
				}
			}
		}

		ResourceInfo[] iconList = null;
		if(iconResList.size() > 0)
			iconList = iconResList.toArray(new ResourceInfo[iconResList.size()]);
		if(widgetSize == null)
			widgetSize = "Unknown";

		return new Object[] { iconList, widgetSize };
	}

	private WidgetInfo[] getLuncherShortCuts(String apkFilePath, ResourceInfo[] shortcutRes) {
		if(shortcutRes == null || shortcutRes.length <= 0) {
			return null;
		}

		ResourceInfo xmlRes = null;
		for(ResourceInfo r: shortcutRes) {
			if(r == null || r.name == null || r.name.isEmpty()) continue;
			if(xmlRes == null || getV(xmlRes.configuration) < getV(r.configuration)) {
				xmlRes = r;
			}
		}
		Log.v("getLuncherShortCuts() " + xmlRes.configuration + ", " + xmlRes.name);

		String[] wdgXml = AaptNativeWrapper.Dump.getXmltree(apkFilePath, new String[] { xmlRes.name });
		AaptXmlTreePath widgetTree = new AaptXmlTreePath(wdgXml);
		String widgetNamespace = widgetTree.getAndroidNamespaceTag() + ":";
		Log.v("widgetNamespace : " + widgetNamespace);

		AaptXmlTreeNode[] shortcuts = widgetTree.getNodeList("/shortcuts/shortcut");
		if(shortcuts == null || shortcuts.length <= 0) {
			Log.w("no such shortcut" + xmlRes.name);
			return null;
		}

		WidgetInfo[] shortcutList = new WidgetInfo[shortcuts.length];
		for(int i = 0; i < shortcuts.length; i++) {
			WidgetInfo widget = new WidgetInfo();
			shortcutList[i] = widget;

			widget.type = "Shortcut";
	    	widget.size = "1 X 1";
			widget.enabled = getAttrBooleanValue(shortcuts[i], "enabled", widgetNamespace);

			widget.icons = getAttrResourceValues(shortcuts[i], "icon", widgetNamespace);
			if(widget.icons != null) {
				String urlFilePath = apkFilePath.replaceAll("#", "%23");
				String jarPath = "jar:file:" + urlFilePath + "!/";
				for(ResourceInfo icon: widget.icons) {
					if(icon.name != null && !icon.name.isEmpty()) {
						if(icon.name.endsWith("qmg")) {
							icon.name = RImg.QMG_IMAGE_ICON.getPath();
						} else {
							icon.name = jarPath + icon.name;
						}
					}
				}
			}
			if(widget.icons == null)
				widget.icons = manifestInfo.application.icons;

			widget.lables = getAttrResourceValues(shortcuts[i], "shortcutLongLabel", widgetNamespace);
	    	if(widget.lables == null)
	    		widget.lables = getAttrResourceValues(shortcuts[i], "shortcutShortLabel", widgetNamespace);
	    	if(widget.lables == null)
	    		widget.lables = manifestInfo.application.labels;

	    	AaptXmlTreeNode[] intentNodes = shortcuts[i].getNodeList("intent");
	    	if(intentNodes != null) {
	        	for(AaptXmlTreeNode intent: intentNodes) {
	        		widget.name = getAttrValue(intent, "targetClass", widgetNamespace);
	        		if(widget.name != null) break;
	        	}
	    	}
	    	if(widget.name != null && widget.name.startsWith("."))
	    		widget.name = manifestInfo.packageName + widget.name;
		}

		return shortcutList;
	}

	private int getV(String config) {
		if(config == null || !config.matches(".*v(\\d+).*")) return 0;
		return Integer.parseInt(config.replaceAll(".*v(\\d+).*", "$1"));
	}

	private WidgetInfo getActivityShortCut(AaptXmlTreeNode node) {
    	WidgetInfo widget = new WidgetInfo();
    	widget.icons = manifestInfo.application.icons;
    	widget.type = "Shortcut";
    	widget.lables = getAttrResourceValues(node, "label");
    	if(widget.lables == null)
    		widget.lables = manifestInfo.application.labels;
    	widget.name = getAttrValue(node, "name");
    	if(widget.name != null && widget.name.startsWith("."))
    		widget.name = manifestInfo.packageName + widget.name;
    	widget.size = "1 X 1";
		return widget;
	}

	private ActionInfo[] getActionInfo(AaptXmlTreeNode[] actionNodeList)
	{
		if(actionNodeList == null || actionNodeList.length == 0) return null;

		ArrayList<ActionInfo> list = new ArrayList<ActionInfo>();
		for(AaptXmlTreeNode node: actionNodeList) {
			ActionInfo info = new ActionInfo();
			info.name = getAttrValue(node, "name");
			list.add(info);
		}
		return list.toArray(new ActionInfo[0]);
	}

	private CategoryInfo[] getCategoryInfo(AaptXmlTreeNode[] categoryNodeList)
	{
		if(categoryNodeList == null || categoryNodeList.length == 0) return null;

		ArrayList<CategoryInfo> list = new ArrayList<CategoryInfo>();
		for(AaptXmlTreeNode node: categoryNodeList) {
			CategoryInfo info = new CategoryInfo();
			info.name = getAttrValue(node, "name");
			list.add(info);
		}
		return list.toArray(new CategoryInfo[0]);
	}

	private MetaDataInfo[] getMetaDataInfo(AaptXmlTreeNode[] metaDataNodeList)
	{
		if(metaDataNodeList == null || metaDataNodeList.length == 0) return null;

		ArrayList<MetaDataInfo> list = new ArrayList<MetaDataInfo>();
		for(AaptXmlTreeNode node: metaDataNodeList) {
			MetaDataInfo info = new MetaDataInfo();
			info.name = getAttrValue(node, "name");
			info.resources = getAttrResourceValues(node, "resource");
			info.value = getAttrValue(node, "value");
			list.add(info);
		}
		return list.toArray(new MetaDataInfo[0]);
	}

	private DataInfo[] getDataInfo(AaptXmlTreeNode[] dataNodeList)
	{
		if(dataNodeList == null || dataNodeList.length == 0) return null;

		ArrayList<DataInfo> list = new ArrayList<DataInfo>();
		for(AaptXmlTreeNode node: dataNodeList) {
			DataInfo info = new DataInfo();
			info.scheme = getAttrValue(node, "scheme");
			info.host = getAttrValue(node, "host");
			info.port = getAttrValue(node, "port");
			info.path = getAttrValue(node, "path");
			info.pathPattern = getAttrValue(node, "pathPattern");
			info.pathPrefix = getAttrValue(node, "pathPrefix");
			info.mimeType = getAttrValue(node, "mimeType");
			list.add(info);
		}
		return list.toArray(new DataInfo[0]);
	}

	private IntentFilterInfo[] getIntentFilterInfo(AaptXmlTreeNode[] intentFilterNodeList)
	{
    	if(intentFilterNodeList == null || intentFilterNodeList.length == 0) return null;

    	ArrayList<IntentFilterInfo> intentList = new ArrayList<IntentFilterInfo>();
		for(AaptXmlTreeNode intent: intentFilterNodeList) {
			IntentFilterInfo intentFilter = new IntentFilterInfo();
			intentFilter.ation = getActionInfo(intent.getNodeList("action"));
			intentFilter.category = getCategoryInfo(intent.getNodeList("category"));
			intentFilter.data = getDataInfo(intent.getNodeList("data"));
			intentList.add(intentFilter);
		}
		return intentList.toArray(new IntentFilterInfo[0]);
	}

	private Integer checkIntentFlag(IntentFilterInfo[] intentFilterInfo)
	{
		Integer featureFlag = 0;
    	if(intentFilterInfo == null) return featureFlag;

    	for(IntentFilterInfo intent: intentFilterInfo) {
    		if(intent.ation != null
    				&& (featureFlag & (ApkInfo.APP_FEATURE_MAIN | ApkInfo.APP_FEATURE_STARTUP))
    					!= (ApkInfo.APP_FEATURE_MAIN | ApkInfo.APP_FEATURE_STARTUP)) {
        		for(ActionInfo action: intent.ation) {
    				if("android.intent.action.BOOT_COMPLETED".equals(action.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_STARTUP;
    				} else if("android.intent.action.MAIN".equals(action.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_MAIN;
    				}
        		}
    		}
    		if(intent.category != null
    				&& (featureFlag & (ApkInfo.APP_FEATURE_LAUNCHER | ApkInfo.APP_FEATURE_DEFAULT))
    					!= (ApkInfo.APP_FEATURE_LAUNCHER | ApkInfo.APP_FEATURE_DEFAULT)) {
    			for(CategoryInfo category: intent.category) {
    				if("android.intent.category.LAUNCHER".equals(category.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_LAUNCHER;
    				} else if("android.intent.category.DEFAULT".equals(category.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_DEFAULT;
    				}
    			}
    		}
    	}

		return featureFlag;
	}

	public void readComponents()
	{
        readActivityInfo();
        readActivityAliasInfo();
        readServiceInfo();
        readReceiverInfo();
        readProviderInfo();
	}

	public void readActivityInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/activity");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ActivityInfo> activityList = new ArrayList<ActivityInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ActivityInfo info = new ActivityInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	info.exported = getAttrBooleanValue(activityTag[idx], "exported");
        	info.enabled =  getAttrBooleanValue(activityTag[idx], "enabled");
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	info.featureFlag |= checkIntentFlag(info.intentFilter);

        	if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
        		activityList.add(0, info);
        	} else {
        		activityList.add(info);
        	}

        	info.xmlString = a2xml.convertToText(activityTag[idx], namespace);
        }

        manifestInfo.application.activity = activityList.toArray(new ActivityInfo[0]);
	}

	public void readActivityAliasInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/activity-alias");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ActivityAliasInfo> list = new ArrayList<ActivityAliasInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ActivityAliasInfo info = new ActivityAliasInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	info.exported = getAttrBooleanValue(activityTag[idx], "exported");
        	info.enabled =  getAttrBooleanValue(activityTag[idx], "enabled");
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.targetActivity = getAttrValue(activityTag[idx], "targetActivity");
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	info.featureFlag |= checkIntentFlag(info.intentFilter);

        	if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
        		list.add(0, info);
        	} else {
        		list.add(info);
        	}

        	info.xmlString = a2xml.convertToText(activityTag[idx], namespace);
        }

        manifestInfo.application.activityAlias = list.toArray(new ActivityAliasInfo[0]);
	}

	public void readServiceInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/service");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ServiceInfo> list = new ArrayList<ServiceInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ServiceInfo info = new ServiceInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	info.exported = getAttrBooleanValue(activityTag[idx], "exported");
        	info.enabled =  getAttrBooleanValue(activityTag[idx], "enabled");
        	info.isolatedProcess = getAttrBooleanValue(activityTag[idx], "isolatedProcess");
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	list.add(info);

        	info.xmlString = a2xml.convertToText(activityTag[idx], namespace);
        }

        manifestInfo.application.service = list.toArray(new ServiceInfo[0]);
	}

	public void readReceiverInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/receiver");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ReceiverInfo> list = new ArrayList<ReceiverInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ReceiverInfo info = new ReceiverInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	info.exported = getAttrBooleanValue(activityTag[idx], "exported");
        	info.enabled =  getAttrBooleanValue(activityTag[idx], "enabled");
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	list.add(info);

        	info.xmlString = a2xml.convertToText(activityTag[idx], namespace);
        }

        manifestInfo.application.receiver = list.toArray(new ReceiverInfo[0]);
	}

	public void readProviderInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/provider");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ProviderInfo> list = new ArrayList<ProviderInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ProviderInfo info = new ProviderInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	info.readPermission = getAttrValue(activityTag[idx], "readPermission");
        	info.writePermission = getAttrValue(activityTag[idx], "writePermission");
        	info.exported = getAttrBooleanValue(activityTag[idx], "exported");
        	info.enabled =  getAttrBooleanValue(activityTag[idx], "enabled");
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	list.add(info);

        	info.xmlString = a2xml.convertToText(activityTag[idx], namespace);
        }

        manifestInfo.application.provider = list.toArray(new ProviderInfo[0]);
	}

	private CompatibleScreensInfo[] getCompatibleScreens(AaptXmlTreeNode[] nodeList)
	{
		if(nodeList == null || nodeList.length == 0) return null;

    	ArrayList<CompatibleScreensInfo> intentList = new ArrayList<CompatibleScreensInfo>();
		for(AaptXmlTreeNode node: nodeList) {
			CompatibleScreensInfo info = new CompatibleScreensInfo();

			AaptXmlTreeNode[] screenNode = node.getNodeList("screen");
			if(screenNode != null) {
				ArrayList<CompatibleScreensInfo.Screen> screensList = new ArrayList<CompatibleScreensInfo.Screen>();
				for(AaptXmlTreeNode s: screenNode) {
					CompatibleScreensInfo.Screen screen = new CompatibleScreensInfo.Screen();
					screen.screenSize = getAttrValue(s, "screenSize");
					screen.screenDensity = getAttrValue(s, "screenDensity");
					screensList.add(screen);
				}
				info.screen = screensList.toArray(new CompatibleScreensInfo.Screen[0]);
			}

			intentList.add(info);
		}
		return intentList.toArray(new CompatibleScreensInfo[0]);
	}

	private SupportsScreensInfo[] getSupportsScreens(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null;

		ArrayList<SupportsScreensInfo> list = new ArrayList<SupportsScreensInfo>();
		for(AaptXmlTreeNode n: tagNodeList) {
			SupportsScreensInfo info = new SupportsScreensInfo();

			info.resizeable = getAttrBooleanValue(n, "resizeable");
			info.smallScreens = getAttrBooleanValue(n, "smallScreens");
			info.normalScreens = getAttrBooleanValue(n, "normalScreens");
			info.largeScreens = getAttrBooleanValue(n, "largeScreens");
			info.xlargeScreens = getAttrBooleanValue(n, "xlargeScreens");
			info.anyDensity = getAttrBooleanValue(n, "anyDensity");
			info.resizeable = getAttrBooleanValue(n, "resizeable");
			info.resizeable = getAttrBooleanValue(n, "resizeable");
			info.resizeable = getAttrBooleanValue(n, "resizeable");
			info.requiresSmallestWidthDp = getAttrIntegerValue(n, "requiresSmallestWidthDp");
			info.compatibleWidthLimitDp = getAttrIntegerValue(n, "compatibleWidthLimitDp");
			info.largestWidthLimitDp = getAttrIntegerValue(n, "largestWidthLimitDp");
			list.add(info);
		}

		return list.toArray(new SupportsScreensInfo[0]);
	}

	private UsesConfigurationInfo[] getUsesConfiguration(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null;

		ArrayList<UsesConfigurationInfo> list = new ArrayList<UsesConfigurationInfo>();
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesConfigurationInfo info = new UsesConfigurationInfo();
			info.reqFiveWayNav = getAttrBooleanValue(n, "reqFiveWayNav");
			info.reqHardKeyboard = getAttrBooleanValue(n, "reqHardKeyboard");
			info.reqKeyboardType = getAttrValue(n, "reqKeyboardType");
			info.reqNavigation = getAttrValue(n, "reqNavigation");
			info.reqTouchScreen = getAttrValue(n, "reqTouchScreen");
			list.add(info);
		}

		return list.toArray(new UsesConfigurationInfo[0]);
	}

	private UsesFeatureInfo[] getUsesFeature(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null;

		ArrayList<UsesFeatureInfo> list = new ArrayList<UsesFeatureInfo>();
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesFeatureInfo info = new UsesFeatureInfo();
			info.name = getAttrValue(n, "name");
			info.required = getAttrBooleanValue(n, "required");
			info.glEsVersion = getAttrIntegerValue(n, "glEsVersion");
			list.add(info);
		}

		return list.toArray(new UsesFeatureInfo[0]);
	}

	private UsesLibraryInfo[] getUsesLibrary(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null;

		ArrayList<UsesLibraryInfo> list = new ArrayList<UsesLibraryInfo>();
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesLibraryInfo info = new UsesLibraryInfo();
			info.name = getAttrValue(n, "name");
			info.required = getAttrBooleanValue(n, "required");
			list.add(info);
		}

		return list.toArray(new UsesLibraryInfo[0]);
	}

	private SupportsGlTextureInfo[] getSupportsGlTexture(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null;

		ArrayList<SupportsGlTextureInfo> list = new ArrayList<SupportsGlTextureInfo>();
		for(AaptXmlTreeNode n: tagNodeList) {
			SupportsGlTextureInfo info = new SupportsGlTextureInfo();
			info.name = getAttrValue(n, "name");
			list.add(info);
		}
		return list.toArray(new SupportsGlTextureInfo[0]);
	}

	private String getAttrValue(AaptXmlTreeNode node, String attr)
	{
		return getAttrValue(node, attr, namespace);
	}

	private String getAttrValue(AaptXmlTreeNode node, String attr, String namespace)
	{
		String value = node.getAttribute(namespace + attr);
		return value != null ? value : node.getAttribute(attr);
	}

	private Boolean getAttrBooleanValue(AaptXmlTreeNode node, String attr)
	{
		return getAttrBooleanValue(node, attr, namespace);
	}

	private Boolean getAttrBooleanValue(AaptXmlTreeNode node, String attr, String namespace)
	{
		Boolean result = null;
		String value = getAttrValue(node, attr, namespace);
    	if(value != null) {
    		if(value.startsWith("@")) {
    			ResourceInfo[] resVal = getResourceValues(value);
    			result = "#ffffffff".equals(resVal[0].name);
    		} else {
    			result = "true".equals(value);
    		}
    	}
    	return result;
	}

	private Integer getAttrIntegerValue(AaptXmlTreeNode node, String attr)
	{
		return getAttrIntegerValue(node, attr, namespace);
	}

	private Integer getAttrIntegerValue(AaptXmlTreeNode node, String attr, String namespace)
	{
		Integer result = null;
		String value = getAttrValue(node, attr, namespace);
    	if(value != null) {
    		if(value.startsWith("@")) {
    			ResourceInfo[] resVal = getResourceValues(value);
    			value = resVal[0].name;
    		}
    		if(value.startsWith("0x")) {
    			result = Integer.parseInt(value.substring(2), 16);
    		} else {
    			result = Integer.parseInt(value);
    		}
    	}
    	return result;
	}

	private ResourceInfo[] getAttrResourceValues(AaptXmlTreeNode node, String attr)
	{
		return getAttrResourceValues(node, attr, namespace);
	}

	public ResourceInfo[] getAttrResourceValues(AaptXmlTreeNode node, String attr, String namespace)
	{
		return getResourceValues(getAttrValue(node, attr, namespace));
	}

	public ResourceInfo[] getResourceValues(String id)
	{
		ResourceInfo[] resVal = null;
		while(id != null && id.startsWith("@")) {
			if(!id.matches("@0x[\\da-fA-F]+\\s*")) {
				resVal = null;
				break;
			}
			resVal = resourceScanner.getResourceValues(id);
			if(resVal == null || resVal.length == 0 || id == resVal[0].name)
				break;
			id = resVal[0].name;
		}
		if(id != null && (resVal == null || resVal.length == 0)) {
			resVal = new ResourceInfo[] { new ResourceInfo(id, null) };
		}
		return resVal;
	}
}
