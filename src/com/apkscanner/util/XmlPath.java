package com.apkscanner.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

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

public class XmlPath {
	private Document document = null;
	private XPath xpath = null;
	private Object objNode = null;
	private QName QType = null;

	public XmlPath(XmlPath clone) {
		document = clone.document;
		xpath = clone.xpath;
		objNode = clone.objNode;
		QType = clone.QType;
	}

	public XmlPath(File xmlFile) {
		try {
			InputSource is = new InputSource(new FileReader(xmlFile));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(false);
			document = factory.newDocumentBuilder().parse(is);
			xpath = XPathFactory.newInstance().newXPath();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			document = null;
			xpath = null;
		}
	}

	public XmlPath(InputStream xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(false);
			document = factory.newDocumentBuilder().parse(xml);
			xpath = XPathFactory.newInstance().newXPath();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			document = null;
			xpath = null;
		}
	}

	public XmlPath(String xmlContent) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(false);
			document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
			xpath = XPathFactory.newInstance().newXPath();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			document = null;
			xpath = null;
		}
	}

	public XmlPath(Node node) {
		if(node != null) {
			objNode = node;
			QType = XPathConstants.NODE;
		}
	}

	public XmlPath(NodeList node) {
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

	public XmlPath getNode(String expression) {
		evaluate(expression, XPathConstants.NODE);
		return this; 
	}

	public NodeList getNodeList() {
		return (NodeList)(QType == XPathConstants.NODESET ? objNode : null);
	}

	public XmlPath getNodeList(String expression) {
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
				if(getNode().getAttributes().item(i).getNodeName().endsWith(shortName)) {
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
				if(getNodeList().item(idx).getAttributes().item(i).getNodeName().endsWith(shortName)) {
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

	public String getComment() {
		if(getNode() == null) return null;

		Node preNode = getNode().getPreviousSibling();
		if(preNode.getNodeType() == Node.TEXT_NODE) {
			preNode = preNode.getPreviousSibling();
		}
		if(preNode.getNodeType() != Node.COMMENT_NODE) {
			return null;
		}
		return preNode.getTextContent();
	}

	public int getLength() {
		return (QType == XPathConstants.NODESET ? ((NodeList)objNode).getLength() : 0);
	}

	public XmlPath getParentNode() {
		Node parent = null;
		if(getNode() != null)
			parent = getNode().getParentNode();
		return new XmlPath(parent);
	}

	public XmlPath getParentNode(int idx) {
		Node parent = null;
		if(getNodeList() != null && getNodeList().item(idx) != null) 
			parent = getNodeList().item(idx).getParentNode();
		return new XmlPath(parent);
	}

	public XmlPath getChildNodes() {
		NodeList child = null;
		if(getNode() != null)
			child = getNode().getChildNodes();
		return new XmlPath(child);
	}

	public XmlPath getChildNodes(int idx) {
		NodeList child = null;
		if(getNodeList() != null && getNodeList().item(idx) != null) 
			child = getNodeList().item(idx).getChildNodes();
		return new XmlPath(child);
	}
}
