package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;

import javax.swing.JPanel;

import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

public class MyButtonPanel extends JPanel{
	
	MyButtonPanel() {
		this.add(new StandardButton("Manifest 보기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		this.add(new StandardButton("탐색기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		
		this.add(new StandardButton("설치",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.EAST);		
	}	
}
