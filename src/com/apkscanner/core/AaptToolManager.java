package com.apkscanner.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import com.apkscanner.data.ApkInfo;
import com.apkscanner.util.Log;

public class AaptToolManager
{
	private ApkInfo mApkInfo = null;
	private Node manifestNode = null;
	
	
	/*
	public AaptToolManager()
	{
		this(null, false);
	}

	public AaptToolManager(String apkPath)
	{
		this(apkPath, false);
	}

	public AaptToolManager(String apkPath, boolean isPackage)
	{
		mApkInfo = getApkInfo(apkPath);
		isPackageTempAPK = isPackage;
	}
	*/
	
	private class Node {
		private String name;
		private HashMap<String, String> attr = new HashMap<String, String>();
		private HashMap<String, Node> child = new HashMap<String, Node>();
		
		public Node(String name)
		{
			this.name = name;
		}
		
		public void addChild(String name, Node node)
		{
			child.put(name, node);
		}
		
		public void addAttr(String name, String data)
		{
			attr.put(name, data);
		}
		
		public String getName()
		{
			return name;
		}
		
		public Node getChildNode(String name)
		{
			return child.get(name);
		}
		
		public String getAttr(String name)
		{
			return attr.get(name);
		}
	}
	
	public Node createNode(String[] xmlTree)
	{
		Node topNode = new Node("root");
		Node curNode = topNode;
		Stack<Node> nodeStack = new Stack<Node>();
		nodeStack.push(topNode);
		int curDepth = 0;
		
		for(String s: xmlTree) {
			int depth = s.indexOf("E:");
			int type = -1;
			if(depth == -1 || !s.matches("^\\s*E:.*")) {
				depth = s.indexOf("A:");
				if(depth == -1 || !s.matches("^\\s*A:.*")) {
					Log.w("Unknown tag : " + s);
				} else {
					type = 1;
				}
			} else {
				type = 0;
			}
			
			if(type == 0) {
				String nodeName = s.replaceAll("^\\s*E: ([^\\s*]*) .*", "$1");
				Node newNode = new Node(nodeName);

				while(depth < curDepth) {
					curNode = nodeStack.pop();
					curDepth -= 2;
				}
				//Log.v("addChild node " + nodeName + ", " + curDepth);
				curNode.addChild(nodeName, newNode);
				nodeStack.push(curNode);
				curNode = newNode;
				curDepth += 2;
				//curNode.addChild("", "");
			} else if(type == 1) {
				if(!s.matches("^\\s*A: ([^\\(]*).*")) {
					Log.w("Unknown attribute : " + s);
					continue;
				} 
				String attrName = s.replaceAll("^\\s*A: ([^\\(=]*).*", "$1");
				String attrData = s.substring(s.indexOf("=")+1);
				
				if(attrData.indexOf("(Raw:") > -1) {
					attrData = attrData.replaceAll(".*\\(Raw: \"(.*)\".*", "$1");
					//Log.v("attrData raw : " + attrData);
				} else if(attrData.startsWith("(type")) {
					if(attrData.startsWith("(type 0x12")) {
						if(attrData.endsWith("0x0")) {
							attrData = "false"; // 0x0
						} else {
							attrData = "true"; // 0xffffffff
						}
					} else {
						attrData = attrData.substring(attrData.indexOf(")")+1);
					}
				} else if(attrData.startsWith("\"")) {
					
				} else if(attrData.startsWith("@")) {
					
				}
				//Log.v("attribute name : " + attrName + " = " + attrData);
				curNode.addAttr(attrName, attrData);
			}
		}
		Log.v(xmlTree[0] + ", curDepth " + curDepth);
		
		return topNode;
	}
	

	public ApkInfo getApkInfo(String apkFilePath) {
		ApkInfo apkInfo = new ApkInfo();
		
		File apkFile = new File(apkFilePath);

		if(!apkFile.exists()) {
			Log.e("No Such APK file");
			return null;
		}
		apkFilePath = apkFile.getAbsolutePath();
		
		if(apkFilePath != null && (new File(apkFilePath)).exists()) {
			apkInfo.ApkPath = apkFilePath;
		}
		
		Log.v("getApkInfo()");
		String androidManifest[] = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {"AndroidManifest.xml"});

		if(!"N: android=http://schemas.android.com/apk/res/android".equals(androidManifest[0])) {
			Log.w("Schemas was not http://schemas.android.com/apk/res/android\n" + androidManifest[0]);
		}
		manifestNode = createNode(androidManifest);
		
		//Log.v(androidManifest[0]);
		
		
		Log.i(manifestNode.getChildNode("manifest").getAttr("package"));
		Log.i(manifestNode.getChildNode("manifest").getAttr("android:versionCode"));
		Log.i(manifestNode.getChildNode("manifest").getAttr("android:versionName"));
		
		return apkInfo;
	}
}
