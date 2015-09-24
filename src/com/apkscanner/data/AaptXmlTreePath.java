package com.apkscanner.data;

import java.util.ArrayList;
import java.util.Stack;

import com.apkscanner.util.Log;

public class AaptXmlTreePath
{
	private AaptXmlTreeNode topNode = null;
	private AaptXmlTreeNode curNode = null;

	public AaptXmlTreePath()
	{
		
	}

	public AaptXmlTreePath(String[] xmlTree)
	{
		createAaptXmlTree(xmlTree);
	}
	
	public void createAaptXmlTree(String[] xmlTree)
	{
		topNode = new AaptXmlTreeNode();
		curNode = topNode;
		
		AaptXmlTreeNode curNode = topNode;
		Stack<AaptXmlTreeNode> nodeStack = new Stack<AaptXmlTreeNode>();
		nodeStack.push(topNode);
		int curDepth = 0;
		
		for(String s: xmlTree) {
			int depth = s.indexOf("E:");
			int type = -1;
			if(depth == -1 || !s.matches("^\\s*E:.*")) {
				depth = s.indexOf("A:");
				if(depth == -1 || !s.matches("^\\s*A:.*")) {
					if(s.startsWith("N:")) {
						Log.i("Namespace " + s);
					} else {
						Log.w("Unknown tag : " + s.trim());
					}
				} else {
					type = 1;
				}
			} else {
				type = 0;
			}
			
			if(type == 0) {
				while(depth < curDepth) {
					curNode = nodeStack.pop();
					curDepth -= 2;
				}
				String nodeName = s.replaceAll("^\\s*E: ([^\\s*]*) .*", "$1");

				AaptXmlTreeNode newNode = new AaptXmlTreeNode(curNode, nodeName);
				//Log.v("addChild node " + nodeName + ", " + curDepth);
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
	
	public AaptXmlTreeNode getNode(String expression)
	{
		ArrayList<AaptXmlTreeNode> nodes = new ArrayList<AaptXmlTreeNode>();
		
		if(expression.startsWith("//")) {
			expression = "." + expression;
		} else if(expression.startsWith("/")) {
			expression = "." + expression;
			curNode = topNode;
		} else if(expression.matches("^[^@\\.].*")) {
			expression = ".//" + expression;
			curNode = topNode;			
		}

		if(curNode == null)
			return null;
		
		for(AaptXmlTreeNode node: curNode.getNodeList()) {
			for(String item: expression.replaceAll("//", "/#").split("/")) {
				if(item.equals(".")) {
					continue;
				} else if(item.equals("..")) {
					node = node.getParent();
				} else if(item.equals("@")) {
					if(curNode.getAttribute(item) == null) {
						node = null;
						break;
					}
				} else if(item.equals("#")) {
					
				}
			}
			if(node != null) {
				Log.i(node.getPath() + ", " + node.getName());
				nodes.add(node);
			}
		}

		return null;
	}
	/*
	private AaptXmlTreeNode getNodeByAbsPath(String path)
	{
		AaptXmlTreeNode node = topNode;
		for(String s: path.split("/")) {
			if(s.isEmpty()) continue;
			node = node.getNode(s);
			if(node == null) break;
		}
		return node;
	}
	*/
}
