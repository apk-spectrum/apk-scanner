package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;


class ADBDialog extends Dialog implements ActionListener 
{
	TextField tf2;
	StandardButton btnInstall;
	Frame mainui;
	public ADBDialog(Frame f1) {
		super(f1, "ADB Install", true);
		mainui = f1;
		setLayout(new FlowLayout());
		this.add(btnInstall = new StandardButton("ADB Install",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		
		tf2 = new TextField(10);
		btnInstall.addActionListener(this);
		add(new Label("Enter Your Password"));
		add(tf2);
		add(btnInstall);
		setSize(225,125);
		this.setLocation(100, 100);
		
		
		
		
		
		}
	public void actionPerformed(ActionEvent e)
	{		
		setVisible(false);
	}
	public void showPlease() {
		setVisible(true);
		
		System.out.println(mainui.getLocation().y);
		
	}
}

