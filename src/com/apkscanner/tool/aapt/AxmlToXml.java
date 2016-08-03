package com.apkscanner.tool.aapt;

import com.apkscanner.core.scanner.AaptNativeScanner;
import com.apkscanner.data.apkinfo.PermissionInfo;

public class AxmlToXml {
	
	private AaptXmlTreePath axmlPath;
	private AaptNativeScanner resourceScanner;
	
	private boolean isMultiLinePrint = false;

	public AxmlToXml(String[] axml, AaptNativeScanner resourceScanner) {
		this.resourceScanner = resourceScanner;
		
		axmlPath = new AaptXmlTreePath();
		axmlPath.createAaptXmlTree(axml);
	}
	
	public void setMultiLinePrint(boolean isMultiLinePrint) {
		this.isMultiLinePrint = isMultiLinePrint;
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
			
			// /android/frameworks/base/core/res/res/values/attrs.xml
			// /android/frameworks/base/core/res/res/values/attrs_manifest.xml
			String val = resourceScanner.getResourceName(node.getAttribute(name));
			if(name.endsWith("protectionLevel")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int level = Integer.parseInt(val.substring(2), 16);
	        		val = PermissionInfo.protectionToString(level);
	        	}
			} else if(name.endsWith("layout_width") || name.endsWith("layout_height")) {
				switch(val) {
				case "-1" : val = "match_parent"; break;
				case "-2" : val = "wrap_content"; break;
				default: break;
				}
			} else if(name.endsWith("orientation")) {
				switch(val) {
				case "0" : val = "horizontal"; break;
				case "1" : val = "vertical"; break;
				default: break;
				}
			} else if(name.endsWith("gravity") || name.endsWith("foregroundGravity")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int iVal = Integer.parseInt(val.substring(2), 16);
	        		val = "";
	        		if((iVal & 0x00800001) == 0x00800001) {
	        			if((iVal & 0x7) == 0x7 || (iVal & 0x6) == 0) {
	        				val += "|unknown"; 
	        			} else if((iVal & 0x3) == 0x3) {
	        				val += "|start"; 
	        			} else if((iVal & 0x5) == 0x5) {
	        				val += "|end";
	        			}
	        			iVal &= 0xF8;
	        		}
	        		if((iVal & 0x08) == 0x08) val += "|clip_horizontal";
	        		if((iVal & 0x80) == 0x80) val += "|clip_vertical";
	        		if((iVal & 0x77) == 0x77) val += "|fill";
	        		else if((iVal & 0x77) == 0x11) val += "|center";
	        		else {
	        			if((iVal & 0x07) == 0x07) val += "|fill_horizontal";
	        			else if((iVal & 0x07) == 0x01) val += "|center_horizontal";
	        			else if((iVal & 0x07) == 0x03) val += "|left";
	        			else if((iVal & 0x07) == 0x05) val += "|right";
	        			else if((iVal & 0x07) != 0x00) val += "|unknown_horizontal";

	        			if((iVal & 0x70) == 0x70) val += "|fill_vertical";
	        			else if((iVal & 0x70) == 0x10) val += "|center_vertical";
	        			else if((iVal & 0x70) == 0x30) val += "|top";
	        			else if((iVal & 0x70) == 0x50) val += "|bottom";
	        			else if((iVal & 0x70) != 0x00) val += "|unknown_vertical";
	        		}
	        		if(!val.isEmpty()) val = val.substring(1);
	        	}
			} else if(name.endsWith("configChanges")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int iVal = Integer.parseInt(val.substring(2), 16);
	        		val = "";
	        		if((iVal & 0x0001) == 0x0001) val += "|mcc";
	        		if((iVal & 0x0002) == 0x0002) val += "|mnc";
	        		if((iVal & 0x0004) == 0x0004) val += "|locale";
	        		if((iVal & 0x0008) == 0x0008) val += "|touchscreen";
	        		if((iVal & 0x0010) == 0x0010) val += "|keyboard";
	        		if((iVal & 0x0020) == 0x0020) val += "|keyboardHidden";
	        		if((iVal & 0x0040) == 0x0040) val += "|navigation";
	        		if((iVal & 0x0080) == 0x0080) val += "|orientation";
	        		if((iVal & 0x0100) == 0x0100) val += "|density";
	        		if((iVal & 0x0200) == 0x0200) val += "|screenSize";
	        		if((iVal & 0x0400) == 0x0400) val += "|version";
	        		if((iVal & 0x0800) == 0x0800) val += "|screenLayout";
	        		if((iVal & 0x1000) == 0x1000) val += "|uiMode";
	        		if((iVal & 0x2000) == 0x2000) val += "|smallestScreenSize";
	        		if((iVal & 0x4000) == 0x4000) val += "|layoutDirection";
	        		if((iVal & 0x8000) == 0x8000) val += "|screenRound";
	        		if(!val.isEmpty()) val = val.substring(1);
	        	}
			} else if(name.endsWith("documentLaunchMode")) {
				switch(val) {
				case "0" : val = "none"; break;
				case "1" : val = "intoExisting"; break;
				case "2" : val = "always"; break;
				case "3" : val = "never"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("launchMode")) {
				switch(val) {
				case "0" : val = "standard"; break;
				case "1" : val = "singleTop"; break;
				case "2" : val = "singleTask"; break;
				case "3" : val = "singleInstance"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("screenOrientation")) {
				switch(val) {
				case "-1" : val = "unspecified"; break;
				case "0" : val = "landscape"; break;
				case "1" : val = "portrait"; break;
				case "2" : val = "user"; break;
				case "3" : val = "behind"; break;
				case "4" : val = "sensor"; break;
				case "5" : val = "nosensor"; break;
				case "6" : val = "sensorLandscape"; break;
				case "7" : val = "sensorPortrait"; break;
				case "8" : val = "reverseLandscape"; break;
				case "9" : val = "reversePortrait"; break;
				case "10" : val = "fullSensor"; break;
				case "11" : val = "userLandscape"; break;
				case "12" : val = "userPortrait"; break;
				case "13" : val = "fullUser"; break;
				case "14" : val = "locked"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("uiOptions")) {
				switch(val) {
				case "0" : val = "none"; break;
				case "1" : val = "splitActionBarWhenNarrow"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("windowSoftInputMode")) {
	        	if(val != null && val.startsWith("0x")) {
	        		int iVal = Integer.parseInt(val.substring(2), 16);
	        		val = "";
					switch(iVal & 0x0F) {
					case 0 : val = "stateUnspecified"; break;
					case 1 : val = "stateUnchanged"; break;
					case 2 : val = "stateHidden"; break;
					case 3 : val = "stateAlwaysHidden"; break;
					case 4 : val = "stateVisible"; break;
					case 5 : val = "stateAlwaysVisible"; break;
					default : val = "unknown(" + val + ")";
					}
					switch(iVal & 0x30) {
					case 0x00 : val += "|adjustUnspecified"; break;
					case 0x10 : val += "|adjustResize"; break;
					case 0x20 : val += "|adjustPan"; break;
					case 0x30 : val += "|adjustNothing"; break;
					default : break;
					}
	        	}
			} else if(name.endsWith("installLocation")) {
				switch(val) {
				case "0" : val = "auto"; break;
				case "1" : val = "internalOnly"; break;
				case "2" : val = "preferExternal"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("reqKeyboardType")) {
				switch(val) {
				case "0" : val = "undefined"; break;
				case "1" : val = "nokeys"; break;
				case "2" : val = "qwerty"; break;
				case "3" : val = "twelvekey"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("reqNavigation")) {
				switch(val) {
				case "0" : val = "undefined"; break;
				case "1" : val = "nonav"; break;
				case "2" : val = "dpad"; break;
				case "3" : val = "trackball"; break;
				case "4" : val = "wheel"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("reqTouchScreen")) {
				switch(val) {
				case "0" : val = "undefined"; break;
				case "1" : val = "notouch"; break;
				case "2" : val = "stylus"; break;
				case "3" : val = "finger"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("screenSize")) {
				switch(val) {
				case "200" : val = "small"; break;
				case "300" : val = "normal"; break;
				case "400" : val = "large"; break;
				case "500" : val = "xlarge"; break;
				default : val = "unknown(" + val + ")";
				}
			} else if(name.endsWith("screenDensity")) {
				switch(val) {
				case "120" : val = "ldpi"; break;
				case "160" : val = "mdpi"; break;
				case "240" : val = "hdpi"; break;
				case "320" : val = "xhdpi"; break;
				case "480" : val = "xxhdpi"; break;
				case "640" : val = "xxxhdpi"; break;
				default : break;
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
