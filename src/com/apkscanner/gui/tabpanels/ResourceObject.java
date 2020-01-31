package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.net.URI;

import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkspectrum.core.scanner.AxmlToXml;
import com.apkspectrum.tool.ImgExtractorWrapper;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.URITool;
import com.apkspectrum.util.ZipFileUtil;

public class ResourceObject extends DefaultNodeData implements Cloneable
{
	protected String config;
	protected ResourceType type;

	public ResourceObject(URI uri) {
		super(uri);
		initValue();
	}

	public ResourceObject(String uriPath) {
		this(URI.create(uriPath));
	}

	private ResourceObject(String uriPath, boolean isFolder) {
		super(URI.create(uriPath), isFolder);
		initValue();
	}

	private void initValue() {
		type = ResourceType.getType(path);

		if (type.getInt() <= ResourceType.XML.getInt()
				&& path.startsWith("res/" + type.toString() + "-")) {
			config = path.replaceAll("res/" + type.toString() + "-([^/]*)/.*", "$1");
		} else {
			config = null;
		}
	}

	public ResourceObject(ResourceType type) {
		this(type.toString(), true);
	}

	public ResourceType getResourceType() {
		return type;
	}

	@Override
	public String toString() {
		String str = null;
		int childCount = 0;
		if(node != null && !node.isRoot() /* && !isFolder */
				&& !".img".equals(getExtension()))
			childCount = node.getChildCount();

		if (childCount > 0) {
			str = label + " (" + childCount + ")";
		} else if (config != null && !config.isEmpty()) {
			str = label + " (" + config + ")";
		} else {
			str = label;
		}
		return str;
	}

	public Object getData() {
		String apkPath = null;
		if ((path.startsWith("res/") && path.endsWith(".xml"))
				|| path.equals("AndroidManifest.xml")
				|| path.endsWith(".img")) {
			apkPath = URITool.getJarPath(getURI());
		}

		if(apkPath != null) {
			switch(getExtension()) {
			case ".xml":
				return AaptNativeWrapper.Dump.getXmltree(apkPath, new String[] { path });
			case ".img":
				String tempWorkPath = FileUtil.makeTempPath(apkPath.substring(apkPath.lastIndexOf(File.separator)));
				String imgPath = tempWorkPath + File.separator + path.replace("/", File.separator);
				if(!new File(imgPath).exists()) {
					ZipFileUtil.unZip(apkPath, path, imgPath);
				}
				String data = null;
				if(SystemUtil.isWindows()) {
					data = ImgExtractorWrapper.getSuperblockInfo(imgPath);
					data += ImgExtractorWrapper.getLsInfo(imgPath);
				} else {
					data = "Not Supported in " + SystemUtil.OS;
				}
				return data;
			}
		}

		return super.getData();
	}

	public Object getData(AxmlToXml a2xConvert) {
		Object data = getData();
		if(data instanceof String[]) {
			String[] xmlbuffer = (String[]) data;
			if(RConst.AXML_VEIWER_TYPE_XML.equals(RProp.S.AXML_VIEWER_TYPE.get())
					&& ".xml".equals(getExtension())) {
				return a2xConvert.convertToText(xmlbuffer);
			} else {
				StringBuilder sb = new StringBuilder();
				for(String s: xmlbuffer) sb.append(s + "\n");
				return sb.toString().trim();
			}
		}
		return data;
	}
}