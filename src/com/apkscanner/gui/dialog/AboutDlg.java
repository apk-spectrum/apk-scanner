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
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#about {");
		style.append("margin:0px;padding:0px;");
		style.append("background-color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		style.append("H1 {margin-top: 0px; margin-bottom: 0px;}");
		style.append("H3 {margin-top: 5px; margin-bottom: 0px;}");

		// html content
		JHtmlEditorPane hep = new JHtmlEditorPane();
		hep.setEditable(false);
		hep.setBackground(label.getBackground());
		hep.setPreferredSize(new Dimension(400,300));
		hep.setAlignmentY(0.0f);
		hep.addStyleRule(style.toString());
		hep.setBody(Resource.RAW_ABUOT_HTML.getString());
		hep.removeElementById("apkscanner-icon-td");
		hep.removeElementById("end-line");
		hep.setInnerHTMLById("apkscanner-title", Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		hep.setOuterHTMLById("programmer-email", "<a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>");

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
