package com.apkscanner.gui.installer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.apkscanner.core.installer.OptionsBundle;
import com.apkscanner.data.apkinfo.CompactApkInfo;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

public class SimpleOptionPanel extends JPanel {
	private static final long serialVersionUID = -1856410346702035872L;

	public static final String ACT_CMD_SIMPLE_INSTALL = "ACT_CMD_SIMPLE_INSTALL";
	public static final String ACT_CMD_SIMPLE_PUSH = "ACT_CMD_SIMPLE_PUSH";
	public static final String ACT_CMD_SET_ADVANCED_OPT = "ACT_CMD_SET_ADVANCED";

	public SimpleOptionPanel(ActionListener listener) {
		setLayout(new GridBagLayout());

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) 
		GridBagConstraints consts = new GridBagConstraints(0,0,1,1,1.0f,1.0f,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,1,0,1),0,0);

		JHtmlEditorPane html = new JHtmlEditorPane();
		html.setText(Resource.RAW_ADB_INSTALL_BUTTON_HTML.getString());
		html.removeElementById("adb-push");
		
		JButton btn = new JButton(html.getText());
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.setHorizontalTextPosition(SwingConstants.RIGHT);
		btn.setIcon(Resource.IMG_TOOLBAR_INSTALL.getImageIcon());;
		btn.setIconTextGap(20);
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SIMPLE_INSTALL);
		add(btn, consts);

		consts.gridy++;
		consts.weighty = 0.2f;

		html.setText(Resource.RAW_ADB_INSTALL_BUTTON_HTML.getString());
		html.removeElementById("adb-install");

		btn = new JButton(html.getText());
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SIMPLE_PUSH);
		add(btn, consts);

		consts.gridy++;
		consts.weighty = 0.0f;
		btn = new JButton("<html><body><h4>Advanced settings</h4></body></html>");
		btn.addActionListener(listener);
		btn.setActionCommand(ACT_CMD_SET_ADVANCED_OPT);
		add(btn, consts);
	}

	public void setApkInfo(CompactApkInfo apkinfo) {

	}

	public void setOptions(OptionsBundle bundle) {

	}
}
