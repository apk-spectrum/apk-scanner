package com.apkscanner.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
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
		//Log.v("evaluate() " + expression + ", " + returnType);
		if(expression == null || expression.isEmpty()) return null;
		if(!(xObject instanceof XNode)) {
			Log.w("Unable to evaluate expression using this context");
			return null;
		}
		Object result = null;
		try {
			result = xPath.evaluate(expression, xObject.getObject(), returnType);
		} catch (XPathExpressionException e) {
			Log.e(e.getMessage());
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
		NodeList nodeList = null;
		if(index < 0 || (nodeList = getNodeList()) == null || nodeList.getLength() <= index) {
			return null;
		}
		return nodeList.item(index).getAttributes();
	}

	private String getAttribute(Node node, String name) {
		NamedNodeMap attrs = null;
		if(node == null || (attrs = node.getAttributes()) == null) return null;
		if(attrs.getNamedItem(name) == null) {
			if(name.indexOf(":") == -1) return null;
			String shortName = name.substring(name.indexOf(":"));
			//Log.i("getAttributes() shortName " + shortName);
			for(int i=0; i < attrs.getLength(); i++) {
				//Log.i("getAttributes() " + i + " : " + attrs.item(i).getNodeName());
				if(attrs.item(i).getNodeName().endsWith(shortName)) {
					//Log.i("getAttributes() maybe...... ");
					return attrs.item(i).getTextContent();
				}
			}
			return null;
		}
		return attrs.getNamedItem(name).getTextContent();
	}

	public String getAttribute(String name) {
		Node node = getNode();
		return getAttribute(node, name);
	}

	public String getAttribute(int index, String name) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) {
			return null;
		}
		return getAttribute(list.item(index), name);
	}

	private void setAttribute(Node node, String name, String value) throws XPathException {
		NamedNodeMap attrs = null;
		if(node == null || (attrs = node.getAttributes()) == null) return;
		if(attrs.getNamedItem(name) == null) {
			if(name.contains(":")) {
				String shortName = name.substring(name.indexOf(":"));
				//Log.i("getAttributes() shortName " + shortName);
				for(int i=0; i < attrs.getLength(); i++) {
					//Log.i("getAttributes() " + i + " : " + getNode().getAttributes().item(i).getNodeName());
					if(attrs.item(i).getNodeName().endsWith(shortName)) {
						//Log.i("getAttributes() maybe...... ");
						attrs.item(i).setTextContent(value);
						return;
					}
				}
			}
			Document document = node instanceof Document
					? (Document)node : node.getOwnerDocument();
			if(document == null) {
				throw new XPathException("OwnerDocument is null : " + node);
			}
			Node anode = document.createAttribute(name);
			anode.setNodeValue(value);
			node.getAttributes().setNamedItem(anode);
		} else {
			attrs.getNamedItem(name).setTextContent(value);
		}
	}

	public XmlPath setAttribute(String name, String value) throws XPathException {
		if(!(xObject instanceof XNode)) {
			throw new XPathException("Unable to evaluate expression using this context");
		}
		setAttribute(getNode(), name, value);
		return this;
	}

	public XmlPath setAttribute(int index, String name, String value) throws XPathException {
		XmlPath node = getNode(index);
		if(node == null) return null;
		setAttribute(node.getNode(), name, value);
		return node;
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

	public void setTextContent(String text) {
		Node node = getNode();
		if(node == null) return;
		node.setTextContent(text);
	}

	public void setTextContent(int index, String text) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return;
		list.item(index).setTextContent(text);
	}

	private Node getCommentNode(Node node) {
		if(node == null) return null;

		Node commentNode = node.getPreviousSibling();
		if(commentNode == null) return null;
		if(commentNode.getNodeType() == Node.TEXT_NODE) {
			commentNode = commentNode.getPreviousSibling();
			if(commentNode == null) return null;
		}
		if(commentNode.getNodeType() != Node.COMMENT_NODE) {
			return null;
		}
		return commentNode;
	}

	public String getComment() {
		Node node = getCommentNode(getNode());
		return node != null ? node.getTextContent() : null;
	}

	public String getComment(int index) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return null;
		Node node = getCommentNode(list.item(index));
		return node != null ? node.getTextContent() : null;
	}

	private void setComment(Node node, String text) {
		Node comment = getCommentNode(node);
		if(comment != null) {
			comment.setTextContent(text);
		} else {
			Document document = null;
			Node parent = null;
			if((document = node.getOwnerDocument()) == null
					|| (parent = node.getParentNode()) == null
					|| (comment = document.createComment(text)) == null) {
				return;
			}
			parent.insertBefore(comment, node);
		}
	}

	public void setComment(String text) {
		Node node = getNode();
		if(node == null) return;
		setComment(node, text);
	}

	public void setComment(int index, String text) {
		NodeList list = null;
		if(index < 0 || (list = getNodeList()) == null || list.getLength() <= index) return;
		setComment(list.item(index), text);
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

	public Document getDocument() {
		NodeList list = getNodeList();
		Node node = (list != null && list.getLength() > 0) ? list.item(0) : getNode();
		if(node == null) return null;
		return node instanceof Document ? (Document)node : node.getOwnerDocument();
	}

	public XmlPath createXPath(String expression) throws XPathException {
		if(!(xObject instanceof XNode)) {
			throw new XPathException("Unable to evaluate expression using this context");
		}
		expression = expression.trim();
		if(expression.startsWith("//")) {
			throw new XPathException("Unable to evaluate expression using this context");
		}

		Node baseNode = getNode();
		Document document = baseNode instanceof Document
				? (Document)baseNode : baseNode.getOwnerDocument();
		if(document == null) {
			throw new XPathException("OwnerDocument is null : " + baseNode);
		}

		XmlPath path = this;
		if(expression.startsWith("/")) {
			path = getNode("/");
			expression = expression.substring(1);
		}

		for (String part: expression.split("/")) {
			part = part.trim();
			int count = path.getCount(part);
			//Log.v(part + " tag count : " + count);
			if (count>1) throw new XPathExpressionException("Xpath '"+xPath+"' was not found multiple times!");
			else if (count==1) { path = path.getNode(part); continue; }

			if (part.startsWith("@")) {
				if (part.contains("=")) {
					String name, value;
					String[] tmp = part.substring(1).split("=", 2);
					name = tmp[0].trim(); value = tmp[1].trim();
					if (value.isEmpty() || !value.startsWith("'") || !value.endsWith("'")) {
						throw new XPathExpressionException("Unsupported XPath attrib: "+part);
					}
					value = value.substring(1, value.length()-1);
					Node anode = document.createAttribute(name);
					anode.setNodeValue(value);
					path.getAttributes().setNamedItem(anode);
				} else {
					Node anode = document.createAttribute(part.substring(1));
					path.getAttributes().setNamedItem(anode);
				}
			} else {
				String elName, attrs = null;
				if (part.contains("[")) {
					String[] tmp = part.split("\\[", 2);
					elName = tmp[0].trim(); attrs = tmp[1].trim();
					if (!attrs.endsWith("]")) throw new XPathExpressionException("Unsupported XPath (missing ]): "+part);
					attrs = attrs.substring(0, attrs.length()-1);
				} else elName = part;

				Node next = document.createElement(elName);
				path.getNode().appendChild(next);
				path = new XmlPath(next);

				if (attrs != null) {
					for(String attr: attrs.split(" and ")) {
						attr = attr.trim();
						if (!attr.startsWith("@")) throw new XPathExpressionException("Unsupported XPath attrib (missing @): "+part);
						String name, value;
						String[] tmp = attr.substring(1).split("=", 2);
						name = tmp[0].trim(); value = tmp[1].trim();
						if (value.isEmpty() || !value.startsWith("'") || !value.endsWith("'")) {
							throw new XPathExpressionException("Unsupported XPath attrib: "+part);
						}
						value=value.substring(1, value.length()-1);
						Node anode = document.createAttribute(name);
						anode.setNodeValue(value);
						path.getAttributes().setNamedItem(anode);
					}
				}
			}
		}
		return path;
	}

	public void transformXml(Result result) {
		if(result == null) {
			Log.e("Result is null");
			return;
		}
		if(!(xObject instanceof XNode)) {
			Log.e("Unable to evaluate expression using this context");
			return;
		}

		Node baseNode = getNode();
		Document document = baseNode instanceof Document
				? (Document)baseNode : baseNode.getOwnerDocument();
		if(document == null) {
			Log.e("OwnerDocument is null : " + baseNode);
			return;
		}

		try {
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			//tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			tr.transform(new DOMSource(document), result);
		} catch (TransformerException te) {
			Log.e(te.getMessage());
		}
	}

	public void saveXmlFile(File saveFile) {
		transformXml(new StreamResult(saveFile));
	}

	public void printXml(OutputStream out) {
		transformXml(new StreamResult(out));
	}

	public Exception getLastException() {
		return lastException;
	}
}