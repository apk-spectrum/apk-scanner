package com.ApkInfo.Core;

import java.io.BufferedReader;
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
	static String APkworkPath;
	static MyApkInfo apkInfo;
	
	public static MyApkInfo XmlToMyApkinfo(String workPath) {
		
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
        Node cols;
		try {
			cols = (Node)xpath.evaluate("/manifest", document, XPathConstants.NODE);
	        apkInfo.strPackageName = cols.getAttributes().getNamedItem("package").getTextContent();
	        
	        cols = (Node)xpath.evaluate("/manifest/application", document, XPathConstants.NODE);
	
	        apkInfo.strLabelname = cols.getAttributes().getNamedItem("android:label").getTextContent();
	        apkInfo.strLabelname = getResourceInfo(apkInfo.strLabelname);
	
	        apkInfo.strIconPath = cols.getAttributes().getNamedItem("android:icon").getTextContent();
	        apkInfo.strIconPath = getResourceInfo(apkInfo.strIconPath);
	
	        cols = (Node)xpath.evaluate("//category[@name='android.intent.category.LAUNCHER']", document, XPathConstants.NODE);
	        if(cols != null){
	        	apkInfo.strHidden = "X - LAUNCHER";
	        }else{
	        	apkInfo.strHidden = "O - HIDDEN";
	        }
	
	        NodeList colsList = (NodeList)xpath.evaluate("//uses-permission", document, XPathConstants.NODESET);
	        for( int idx=0; idx<colsList.getLength(); idx++ ){
	        	apkInfo.strPermissions += (idx==0 ? "":"\n") + colsList.item(idx).getAttributes().getNamedItem("android:name").getTextContent();
	        }
	        colsList = (NodeList)xpath.evaluate("//permission", document, XPathConstants.NODESET);
	        for( int idx=0; idx<colsList.getLength(); idx++ ){
	        	apkInfo.strPermissions += (apkInfo.strPermissions=="" ? "":"\n") + colsList.item(idx).getAttributes().getNamedItem("android:name").getTextContent();
	        }
	        
	        NodeList widgetList = (NodeList)xpath.evaluate("//meta-data[@name='android.appwidget.provider']", document, XPathConstants.NODESET);
	        System.out.println("widgetList cnt = " + widgetList.getLength());
	        for( int idx=0; idx<widgetList.getLength(); idx++ ){
	        	String widgetTitle = "Unknown"; 
	        	Object[] widgetExtraInfo = null;
	        	Node parent = widgetList.item(idx).getParentNode().getAttributes().getNamedItem("android:label");
	        	Node res = widgetList.item(idx).getAttributes().getNamedItem("android:resource");
	        	if(parent != null) {
		        	widgetTitle = getResourceInfo(parent.getTextContent());
	        	}
	        	if(res != null) {
	        		widgetExtraInfo = getWidgetInfo(res.getTextContent());
	        	}

	        	System.out.println("widgetTitle = " + widgetTitle);
	        	System.out.println("widget = " + res.getTextContent());

	        	apkInfo.arrWidgets.add(new Object[] {widgetExtraInfo[0], widgetTitle, widgetExtraInfo[1], widgetExtraInfo[2], widgetExtraInfo[3]});
	        }

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Package = " + apkInfo.strPackageName);
        System.out.println("Label = " + apkInfo.strLabelname);
        System.out.println("VersionName = " + apkInfo.strVersionName);
        System.out.println("VersionCode = " + apkInfo.strVersionCode);
        System.out.println("minSdkVersion = " + apkInfo.strMinSDKversion);
        System.out.println("targetSdkVersion = " + apkInfo.strTargerSDKversion);
        System.out.println("Hidden = " + apkInfo.strHidden);
        System.out.println("Hidden = " + apkInfo.strPermissions);
        System.out.println("Icon = " + apkInfo.strIconPath);

		return apkInfo;
	}
	
	private static String getResourceInfo(String id) {
		
		String result = null;
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);
		String query = "//";
		
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
			query = "//resources/string[@name='"+id.substring(id.indexOf("/")+1)+"']";
		} else if(id.matches("^@xml/.*")) {
			System.out.println("@xml");
			resXmlPath += id.substring(1);
		} else if(id.matches("^@dimen/.*")) {
			System.out.println("@dimen");
			resXmlPath += "values" + File.separator + "dimens.xml";
			query = "//resources/dimen[@name='"+id.substring(id.indexOf("/")+1)+"']";
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
		
		  
        // NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
        Node cols;
		try {
			cols = (Node)xpath.evaluate(query, document, XPathConstants.NODE);
	        result = new String(cols.getTextContent());
	    	System.out.println(">> " + cols.getTextContent());
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	private static Object[] getWidgetInfo(String resource) {

		System.out.println("getWidgetInfo() " + resource);
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);

		String Size = "Unknown";
		String IconPath = "Unknown";
		String Type = "Unknown";
		String Activity = "Unknown";
		
		if(!resource.matches("^@xml/.*")) {
			return null;
		}

		String widgetXml = new String(resource.substring(5));
		System.out.println("widgetXml : " + widgetXml);

		for (String s : (new File(resXmlPath)).list()) {
			if(!s.matches("^xml.*")) continue;

			File xmlFile = new File(resXmlPath + s + File.separator + widgetXml + ".xml");
			if(!xmlFile.exists()) continue;
			
			System.out.println("xmlFile " + xmlFile.getAbsolutePath());

			
			InputSource is = null;
			Document document = null;
			try {
				is = new InputSource(new FileReader(xmlFile.getAbsolutePath()));
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
	        Node cols;
			try {
				cols = (Node)xpath.evaluate("//appwidget-provider", document, XPathConstants.NODE);
		        
				if(Size.equals("Unknown") && cols.getAttributes().getNamedItem("android:minWidth") != null
						&& cols.getAttributes().getNamedItem("android:minHeight") != null) {
					String width = cols.getAttributes().getNamedItem("android:minWidth").getTextContent();
					String Height = cols.getAttributes().getNamedItem("android:minHeight").getTextContent();
					width = getResourceInfo(width).replaceAll("^([0-9]*).*", "$1");
					Height = getResourceInfo(Height).replaceAll("^([0-9]*).*", "$1");
					Size = (int)Math.ceil(Float.parseFloat(width) / 74) + " X " + (int)Math.ceil(Float.parseFloat(Height) / 74);
			    	System.out.println("Size " + Size);
				}

				if(IconPath.equals("Unknown") && cols.getAttributes().getNamedItem("android:previewImage") != null) {
					String icon = cols.getAttributes().getNamedItem("android:previewImage").getTextContent();
			    	System.out.println("icon " + getResourceInfo(icon));
				}
				//cols.getAttributes().getNamedItem("android:label").getTextContent();
		    	//System.out.println("string " + cols.getTextContent());
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
		return new Object[] { 
    			IconPath,
    			Size,
    			Activity,
    			Type,
			};
		/*
		String result = null;
		String resXmlPath = new String(APkworkPath + File.separator + "res" + File.separator);


*/
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
					apkInfo.strVersionCode = sLine.replaceFirst("\\s*versionCode:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					apkInfo.strVersionCode = getResourceInfo(apkInfo.strVersionCode);
				} else if(sLine.matches("^\\s*versionName:.*")) {
					apkInfo.strVersionName = sLine.replaceFirst("\\s*versionName:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
					apkInfo.strVersionName = getResourceInfo(apkInfo.strVersionName);
				} else if(sLine.matches("^\\s*minSdkVersion:.*")) {
					apkInfo.strMinSDKversion = sLine.replaceFirst("\\s*minSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
				} else if(sLine.matches("^\\s*targetSdkVersion:.*")) {
					apkInfo.strTargerSDKversion = sLine.replaceFirst("\\s*targetSdkVersion:\\s*['\"]?([^'\"]+)['\"]?\\s*$", "$1");
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
