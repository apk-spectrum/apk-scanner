package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.resource.RImg;

public class EasyGuiLoadingPanel extends FlatPanel {

	private static final long serialVersionUID = 8040522200840281878L;
	static private Color sdkverPanelcolor = new Color(242,242,242);
	
	public EasyGuiLoadingPanel(String str) {
		setLayout(new BorderLayout());
		
		setshadowlen(3);
		setBorder(BorderFactory.createEmptyBorder(10, 10,10, 10));
		setBackground(sdkverPanelcolor);
		JLabel scannertext = new JLabel(str);
		scannertext.setFont(new Font(getFont().getName(), Font.BOLD, 10));
		scannertext.setIcon(RImg.TREE_LOADING.getImageIcon());
		
		//appicon.setText(strTabInfo.toString());  //delay 50ms using html
		add(scannertext, BorderLayout.CENTER);
		
	}    
}
