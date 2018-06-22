package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

public class AboutDlg /*extends JDialog*/
{
	static public void showAboutDialog(Component component)
	{
		StringBuilder body = new StringBuilder();
		body.append("<div id=\"about\">");
		body.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		body.append("  <H3>Using following tools</H3>");
		body.append("  Android Asset Packaging Tool, Android Debug Bridge, signapk<br/>");
		body.append("  - <a href=\"https://developer.android.com/tools/help/index.html\" title=\"Android Developer Site\">https://developer.android.com/tools/help/index.html</a><br/>");
		//body.append("  Apktool " + ApktoolManager.getApkToolVersion() + "<br/>");
		//body.append("  - <a href=\"http://ibotpeaches.github.io/Apktool/\" title=\"Apktool Project Site\">http://ibotpeaches.github.io/Apktool/</a><br/>");
		body.append("  JD-GUI<br/>");
		body.append("  - <a href=\"http://jd.benow.ca/\" title=\"JD Project Site\">http://jd.benow.ca/</a><br/>");
		body.append("  dex2jar<br/>");
		body.append("  - <a href=\"https://sourceforge.net/projects/dex2jar/\" title=\"JD Project Site\">https://sourceforge.net/projects/dex2jar/</a><br/>");
		body.append("  <H3>Included libraries</H3>");
		body.append("  - <a href=\"https://android.googlesource.com/platform/tools/base/+/master/ddmlib/\" title=\"Google Git Site\">ddmlib</a>,");
		body.append("  <a href=\"https://github.com/google/guava\" title=\"guava Site\">guava-18.0</a>,");
		body.append("  <a href=\"https://github.com/java-native-access/jna\" title=\"jna Site\">jna-4.4.0</a>,");
		body.append("  <a href=\"https://github.com/BlackOverlord666/mslinks\" title=\"mslinks Site\">mslinks</a>,");
		body.append("  <a href=\"http://bobbylight.github.io/RSyntaxTextArea/\" title=\"RSyntaxTextArea Site\">rsyntaxtextarea-2.6.1</a>,<br/>");
		body.append("  <a href=\"https://commons.apache.org/proper/commons-cli/\" title=\"commons-cli Site\">commons-cli-1.3.1</a>,");
		body.append("  <a href=\"https://code.google.com/archive/p/json-simple/\" title=\"json-simple Site\">json-simple-1.1.1</a>,");
		body.append("  <a href=\"https://bitbucket.org/luciad/webp-imageio\" title=\"luciad-webp-imageio Site\">luciad-webp-imageio</a>");
		body.append("  <br/><br/><hr/>");
		body.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		body.append("  It is open source project on <a href=\"https://github.sec.samsung.net/sunggyu-kam/apk-scanner\" title=\"APK Scanner Site\">SEC Github</a>");
		body.append("</div>");

		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#about {");
		style.append("width:350px;margin:0px;padding:0px;");
		style.append("background-color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		style.append("H1 {margin-top: 0px; margin-bottom: 0px;}");
		style.append("H3 {margin-top: 5px; margin-bottom: 0px;}");

		// html content
		JHtmlEditorPane hep = new JHtmlEditorPane("", style.toString(), body.toString());
		hep.setEditable(false);
		hep.setBackground(label.getBackground());
		hep.setPreferredSize(new Dimension(400,300));

		MessageBoxPane.showMessageDialog(component, hep, Resource.STR_BTN_ABOUT.getString(), MessageBoxPane.INFORMATION_MESSAGE, Resource.IMG_APP_ICON.getImageIcon(100,100));
	}
}
