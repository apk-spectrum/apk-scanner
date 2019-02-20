package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
		// html content
		JHtmlEditorPane hep = new JHtmlEditorPane();
		hep.setEditable(false);
		hep.setPreferredSize(new Dimension(400,300));
		hep.setAlignmentY(0.0f);
		hep.setBody(Resource.RAW_ABUOT_HTML.getString());
		hep.removeElementById("apkscanner-icon-td");
		hep.removeElementById("end-line");
		hep.setInnerHTMLById("apkscanner-title", Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		hep.setOuterHTMLById("programmer-email", String.format("<a href=\"mailto:%s\" title=\"%s\">%s</a>",
						Resource.STR_APP_MAKER_EMAIL.getString(), Resource.STR_APP_MAKER_EMAIL.getString(), Resource.STR_APP_MAKER.getString()));

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
