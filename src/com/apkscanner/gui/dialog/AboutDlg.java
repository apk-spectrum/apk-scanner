package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.plugin.IUpdateChecker;
import com.apkspectrum.plugin.PlugInConfig;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.plugin.gui.UpdateNotificationPanel;
import com.apkspectrum.swing.HtmlEditorPane;
import com.apkspectrum.swing.ImagePanel;
import com.apkspectrum.swing.tabbedpaneui.MessageBoxPane;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;

public class AboutDlg /*extends JDialog*/
{
	static JCheckBox showUpdatePopup;

	static public void showAboutDialog(Component component)
	{
		// html content
		HtmlEditorPane hep = new HtmlEditorPane();
		hep.setEditable(false);
		hep.setPreferredSize(new Dimension(400,300));
		hep.setAlignmentY(0.0f);
		hep.setBody(RFile.RAW_ABUOT_HTML.getString());
		hep.removeElementById("apkscanner-icon-td");
		hep.removeElementById("end-line");
		hep.setInnerHTMLById("apkscanner-title", RStr.APP_NAME.get() + " " + RStr.APP_VERSION.get());
		hep.setOuterHTMLById("programmer-email", String.format("<a href=\"mailto:%s\" title=\"%s\">%s</a>",
						RStr.APP_MAKER_EMAIL.get(), RStr.APP_MAKER_EMAIL.get(), RStr.APP_MAKER.get()));

		ImagePanel imagePanel = new ImagePanel(RImg.APP_ICON.getImage(100,100));
		imagePanel.setAlignmentY(0.0f);

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.X_AXIS));
		aboutPanel.add(imagePanel);
		aboutPanel.add(hep);

		JTabbedPane tabbed = new JTabbedPane();
		tabbed.setOpaque(true);
		tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbed.setBorder(new EmptyBorder(0,0,0,0));

		TabbedPaneUIManager.setUI(tabbed, RProp.S.TABBED_UI_THEME.get());

		IUpdateChecker[] updator = PlugInManager.getUpdateChecker();
		int possibleCnt = 0;
		for(IUpdateChecker plugin: updator) {
			if(plugin.hasNewVersion()) {
				possibleCnt++;
			}
		}

		UpdateNotificationPanel updatePanel = new UpdateNotificationPanel();
		updatePanel.addUpdateList(updator);
		updatePanel.add(showUpdatePopup = new JCheckBox(RStr.LABEL_SHOW_UPDATE_STARTUP.get()));

		boolean propNoLookPopup = "true".equals(PlugInConfig.getGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP));
		showUpdatePopup.setSelected(!propNoLookPopup);

		tabbed.addTab(RStr.TAB_ABOUT.get(), aboutPanel);
		tabbed.addTab(String.format(RStr.TAB_UPDATE.get(), possibleCnt), updatePanel);

		if(possibleCnt > 0) {
			tabbed.setSelectedComponent(updatePanel);
		}

		JPanel tabbedPanel = new JPanel();
		tabbedPanel.setLayout(new BoxLayout(tabbedPanel, BoxLayout.Y_AXIS));
		tabbedPanel.add(tabbed);

		MessageBoxPane.showMessageDialog(component, tabbedPanel, RStr.BTN_ABOUT.get(), MessageBoxPane.PLAIN_MESSAGE, null);

		if(propNoLookPopup == showUpdatePopup.isSelected()) {
			PlugInConfig.setGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP, propNoLookPopup ? null : "true");
			PlugInManager.saveProperty();
		}
	}
}
