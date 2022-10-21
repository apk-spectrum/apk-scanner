package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.resource.RStr;

public class EasyGuiEmptyPanel extends FlatPanel {

	private static final long serialVersionUID = 8040522200840281878L;
	static private Color sdkverPanelcolor = new Color(242,242,242);
	
	public EasyGuiEmptyPanel() {
		setLayout(new BorderLayout());
		
		setshadowlen(3);
		setBorder(BorderFactory.createEmptyBorder(10, 10,10, 10));
		setBackground(sdkverPanelcolor);
		JLabel scannertext = new JLabel(RStr.APP_NAME.get() + " " + RStr.APP_VERSION.get());
		scannertext.setFont(new Font(getFont().getName(), Font.BOLD, 30));
		
		JLabel makertext = new JLabel("Programmed by " + RStr.APP_MAKER_EMAIL.get() + ", 2015.");
		makertext.setFont(new Font(getFont().getName(), Font.BOLD, 10));
		
		
		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<html><div id=\"about\">");
		strTabInfo.append("  <H1>" + RStr.APP_NAME.get() + " " + RStr.APP_VERSION.get() + "</H1>");
		//strTabInfo.append("  <H3>Using following tools</H3></div>");
		strTabInfo.append("  <br/><br/>");
		strTabInfo.append("  Programmed by <a href=\"mailto:" + RStr.APP_MAKER_EMAIL.get() + "\" title=\"" + RStr.APP_MAKER_EMAIL.get() + "\">" + RStr.APP_MAKER.get() + "</a>, 2015.<br/>");
		strTabInfo.append("  It is open source project on <a href=\"https://github.sec.samsung.net/sunggyu-kam/apk-scanner\" title=\"APK Scanner Site\">SEC Github</a></html>");		
		
		//appicon.setText(strTabInfo.toString());  //delay 50ms using html
		add(scannertext, BorderLayout.CENTER);
		add(makertext, BorderLayout.SOUTH);
		
		
	}    
}
