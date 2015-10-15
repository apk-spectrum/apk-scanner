package com.apkscanner.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.MyXPath;

public class AaptXmlTreePath
{
	private AaptXmlTreeNode topNode = null;
	private AaptXmlTreeNode[] curNodes = null;
	private String namespace = null;
	
	private MyXPath attrIdPath = null;

	public AaptXmlTreePath()
	{
		
	}

	public AaptXmlTreePath(String[] xmlTree)
	{
		createAaptXmlTree(xmlTree);
	}
	
	public void createAaptXmlTree(String[] xmlTree)
	{
		topNode = new AaptXmlTreeNode(null, "top");
		curNodes = new AaptXmlTreeNode[] { topNode };
		
		AaptXmlTreeNode curNode = topNode;
		Stack<AaptXmlTreeNode> nodeStack = new Stack<AaptXmlTreeNode>();
		nodeStack.push(topNode);
		int curDepth = -1;

		namespace = "android";
		
		for(String s: xmlTree) {
			int depth = s.indexOf("E:");
			int type = -1;
			if(depth == -1 || !s.matches("^\\s*E:.*")) {
				depth = s.indexOf("A:");
				if(depth == -1 || !s.matches("^\\s*A:.*")) {
					if(s.startsWith("N:")) {
						namespace = xmlTree[0].replaceAll("N: (.*)=http://schemas.android.com/apk/res/android", "$1");
						Log.i("namespace : " + namespace);
					} else {
						Log.w("Unknown tag : " + s.trim());
					}
				} else {
					type = 1;
				}
			} else {
				type = 0;
				if(curDepth == -1) {
					curDepth = depth - 2;
				}
			}
			
			if(type == 0) {
				while(depth <= curDepth) {
					curNode = nodeStack.pop();
					curDepth -= 2;
				}
				String nodeName = s.replaceAll("^\\s*E: ([^\\s*]*) .*", "$1");

				AaptXmlTreeNode newNode = new AaptXmlTreeNode(curNode, nodeName);
				//Log.d("addChild node " + nodeName + ", " + curDepth + ", path " + newNode.getPath());
				curNode.addNode(nodeName, newNode);
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
				if(attrName.equals(":")) {
					String attrId = s.replaceAll("^\\s*A: :\\(([^\\(=]*)\\).*", "$1");
					attrName = getAttrName(attrId);
				}
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
				curNode.addAttribute(attrName, attrData);
			}
		}
		Log.v(xmlTree[0] + ", curDepth " + curDepth);
	}
	
	public String getNamespace()
	{
		return namespace;
	}
	
	public AaptXmlTreeNode getNode(String expression)
	{
		AaptXmlTreeNode[] nodeList = getNodeList(expression);
		if(nodeList == null || nodeList.length == 0)
			return null;
		return nodeList[0];
	}
	
	public AaptXmlTreeNode[] getNodeList(String expression)
	{
		if(expression.startsWith("//")) {
			expression = "." + expression;
		} else if(expression.startsWith("/")) {
			expression = "." + expression;
			curNodes = new AaptXmlTreeNode[] { topNode };
		} else if(expression.matches("^[^@\\.].*")) {
			expression = ".//" + expression;
			curNodes = new AaptXmlTreeNode[] { topNode };			
		}
		expression = expression.replaceAll("//", "/#");
		//Log.d("getNodeList() " + expression + ", curNodes " + curNodes);

		if(curNodes == null)
			return null;
		
		AaptXmlTreeNode[] curNodeList = curNodes;
		
		for(String item: expression.split("/")) {
			String attrCond = null;
			if(item.matches(".*\\[.*\\]")) {
				attrCond = item.replaceAll(".*\\[(.*)\\]", "$1");
				item = item.replaceAll("(.*)\\[.*", "$1");
			}
			//Log.d("getNodeList() item : " + item + ", attrCond : " + attrCond);
			if(item.equals(".")) {
				//continue;
			} else if(item.equals("..")) {
				curNodeList = getParents(curNodeList, attrCond);
			} else if(item.startsWith("@")) {
				curNodeList = getHasAttrNode(curNodeList, item.substring(1));
			} else if(item.startsWith("#")) {
				curNodeList = suchNode(curNodeList, item.substring(1), attrCond);
			} else {
				curNodeList = getChilds(curNodeList, item, attrCond);
			}
		}
		
		curNodes = curNodeList;

		return curNodeList;
	}
	
	private AaptXmlTreeNode[] getChilds(AaptXmlTreeNode[] curList, String item, String attrCond)
	{
		ArrayList<AaptXmlTreeNode> nodes = new ArrayList<AaptXmlTreeNode>();
		for(AaptXmlTreeNode node: curList) {
			//Log.d("getChilds() node : " + node.getName() + ", " + node.getNodeCount());
			for(AaptXmlTreeNode child: node.getNodeList()) {
				//Log.d("getChilds() child : " + child.getName());
				if("*".equals(item) 
						|| (child.getName().equals(item) && checkAttrCond(child, attrCond))) {
					nodes.add(child);
				}
			}
		}
		return nodes.toArray(new AaptXmlTreeNode[0]);
	}
	
	private AaptXmlTreeNode[] getParents(AaptXmlTreeNode[] curList, String attrCond)
	{
		ArrayList<AaptXmlTreeNode> nodes = new ArrayList<AaptXmlTreeNode>();
		for(AaptXmlTreeNode node: curList) {
			AaptXmlTreeNode p = node.getParent();
			if(p != null && checkAttrCond(p, attrCond)) {
				nodes.add(p);
			}
		}
		return nodes.toArray(new AaptXmlTreeNode[0]);
	}
	
	private AaptXmlTreeNode[] getHasAttrNode(AaptXmlTreeNode[] curList, String attr)
	{
		ArrayList<AaptXmlTreeNode> nodes = new ArrayList<AaptXmlTreeNode>();
		for(AaptXmlTreeNode node: curList) {
			if(node.getAttribute(attr) != null) {
				nodes.add(node);
			}
		}
		return nodes.toArray(new AaptXmlTreeNode[0]);
	}
	
	private AaptXmlTreeNode[] suchNode(AaptXmlTreeNode[] curList, String item, String attrCond)
	{
		ArrayList<AaptXmlTreeNode> nodes = new ArrayList<AaptXmlTreeNode>();
		for(AaptXmlTreeNode node: curList) {
			if(node.getName().equals(item) && checkAttrCond(node, attrCond)) {
				nodes.add(node);
			}
			Collections.addAll(nodes, suchNode(node.getNodeList(), item, attrCond));
		}
		return nodes.toArray(new AaptXmlTreeNode[0]);
	}
	
	private boolean checkAttrCond(AaptXmlTreeNode node, String attrCond)
	{
		if(attrCond == null || attrCond.trim().isEmpty() || attrCond.indexOf("=") < 0)
			return true;
		
		String cond[] = attrCond.split("=");
		String attr = cond[0].trim().substring(1);
		String val = cond[1].trim().replaceAll("^['\"](.*)['\"]$", "$1");
		
		if(val.equals(node.getAttribute(attr)))
			return true;
		
		return false;
	}
	
	private String getAttrName(String id) 
	{
		if(attrIdPath == null) {
			InputStream xml = Resource.class.getResourceAsStream("/values/public.xml");
			attrIdPath = new MyXPath(xml);
		}
		String name = attrIdPath.getNode("/resources/public[@id='" + id + "']").getAttributes("name");
		if(name == null) {
			name = id;
		}
		return name;
	}
}
