package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.apkscanner.core.AaptWrapper;
import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.ApktoolManager;
import com.apkscanner.gui.util.ArrowTraversalPane;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

import com.apkscanner.gui.dialog.install.*;

public class AboutDlg /*extends JDialog*/
{
	public AboutDlg()
	{

	}
	
	static public int showAboutDialog(Component component)
	{
		final ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon(100,100);
	    
		StringBuilder body = new StringBuilder();
		body.append("<div id=\"about\">");
		body.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		body.append("  Using following tools,<br/>");
		body.append("  Apktool " + ApktoolManager.getApkToolVersion() + "<br/>");
		body.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
		body.append("  " + AdbWrapper.getVersion() + "<br/>");
		body.append("  " + AaptWrapper.getVersion() + "<br/>");
		body.append("  - <a href=\"https://developer.android.com/tools/help/index.html\" title=\"Android Developer Site\">https://developer.android.com/tools/help/index.html</a><br/>");
		body.append("  <br/><hr/>");
		body.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		body.append("</div>");

		JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuilder style = new StringBuilder("#about {");
	    style.append("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;}");
	    style.append("#about a {text-decoration:none;}");

	    // html content
	    JHtmlEditorPane hep = new JHtmlEditorPane("", "", body.toString());
	    hep.setStyle(style.toString());

	    hep.setEditable(false);
	    hep.setBackground(label.getBackground());
	    
	    return JOptionPane.showOptionDialog(component, hep, Resource.STR_BTN_ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, Appicon,
	    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
	}
	
	static public JPanel GetPanel()
	{
		JPanel infoPanel = new JPanel();
		StringBuilder body = new StringBuilder();
		body.append("<br/>");
		
		body.append("<div id=\"about\">");
		body.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		body.append("  Using following tools,<br/>");
		body.append("  Apktool " + ApktoolManager.getApkToolVersion() + "<br/>");
		body.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
		body.append("  " + AdbWrapper.getVersion() + "<br/>");
		body.append("  " + AaptWrapper.getVersion() + "<br/>");
		body.append("  - <a href=\"https://developer.android.com/tools/help/index.html\" title=\"Android Developer Site\">https://developer.android.com/tools/help/index.html</a><br/>");
		body.append("  <br/><hr/>");
		body.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		body.append("</div>");
		
		body.append("<br/><br/>");
		

		JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuilder style = new StringBuilder("#about {");
	    style.append("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;}");
	    style.append("#about a {text-decoration:none;}");

	    // html content
	    JHtmlEditorPane hep = new JHtmlEditorPane("", "", body.toString());
	    hep.setStyle(style.toString());

	    hep.setEditable(false);
	    hep.setBackground(label.getBackground());
	    hep.setBackgroundImg(Resource.IMG_APP_ICON.getImageIcon(250,250).getImage());
	    //Spotlight SpotPanel = new Spotlight(Resource.IMG_APP_ICON.getImageIcon(250,250).getImage());
	    
	    
	    
	    //hep.add(SpotPanel);
	    
	    infoPanel.add(hep);
	    return infoPanel;
	}
}
