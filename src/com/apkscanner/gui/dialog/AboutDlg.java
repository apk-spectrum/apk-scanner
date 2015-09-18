package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.ApktoolManager;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

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
		body.append("  - <a href=\"http://developer.android.com/tools/help/adb.html\" title=\"Android Developer Site\">http://developer.android.com/tools/help/adb.html</a><br/>");
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
}
