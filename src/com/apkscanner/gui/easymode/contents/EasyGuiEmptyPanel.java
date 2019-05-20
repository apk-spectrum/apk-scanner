package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.resource.Resource;

public class EasyGuiEmptyPanel extends FlatPanel {

	private static final long serialVersionUID = 8040522200840281878L;
	static private Color sdkverPanelcolor = new Color(242,242,242);
	
	public EasyGuiEmptyPanel() {
		setLayout(new BorderLayout());
		
		setshadowlen(3);
		setBorder(BorderFactory.createEmptyBorder(10, 10,10, 10));
		setBackground(sdkverPanelcolor);
		JLabel scannertext = new JLabel(Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		scannertext.setFont(new Font(getFont().getName(), Font.BOLD, 30));
		
		JLabel makertext = new JLabel("Programmed by " + Resource.STR_APP_MAKER_EMAIL.getString() + ", 2015.");
		makertext.setFont(new Font(getFont().getName(), Font.BOLD, 10));
		
		
		StringBuilder strTabInfo = new StringBuilder("");
		strTabInfo.append("<html><div id=\"about\">");
		strTabInfo.append("  <H1>" + Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + "</H1>");
		//strTabInfo.append("  <H3>Using following tools</H3></div>");
		strTabInfo.append("  <br/><br/>");
		strTabInfo.append("  Programmed by <a href=\"mailto:" + Resource.STR_APP_MAKER_EMAIL.getString() + "\" title=\"" + Resource.STR_APP_MAKER_EMAIL.getString() + "\">" + Resource.STR_APP_MAKER.getString() + "</a>, 2015.<br/>");
		strTabInfo.append("  It is open source project on <a href=\"https://github.sec.samsung.net/sunggyu-kam/apk-scanner\" title=\"APK Scanner Site\">SEC Github</a></html>");		
		
		//appicon.setText(strTabInfo.toString());  //delay 50ms using html
		add(scannertext, BorderLayout.CENTER);
		add(makertext, BorderLayout.SOUTH);
		
		
	}    
}
