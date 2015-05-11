package com.ApkInfo.Core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

import com.ApkInfo.UI.MyProgressBarDemo;

public class CoreXmlTool {
	
	static MyProgressBarDemo progressBarDemo;
	
	public static MyApkInfo XmlToMyApkinfo(String APkworkPath) throws XPathExpressionException {
		
		MyApkInfo temp = new MyApkInfo();
		
		InputSource is = null;
		Document document = null;
		try {
			is = new InputSource(new FileReader(APkworkPath+"/AndroidManifest.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// xpath 생성
		  XPath  xpath = XPathFactory.newInstance().newXPath();

		  
        // NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
        Node cols = (Node)xpath.evaluate("//manifest", document, XPathConstants.NODE);
        temp.strPackageName = cols.getAttributes().getNamedItem("package").getTextContent();
        
        cols = (Node)xpath.evaluate("//manifest/application", document, XPathConstants.NODE);

        temp.strLabelname = cols.getAttributes().getNamedItem("android:label").getTextContent();
        temp.strLabelname = getResourceInfo(APkworkPath,temp.strLabelname);

        temp.strIconPath = cols.getAttributes().getNamedItem("android:icon").getTextContent();
        temp.strIconPath = getResourceInfo(APkworkPath,temp.strIconPath);

        System.out.println("Package = " + temp.strPackageName);
        System.out.println("Label = " + temp.strLabelname);
        System.out.println("Icon = " + temp.strIconPath);

		return temp;
	}
	
	public static String getResourceInfo(String APkworkPath, String id) throws XPathExpressionException {
		
		String result = null;
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);
		
		if(!id.matches("^@.*")) {
			System.out.println("id is start without @");
			result = new String(id);
			return result;
		} else if(id.matches("^@drawable/.*")) {
			System.out.println("@drawable");
			String imgName = new String(id.substring(10));
			long maxImgSize = 0;
			System.out.println(id + "->" +imgName);

			for (String s : (new File(resXmlPath)).list()) {
				//System.out.println("dir " + s);
				if(!s.matches("^drawable.*")) continue;

				File imgFile = new File(resXmlPath + s + File.separator + imgName + ".png");
				if(!imgFile.exists()) continue;

				if(imgFile.length() > maxImgSize) {
					System.out.println(imgFile.getPath() + ", " + maxImgSize);
					result = new String(imgFile.getPath());
					maxImgSize = imgFile.length();
				}
			}
			return result;
		} else if(id.matches("^@string/.*")) {
			System.out.println("@stirng");
			resXmlPath += "values" + File.separator + "strings.xml";
		} else if(id.matches("^@xml/.*")) {
			System.out.println("@xml");
			resXmlPath += id.substring(1);
		} else {
			System.out.println("Unknown id " + id);
			return result;
		}

		InputSource is = null;
		Document document = null;
		try {
			is = new InputSource(new FileReader(resXmlPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// xpath 생성
		XPath  xpath = XPathFactory.newInstance().newXPath();
		
		String idName = new String(id.substring(id.indexOf("/")+1));
		  
        // NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
        Node cols = (Node)xpath.evaluate("//resources/string[@name='"+idName+"']", document, XPathConstants.NODE);
        result = new String(cols.getTextContent());
    	System.out.println("string " + cols.getTextContent());

		return result;
	}

	public static void setProgressBarDlg(MyProgressBarDemo progressBarDlg) {
		// TODO Auto-generated method stub
		progressBarDemo = progressBarDlg;
	}	
	
}
