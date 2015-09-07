package com.apkscanner.core;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MyXPath {
	private Document document = null;
	private XPath xpath = null;
	private Object objNode = null;
	private QName QType = null;
	
	public MyXPath(MyXPath clone) {
		document = clone.document;
		xpath = clone.xpath;
		objNode = clone.objNode;
		QType = clone.QType;
	}
	
	public MyXPath(String xmlPath) {
		try {
			InputSource is = new InputSource(new FileReader(xmlPath));
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			xpath = XPathFactory.newInstance().newXPath();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			document = null;
			xpath = null;
		}
	}
	
	public MyXPath(InputStream xml) {
		try {
			//InputSource is = ;
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
			xpath = XPathFactory.newInstance().newXPath();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			document = null;
			xpath = null;
		}
	}

	public MyXPath(Node node) {
		if(node != null) {
			objNode = node;
			QType = XPathConstants.NODE;
		}
	}

	public MyXPath(NodeList node) {
		if(node != null) {
			objNode = node;
			QType = XPathConstants.NODESET;
		}
	}

	public Object evaluate(String expression, QName returnType) {
		if(xpath == null) return null;

    	//Log.i("evaluate() " + expression + ", " + document + ", " + returnType);
		try {
			objNode = xpath.evaluate(expression, document, returnType);
			QType = objNode != null ? returnType : null;
			return objNode;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			QType = null;
			objNode = null;
		}
		return objNode;
	}
	
	public boolean isNode(String expression) {
		return (getNode(expression).getNode() != null);
	}
	
	public Node getNode() {
		return (Node)(QType == XPathConstants.NODE ? objNode : null);
	}
	
	public MyXPath getNode(String expression) {
		evaluate(expression, XPathConstants.NODE);
		return this; 
	}
	
	public NodeList getNodeList() {
		return (NodeList)(QType == XPathConstants.NODESET ? objNode : null);
	}
	
	public MyXPath getNodeList(String expression) {
		evaluate(expression, XPathConstants.NODESET);
		return this;
	}
	
	public String getAttributes(String name) {
		if(getNode() == null || getNode().getAttributes() == null) {
			return null;
		}
		if(getNode().getAttributes().getNamedItem(name) == null) {
			if(name.indexOf(":") == -1) return null;
			String shortName = name.substring(name.indexOf(":"));
			//Log.i("getAttributes() shortName " + shortName);
			for(int i=0; i < getNode().getAttributes().getLength(); i++) {
				//Log.i("getAttributes() " + i + " : " + getNode().getAttributes().item(i).getNodeName());
				if(getNode().getAttributes().item(i).getNodeName().matches(".*"+shortName)) {
					//Log.i("getAttributes() maybe...... ");
					return getNode().getAttributes().item(i).getTextContent();
				}
			}
			return null;
		}
		
		return getNode().getAttributes().getNamedItem(name).getTextContent();
	}
	
	public String getAttributes(int idx, String name) {
		if(getNodeList() == null || getNodeList().item(idx) == null 
			|| getNodeList().item(idx).getAttributes() == null) {
			return null;
		}
		if(getNodeList().item(idx).getAttributes().getNamedItem(name) == null) {
			if(name.indexOf(":") == -1) return null;
			String shortName = name.substring(name.indexOf(":"));
			//Log.i("getAttributes() shortName " + shortName);
			for(int i=0; i < getNodeList().item(idx).getAttributes().getLength(); i++) {
				//Log.i("getAttributes() " + i + " : " + getNodeList().item(idx).getAttributes().item(i).getNodeName());
				if(getNodeList().item(idx).getAttributes().item(i).getNodeName().matches(".*"+shortName)) {
					//Log.i("getAttributes() maybe...... ");
					return getNodeList().item(idx).getAttributes().item(i).getTextContent();
				}
			}
			return null;
		}

		return getNodeList().item(idx).getAttributes().getNamedItem(name).getTextContent();
	}

	public String getTextContent() {
		if(getNode() == null) return null;
		return getNode().getTextContent();
	}

	public String getTextContent(int idx) {
		if(getNodeList() == null || getNodeList().item(idx) == null) return null;
		return getNodeList().item(idx).getTextContent();
	}
	
	public int getLength() {
		return (QType == XPathConstants.NODESET ? ((NodeList)objNode).getLength() : 0);
	}
	
	public MyXPath getParentNode() {
		Node parent = null;
		if(getNode() != null)
			parent = getNode().getParentNode();
		return new MyXPath(parent);
	}
	
	public MyXPath getParentNode(int idx) {
		Node parent = null;
		if(getNodeList() != null && getNodeList().item(idx) != null) 
			parent = getNodeList().item(idx).getParentNode();
		return new MyXPath(parent);
	}
	
	public MyXPath getChildNodes() {
		NodeList child = null;
		if(getNode() != null)
			child = getNode().getChildNodes();
		return new MyXPath(child);
	}
	
	public MyXPath getChildNodes(int idx) {
		NodeList child = null;
		if(getNodeList() != null && getNodeList().item(idx) != null) 
			child = getNodeList().item(idx).getChildNodes();
		return new MyXPath(child);
	}
}
