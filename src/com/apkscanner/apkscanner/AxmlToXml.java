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
			
			String val = getResourceName(node.getAttribute(name));
			if(name.endsWith("protectionLevel")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int level = Integer.parseInt(val.substring(2), 16);
	        		val = PermissionInfo.protectionToString(level);
	        	}
			} else if(name.endsWith("layout_width") || name.endsWith("layout_height")) {
				if("-1".equals(val)) {
					val = "match_parent";
				} else if("-2".equals(val)) {
					val = "wrap_content";
				}
			} else if(name.endsWith("orientation")) {
				if("0".equals(val)) {
					val = "horizontal";
				} else if("1".equals(val)) {
					val = "vertical";
				}
			} else if(name.endsWith("gravity")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int level = Integer.parseInt(val.substring(2), 16);
	        		val = "";
	        		if((level & 0x00800001) == 0x00800001) {
	        			if((level & 0x7) == 0x7 || (level & 0x6) == 0) {
	        				val += "|unknown"; 
	        			} else if((level & 0x3) == 0x3) {
	        				val += "|start"; 
	        			} else if((level & 0x5) == 0x5) {
	        				val += "|end";
	        			}
	        			level &= 0xF8;
	        		}
	        		if((level & 0x08) == 0x08) val += "|clip_horizontal";
	        		if((level & 0x80) == 0x80) val += "|clip_vertical";
	        		if((level & 0x77) == 0x77) val += "|fill";
	        		else if((level & 0x77) == 0x11) val += "|center";
	        		else {
	        			if((level & 0x07) == 0x07) val += "|fill_horizontal";
	        			else if((level & 0x07) == 0x01) val += "|center_horizontal";
	        			else if((level & 0x07) == 0x03) val += "|left";
	        			else if((level & 0x07) == 0x05) val += "|right";
	        			else if((level & 0x07) != 0x00) val += "|unknown_horizontal";

	        			if((level & 0x70) == 0x70) val += "|fill_vertical";
	        			else if((level & 0x70) == 0x10) val += "|center_vertical";
	        			else if((level & 0x70) == 0x30) val += "|top";
	        			else if((level & 0x70) == 0x50) val += "|bottom";
	        			else if((level & 0x70) != 0x00) val += "|unknown_vertical";
	        		}
	        		if(!val.isEmpty()) val = val.substring(1);
	        	}
			}
			xml.append(val);

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
