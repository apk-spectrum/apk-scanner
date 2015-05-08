package com.ApkInfo.Core;

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
        temp.strIconPath = cols.getAttributes().getNamedItem("android:icon").getTextContent();

        System.out.println("Package = " + temp.strPackageName);
        System.out.println("Label = " + temp.strLabelname);
        System.out.println("Icon = " + temp.strIconPath);
        System.out.println("Icon = " + cols.getAttributes().getNamedItem("android:icon").getTextContent());
        

		return temp;
	}

	public static void setProgressBarDlg(MyProgressBarDemo progressBarDlg) {
		// TODO Auto-generated method stub
		progressBarDemo = progressBarDlg;
	}	
	
}
