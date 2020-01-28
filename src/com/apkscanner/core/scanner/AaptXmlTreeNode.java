package com.apkscanner.core.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.apkspectrum.util.Log;

public class AaptXmlTreeNode
{
	private String name;
	private String path;
	private AaptXmlTreeNode parent;
	private LinkedHashMap<String, String> attribute = new LinkedHashMap<String, String>();
	private HashMap<String, ArrayList<AaptXmlTreeNode>> childMap = new HashMap<String, ArrayList<AaptXmlTreeNode>>();
	private ArrayList<AaptXmlTreeNode> childList = new ArrayList<AaptXmlTreeNode>();
	private int namespaceCount = 0;

	public AaptXmlTreeNode()
	{
		this(null);
	}

	public AaptXmlTreeNode(AaptXmlTreeNode parent)
	{
		this(parent, null);
	}

	public AaptXmlTreeNode(AaptXmlTreeNode parent, String name)
	{
		String path = null;
		if(parent != null) {
			path = parent.getPath() + name + "/";
		} else {
			path = "/";
		}
		this.parent = parent;
		this.name = name;
		this.path = path;
	}

	public void addNode(String name, AaptXmlTreeNode node)
	{
		childList.add(node);

		ArrayList<AaptXmlTreeNode> nodes = null;
		if(childMap.containsKey(name)) {
			nodes = childMap.get(name);
		} else {
			nodes = new ArrayList<AaptXmlTreeNode>();
			childMap.put(name, nodes);
		}
		nodes.add(node);
	}

	public void addNameSpace(String name, String data)
	{
		namespaceCount++;
		attribute.put(name, data);
	}

	public void addAttribute(String name, String data)
	{
		attribute.put(name, data);
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public AaptXmlTreeNode getParent()
	{
		return parent;
	}

	public int getNodeCount()
	{
		return childList.size();
	}

	public int getNodeCount(String name)
	{
		if(!childMap.containsKey(name))
			return -1;

		return getNodeList(name).length;
	}

	public int getAttrCount()
	{
		return attribute.size();
	}

	public AaptXmlTreeNode getNode()
	{
		if(getNodeCount() <= 0)
			return null;

		return getNodeList()[0];
	}

	public AaptXmlTreeNode getNode(String name)
	{
		if(!childMap.containsKey(name))
			return null;

		return getNodeList(name)[0];
	}

	public AaptXmlTreeNode getNode(int idx)
	{
		if(idx < 0 || idx >= getNodeCount())
			return null;

		return getNodeList()[idx];
	}

	public AaptXmlTreeNode getNextSibling()
	{
		if(getParent() == null)
			return null;
		int idx = getParent().childList.indexOf(this);
		if(++idx >= getParent().getNodeCount())
			return null;

		return getParent().getNode(idx);
	}

	public AaptXmlTreeNode[] getNodeList()
	{
		return childList.toArray(new AaptXmlTreeNode[0]);
	}

	public AaptXmlTreeNode[] getNodeList(String name)
	{
		if(!childMap.containsKey(name))
			return null;

		return childMap.get(name).toArray(new AaptXmlTreeNode[0]);
	}

	public String getAttribute(String name)
	{
		return attribute.get(name);
	}

	public String[] getAttributeList()
	{
		return attribute.keySet().toArray(new String[0]);
	}

	public int getNamespaceCount()
	{
		return namespaceCount;
	}

	public void dump()
	{
		Log.i("Node : " + name);
		Log.i("Path : " + path);
		Log.i("Child cnt : " + childList.size());
		Log.i("Attr cnt : " + attribute.size());
	}
}
