package com.apkscanner.apkscanner;

import com.apkscanner.apkinfo.PermissionInfo;
import com.apkscanner.data.AaptXmlTreeNode;
import com.apkscanner.data.AaptXmlTreePath;

public class AxmlToXml {
	
	private AaptXmlTreePath axmlPath;
	private String[] resourcesWithValue;
	
	private boolean isMultiLinePrint = false;

	public AxmlToXml(String[] axml, String[] resourcesWithValue) {
		this.resourcesWithValue = resourcesWithValue;
		
		axmlPath = new AaptXmlTreePath();
		axmlPath.createAaptXmlTree(axml);
	}
	
	public void setMultiLinePrint(boolean isMultiLinePrint) {
		this.isMultiLinePrint = isMultiLinePrint;
	}

	private String getResourceName(String id)
	{
		if(resourcesWithValue == null || id == null || !id.startsWith("@"))
			return id;
		String name = id;
		String filter = "spec resource " + id.substring(1);
		for(String s: resourcesWithValue) {
			if(s.indexOf(filter) > -1) {
				name = s.replaceAll(".*:(.*):.*", "@$1");
				break;
			}
		}
		return name;
	}
	
	private String makeNodeXml(AaptXmlTreeNode node, String namespace, String depthSpace)
	{
		StringBuilder xml = new StringBuilder(depthSpace);

		xml.append("<" + node.getName());
		String attrDepthSpace = "\r\n" + xml.toString().replaceAll(".", " ");
		boolean firstAttr = true;
		
		if(node.getName().equals("manifest")) {
			xml.append(" xmlns:");
			xml.append(axmlPath.getNamespace());
			xml.append("=\"http://schemas.android.com/apk/res/android\"");
		}
		for(String name: node.getAttributeList()) {
			if(isMultiLinePrint && !firstAttr) {
				xml.append(attrDepthSpace);
			}
			firstAttr = false;
			xml.append(" ");
			xml.append(name);
			xml.append("=\"");
			if(name.endsWith("protectionLevel")) {
				String protection = node.getAttribute(name);
	        	if(protection != null && protection.startsWith("0x")) {
	        		int level = Integer.parseInt(protection.substring(2), 16);
	        		xml.append(PermissionInfo.protectionToString(level));
	        	} else {
	        		xml.append(protection);
	        	}
			} else if(name.endsWith("layout_width") || name.endsWith("layout_height")) {
				String val = getResourceName(node.getAttribute(name));
				if("-1".equals(val)) {
					val = "match_parent";
				} else if("-2".equals(val)) {
					val = "wrap_content";
				}
				xml.append(val);
			} else if(name.endsWith("orientation")) {
				String val = getResourceName(node.getAttribute(name));
				if("0".equals(val)) {
					val = "horizontal";
				} else if("1".equals(val)) {
					val = "vertical";
				}
				xml.append(val);
			} else if(name.endsWith("gravity")) {
				String val = getResourceName(node.getAttribute(name));
				if("0".equals(val)) {
					val = "horizontal";
				} else if("1".equals(val)) {
					val = "vertical";
				}
				xml.append(val);
			} else {
				xml.append(getResourceName(node.getAttribute(name)));
			}
			xml.append("\"");
		}
		if(node.getNodeCount() > 0) {
			xml.append(">\r\n");
			for(AaptXmlTreeNode child: node.getNodeList()) {
				xml.append(makeNodeXml(child, namespace, depthSpace + "    "));
			}
			xml.append(depthSpace);
			xml.append("</");
			xml.append(node.getName());
			xml.append(">\r\n");
		} else {
			xml.append("/>\r\n");
		}
		
		return xml.toString();
	}
	
	public String toString()
	{
		if(axmlPath == null) return null;
		
		AaptXmlTreeNode topNode = axmlPath.getNode("/*");
		if(topNode == null) return null;
		
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\r\n");
		
		xml.append(makeNodeXml(topNode, axmlPath.getNamespace(), ""));
		
		return xml.toString();
	}
}
