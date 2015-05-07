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
        
        System.out.println("package = " + cols.getAttributes().getNamedItem("package").getTextContent());
        System.out.println("platformBuildVersionCode = " +cols.getAttributes().getNamedItem("platformBuildVersionCode").getTextContent());
        System.out.println("platformBuildVersionName = " +cols.getAttributes().getNamedItem("platformBuildVersionName").getTextContent());
        
        temp.strPackageName = cols.getAttributes().getNamedItem("package").getTextContent();
        temp.strVersionCode = cols.getAttributes().getNamedItem("platformBuildVersionCode").getTextContent();
        temp.strVersionName = cols.getAttributes().getNamedItem("platformBuildVersionName").getTextContent();
    
	        
	       
		return temp;
	}

	public static void setProgressBarDlg(MyProgressBarDemo progressBarDlg) {
		// TODO Auto-generated method stub
		progressBarDemo = progressBarDlg;
	}	
	
}
