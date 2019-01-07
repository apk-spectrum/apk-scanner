package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.gui.UpdateNotificationPanel;
import com.apkscanner.resource.Resource;

public class AboutDlg /*extends JDialog*/
{
	static JCheckBox showUpdatePopup;

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
		body.append("  JD-GUI - <a href=\"http://jd.benow.ca/\" title=\"JD Project Site\">http://jd.benow.ca/</a><br/>");
		body.append("  JADX-GUI - <a href=\"https://github.com/skylot/jadx\" title=\"JADX Project Site\">https://github.com/skylot/jadx</a><br/>");
		body.append("  Bytecode Viewer - <a href=\"https://github.com/konloch/bytecode-viewer\" title=\"Bytecode Viewer Project Site\">https://github.com/konloch/bytecode-viewer</a><br/>");
		body.append("  dex2jar - <a href=\"https://sourceforge.net/projects/dex2jar/\" title=\"JD Project Site\">https://sourceforge.net/projects/dex2jar/</a><br/>");
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
		body.append("  It is open source project on <a href=\"https://github.com/apk-spectrum/apk-scanner\" title=\"APK Scanner Site\">Github</a>");
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
		hep.setAlignmentY(0.0f);

		ImagePanel imagePanel = new ImagePanel(Resource.IMG_APP_ICON.getImageIcon(100,100));
		imagePanel.setAlignmentY(0.0f);

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.X_AXIS));
		aboutPanel.add(imagePanel);
		aboutPanel.add(hep);

		JTabbedPane tabbed = new JTabbedPane();
		tabbed.setOpaque(true);
		tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbed.setBorder(new EmptyBorder(0,0,0,0));

		TabbedPaneUIManager.setUI(tabbed, (String) Resource.PROP_TABBED_UI_THEME.getData());

		IUpdateChecker[] updator = PlugInManager.getUpdateChecker();
		int possibleCnt = 0;
		for(IUpdateChecker plugin: updator) {
			if(plugin.hasNewVersion()) {
				possibleCnt++;
			}
		}

		UpdateNotificationPanel updatePanel = new UpdateNotificationPanel();
		updatePanel.addUpdateList(updator);
		updatePanel.add(showUpdatePopup = new JCheckBox(Resource.STR_LABEL_SHOW_UPDATE_STARTUP.getString()));

		boolean propNoLookPopup = "true".equals(PlugInConfig.getGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP));
		showUpdatePopup.setSelected(!propNoLookPopup);

		tabbed.addTab(Resource.STR_TAB_ABOUT.getString(), aboutPanel);
		tabbed.addTab(String.format(Resource.STR_TAB_UPDATE.getString(), possibleCnt), updatePanel);

		if(possibleCnt > 0) {
			tabbed.setSelectedComponent(updatePanel);
		}

		JPanel tabbedPanel = new JPanel();
		tabbedPanel.setLayout(new BoxLayout(tabbedPanel, BoxLayout.Y_AXIS));
		tabbedPanel.add(tabbed);

		MessageBoxPane.showMessageDialog(component, tabbedPanel, Resource.STR_BTN_ABOUT.getString(), MessageBoxPane.PLAIN_MESSAGE, null);

		if(propNoLookPopup == showUpdatePopup.isSelected()) {
			PlugInConfig.setGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP, propNoLookPopup ? null : "true");
			PlugInManager.saveProperty();
		}
	}
}
