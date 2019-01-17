package com.apkscanner.tool.aapt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

import android.util.TypedValue;

public class AaptXmlTreePath
{
	private static final int NODE_TYPE_UNKNOWN = -1;
	private static final int NODE_TYPE_NAMESPACE = 0;
	private static final int NODE_TYPE_ELEMENT = 1;
	private static final int NODE_TYPE_ATTRIBUTE = 2;

	private static final int DEPTH_SPACE = 2;

	private AaptXmlTreeNode topNode = null;
	private AaptXmlTreeNode[] curNodes = null;
	private String defaultNamespace = null;

	private XmlPath attrIdPath = null;

	public AaptXmlTreePath()
	{

	}

	public AaptXmlTreePath(String[] xmlTree)
	{
		createAaptXmlTree(xmlTree);
	}

	public void createAaptXmlTree(String[] xmlTree)
	{
		Stack<String> namespaces = new Stack<String>();
		Stack<AaptXmlTreeNode> nodeStack = new Stack<AaptXmlTreeNode>();

		synchronized(this) {
			topNode = new AaptXmlTreeNode(null, "top");
			curNodes = new AaptXmlTreeNode[] { topNode };

			AaptXmlTreeNode curNode = topNode;
			nodeStack.push(topNode);
			int curDepth = 0;

			defaultNamespace = "http://schemas.android.com/apk/res/android";

			if(xmlTree == null) {
				Log.w("xmlTree is null");
				return;
			}

			for(String s: xmlTree) {
				int type = NODE_TYPE_UNKNOWN;
				int depth = s.indexOf("A:");
				if(depth > -1 && s.matches("^\\s*A:.*")) {
					type = NODE_TYPE_ATTRIBUTE;
							//Log.d("Unknown tag : " + s.trim());
				} else {
					depth = s.indexOf("E:");
					if(depth > -1 && s.matches("^\\s*E:.*")) {
						type = NODE_TYPE_ELEMENT;
					} else {
						depth = s.indexOf("N:");
						if(depth > -1 && s.matches("^\\s*N:.*")) {
							type = NODE_TYPE_NAMESPACE;
						}
					}
				}

				switch(type) {
				case NODE_TYPE_ATTRIBUTE:
					if(!s.matches("^\\s*A: ([^\\(]*).*")) {
						Log.w("Unknown attribute : " + s);
						continue;
					}
					String attrName = s.replaceAll("^\\s*A: ([^\\(=]*).*", "$1");
					String attrId = s.replaceAll("^\\s*A: ([^: ]*)?:([^:= ]*)?\\(([^\\(=]*)\\).*", "$3");
					String attrRealName = null;
					if(s.endsWith(attrId) || (attrName.startsWith(defaultNamespace+":") && !attrName.equals(":"))) {
						attrRealName = attrName;
						//Log.v(">>>>>>> attrName " + attrRealName);
					} else if(attrId.matches("^0x0?1[0-9a-f]{6}")) {
						attrRealName = defaultNamespace+":"+getAttrName(attrId);
						//Log.v("<<<<<<< attrRealName " + attrId + ", " + attrRealName);
					} else {
						//Log.v("<<<<<<< attrRealName " + attrId + ", " + attrRealName);
						attrRealName = attrName;
					}
					if(attrName.equals(":")) {
						attrName = attrRealName;
					}
					String attrData = s.substring(s.indexOf("=")+1);

					if(attrData.indexOf("(Raw:") > -1) {
						attrData = attrData.replaceAll(".*\\(Raw: \"(.*)\".*", "$1");
						//Log.v("attrData raw : " + attrData);
					} else if(attrData.startsWith("(type")) {
						int t = (int)Long.parseLong(attrData.replaceAll("^\\(type 0x(.*)\\).*", "$1"), 16);
						int d = (int)Long.parseLong(attrData.replaceAll("^\\(type .*\\)0x(.*)", "$1"), 16);
						attrData = TypedValue.coerceToString(t, d);
						//attrData = attrData.substring(attrData.indexOf(")")+1);
					} else if(attrData.startsWith("\"")) {

					} else if(attrData.startsWith("@")) {

					}
					//Log.v("attribute name : " + attrName + " = " + attrData);
					curNode.addAttribute(attrRealName, attrData);
					break;
				case NODE_TYPE_ELEMENT:
					while(depth < curDepth) {
						//Log.v("depth " + depth + ", curDepth " + curDepth + ", nodeStack " +  nodeStack.size() + ", curNode.getNamespaceCount() " + curNode.getNamespaceCount());
						curDepth -= (curNode.getNamespaceCount() + 1) * DEPTH_SPACE;
						curNode = nodeStack.pop();
					}
					String nodeName = s.replaceAll("^\\s*E: ([^\\s*]*) .*", "$1");

					AaptXmlTreeNode newNode = new AaptXmlTreeNode(curNode, nodeName);
					//Log.d("addChild node " + nodeName + ", " + curDepth + ", path " + newNode.getPath());
					curNode.addNode(nodeName, newNode);
					nodeStack.push(curNode);
					curNode = newNode;
					curDepth += DEPTH_SPACE;
					//curNode.addChild("", "");

					while(!namespaces.empty()) {
						String n = namespaces.pop();
						String[] space = n.split("=");
						String ref = space.length >= 2 ? space[1] : "";
						curNode.addNameSpace(space[0], ref);
					}
					break;
				case NODE_TYPE_NAMESPACE:
					while(depth < curDepth) {
						//Log.v("depth " + depth + ", curDepth " + curDepth + ", nodeStack " +  nodeStack.size() + ", curNode.getNamespaceCount() " + curNode.getNamespaceCount());
						curDepth -= (curNode.getNamespaceCount() + 1) * DEPTH_SPACE;
						curNode = nodeStack.pop();
					}
					curDepth += DEPTH_SPACE;

					String tag = s.replaceAll("^\\s*N: (.*)=.*", "$1");
					String space = s.replaceAll("^\\s*N: .*=(.*)", "$1");
					if("http://schemas.android.com/apk/res/android".equals(space)) {
						defaultNamespace = tag;
					}
					namespaces.push("xmlns:" + tag + "=" + space);
					//Log.i("namespace : " + s);
					break;
				default:
					Log.v("Unknown tag : " + s.trim());
					break;
				}
			}
			//Log.v(xmlTree[0] + ", curDepth " + curDepth);
		}
	}

	public String getAndroidNamespaceTag()
	{
		return defaultNamespace;
	}

	public AaptXmlTreeNode getNode(String expression)
	{
		synchronized(this) {
			AaptXmlTreeNode[] nodeList = getNodeList(expression);
			if(nodeList == null || nodeList.length == 0)
				return null;
			return nodeList[0];
		}
	}

	public AaptXmlTreeNode[] getNodeList(String expression)
	{
		synchronized(this) {
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
			if((node.getName().equals(item) || "*".equals(item)) && checkAttrCond(node, attrCond)) {
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
			try(InputStream xml = Resource.class.getResourceAsStream("/values/public.xml")) {
				if(xml != null) attrIdPath = new XmlPath(xml);
			} catch (IOException e) { }
		}
		String name = attrIdPath.getNode("/resources/public[@id='" + id + "']").getAttribute("name");
		if(name == null) {
			name = id;
		}
		return name;
	}
}
