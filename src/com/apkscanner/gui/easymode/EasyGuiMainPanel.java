package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.apkscanner.gui.easymode.contents.EasyBordPanel;
import com.apkscanner.gui.easymode.contents.EasyContentsPanel;
import com.apkscanner.gui.easymode.contents.EasyPermissionPanel;

class EasyGuiMainPanel extends JPanel {
	private JPanel rootpanel;
	private JPanel borderpanel;
	static Color maincolor = new Color(249,249,249);
	public EasyGuiMainPanel(JFrame mainframe) {
		setLayout(new BorderLayout());
		setBackground(maincolor);
		add(new EasyContentsPanel(), BorderLayout.CENTER);
		add(new EasyBordPanel(mainframe), BorderLayout.PAGE_START);
		add(new EasyPermissionPanel(), BorderLayout.PAGE_END);
		setBorder(new LineBorder(Color.BLACK, 0));
	}
}


