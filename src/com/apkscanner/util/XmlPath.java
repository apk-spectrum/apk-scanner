package com.apkscanner.util;

import java.io.File;
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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlPath {
	private static final XPathFactory xPathFactory;
	private static final DocumentBuilderFactory documentBuilderFactory;

	private XPath xPath = xPathFactory.newXPath();
	private XObject xObject;

	private Exception lastException = null;

	static {
		xPathFactory = XPathFactory.newInstance();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setIgnoringComments(false);
	}

	public class XObject {
		private Object object;
		private XObject(Node node) { object = node; }
		private XObject(NodeList nodeList) { object = nodeList; }
		public Object getObject() { return object; }
	}

	public class XNode extends XObject {
		public XNode(Node node) { super(node); }
	}

	public class XNodeList extends XObject {
		public XNodeList(NodeList nodeList) { super(nodeList); }
	}

	public XmlPath() {
		try {
			xObject = new XNode(documentBuilderFactory.newDocumentBuilder().newDocument());
		} catch (ParserConfigurationException e) {
			lastException = e;
			e.printStackTrace();
		}
	}

	public XmlPath(File xmlFile) {
		try {
			xObject = new XNode(documentBuilderFactory.newDocumentBuilder().parse(xmlFile));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			lastException = e;
			e.printStackTrace();
		}
	}

	public XmlPath(InputStream xml) {
		try {
			xObject = new XNode(documentBuilderFactory.newDocumentBuilder().parse(xml));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			lastException = e;
			e.printStackTrace();
		}
	}

	public XmlPath(String xmlContent) {
		try (StringReader sr = new StringReader(xmlContent)) {
			xObject = new XNode(documentBuilderFactory.newDocumentBuilder().parse(new InputSource(sr)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			lastException = e;
			e.printStackTrace();
		}
	}

	public XmlPath(Node node) {
		if(node == null) {
			lastException = new NullPointerException("starting node is null");
			return;
		}
		xObject = new XNode(node);
	}

	public XmlPath(NodeList nodeList) {
		if(nodeList == null) {
			lastException = new NullPointerException("starting node is null");
			return;
		}
		xObject = new XNodeList(nodeList);
	}

	public XmlPath(XmlPath clone) {
		xObject = clone.xObject;
	}

	protected Node getNode() {
		return xObject instanceof XNode ? (Node) xObject.getObject() : null;
	}

	protected NodeList getNodeList() {
		return xObject instanceof XNodeList ? (NodeList) xObject.getObject() : null;
	}

	public Object getData() {
		return xObject != null ? xObject.getObject() : null;
	}

	public QName getDataType() {
		return xObject instanceof XNode ? XPathConstants.NODE : XPathConstants.NODESET;
	}

	public Object evaluate(String expression, QName returnType) {
		//Log.v("evaluate() " + expression + ", " + baseNode + ", " + returnType);
		if(expression == null || expression.isEmpty()) return null;
		if(!(xObject instanceof XNode)) {
			Log.w("Unable to evaluate expression using this context");
			return null;
		}
		Object result = null;
		try {
			result = xPath.evaluate(expression, xObject.getObject(), returnType);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		//Log.v("evaluate() result " + result);
		return result;
	}

	public Node evaluateNode(String expression) {
		return (Node) evaluate(expression, XPathConstants.NODE);
	}

	public NodeList evaluateNodeList(String expression) {
		return (NodeList) evaluate(expression, XPathConstants.NODESET);
	}

	public boolean isNodeExisted(String expression) {
		return evaluateNode(expression) instanceof Node;
	}

	public int getCount() {
		NodeList nodeList = getNodeList();
		return nodeList != null ? nodeList.getLength() : (getNode() != null ? 1 : 0);
	}

	public int getCount(String expression) {
		NodeList list = evaluateNodeList(expression);
		return list != null ? list.getLength() : 0;
	}

	public XmlPath getNode(String expression) {
		Node node = evaluateNode(expression);
		return node != null ? new XmlPath(node) : null;
	}

	public XmlPath getNode(int index) {
		NodeList nodeList = null;
		if(index < 0 || (nodeList = getNodeList()) == null || nodeList.getLength() <= index) {
			return null;
		}
		return new XmlPath(nodeList.item(index));
	}

	public XmlPath getNodeList(String expression) {
		NodeList list = evaluateNodeList(expression);
		return list != null ? new XmlPath(list) : null;
	}

	public NamedNodeMap getAttributes() {
		Node node = getNode();
		return node != null ? node.getAttributes() : null;
	}

	public NamedNodeMap getAttributes(int index) {
		return getNode(index).getAttributes();
	}

	public String getAttribute(String name) {
		Node node = getNode();
		NamedNodeMap attrs = null;
		if(node == null || (attrs = node.getAttributes()) == null)  return null;
		if(attrs.getNamedItem(name) == null) {
			if(name.indexOf(":") == -1) return null;
			String shortName = name.substring(name.indexOf(":"));
			//Log.i("getAttributes() shortName " + shortName);
			for(int i=0; i < attrs.getLength(); i++) {
				//Log.i("getAttributes() " + i + " : " + getNode().getAttributes().item(i).getNodeName());
				if(attrs.item(i).getNodeName().endsWith(shortName)) {
					//Log.i("getAttributes() maybe...... ");
					return attrs.item(i).getTextContent();
				}
			}
			return null;
		}

		return attrs.getNamedItem(name).getTextContent();
	}

	public String getAttribute(int index, String name) {
		Node node = null;
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index
				|| (node = list.item(index)) == null || node.getAttributes() == null) {
			return null;
		}
		NamedNodeMap attrs = node.getAttributes();
		if(attrs.getNamedItem(name) == null) {
			if(name.indexOf(":") == -1) return null;
			String shortName = name.substring(name.indexOf(":"));
			//Log.i("getAttributes() shortName " + shortName);
			for(int i=0; i < attrs.getLength(); i++) {
				//Log.i("getAttributes() " + i + " : " + getNodeList().item(idx).getAttributes().item(i).getNodeName());
				if(attrs.item(i).getNodeName().endsWith(shortName)) {
					//Log.i("getAttributes() maybe...... ");
					return attrs.item(i).getTextContent();
				}
			}
			return null;
		}

		return attrs.getNamedItem(name).getTextContent();
	}

	public String getTextContent() {
		Node node = getNode();
		return node != null ? node.getTextContent() : null;
	}

	public String getTextContent(int index) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return null;
		return list.item(index).getTextContent();
	}

	public String getComment() {
		Node node = getNode();
		if(node == null) return null;

		Node preNode = node.getPreviousSibling();
		if(preNode.getNodeType() == Node.TEXT_NODE) {
			preNode = preNode.getPreviousSibling();
		}
		if(preNode.getNodeType() != Node.COMMENT_NODE) {
			return null;
		}
		return preNode.getTextContent();
	}

	public short getNodeType() {
		Node node = getNode();
		return node != null ? node.getNodeType() : -1;
	}

	public String getNodeName() {
		Node node = getNode();
		return node != null ? node.getNodeName() : null;
	}

	public XmlPath getParentNode() {
		Node node = getNode();
		return (node != null) ? new XmlPath(node.getParentNode()) : null;
	}

	public XmlPath getParentNode(int index) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return null;
		return new XmlPath(list.item(index).getParentNode());
	}

	public XmlPath getChildNodes() {
		Node node = getNode();
		NodeList list = (node != null) ? node.getChildNodes() : null;
		return list != null ? new XmlPath(list) : null;
	}

	public XmlPath getChildNodes(int index) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return null;
		return new XmlPath(list.item(index).getChildNodes());
	}

	public int getChildCount() {
		Node node = getNode();
		NodeList list = (node != null) ? node.getChildNodes() : null;
		return list != null ? list.getLength() : -1;
	}

	public Exception getLastException() {
		return lastException;
	}
}
