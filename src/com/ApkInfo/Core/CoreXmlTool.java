package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
	static String APkworkPath;
	static MyApkInfo apkInfo;
	
	public static MyApkInfo XmlToMyApkinfo(String workPath) throws XPathExpressionException {
		
		apkInfo = new MyApkInfo();
		APkworkPath = new String(workPath);
		
		YmlToMyApkinfo();
		
		InputSource is = null;
		Document document = null;
		try {
			is = new InputSource(new FileReader(APkworkPath + File.separator + "AndroidManifest.xml"));
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
        apkInfo.strPackageName = cols.getAttributes().getNamedItem("package").getTextContent();
        
        cols = (Node)xpath.evaluate("//manifest/application", document, XPathConstants.NODE);

        apkInfo.strLabelname = cols.getAttributes().getNamedItem("android:label").getTextContent();
        apkInfo.strLabelname = getResourceInfo(apkInfo.strLabelname);

        apkInfo.strIconPath = cols.getAttributes().getNamedItem("android:icon").getTextContent();
        apkInfo.strIconPath = getResourceInfo(apkInfo.strIconPath);

        System.out.println("Package = " + apkInfo.strPackageName);
        System.out.println("Label = " + apkInfo.strLabelname);
        System.out.println("VersionName = " + apkInfo.strVersionName);
        System.out.println("VersionCode = " + apkInfo.strVersionCode);
        System.out.println("Icon = " + apkInfo.strIconPath);

		return apkInfo;
	}
	
	private static String getResourceInfo(String id) throws XPathExpressionException {
		
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
	
	@SuppressWarnings("resource")
	private static void YmlToMyApkinfo() {
		String ymlPath = new String(APkworkPath + File.separator + "apktool.yml");
		File ymlFile = new File(ymlPath);

		if(!ymlFile.exists()) {
			return;
		}
		
		try {
		    BufferedReader inFile;
		    String sLine = null;
			inFile = new BufferedReader(new FileReader(ymlFile));
			while( (sLine = inFile.readLine()) != null ) {
				if(sLine.matches("^\\s*versionCode:.*")) {
					apkInfo.strVersionCode = sLine.replaceFirst("\\s*versionCode:\\s*(.*)\\s*$", "$1");
				} else if(sLine.matches("^\\s*versionName:.*")) {
					apkInfo.strVersionName = sLine.replaceFirst("\\s*versionName:\\s*(.*)\\s*$", "$1");
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setProgressBarDlg(MyProgressBarDemo progressBarDlg) {
		// TODO Auto-generated method stub
		progressBarDemo = progressBarDlg;
	}	
	
}
