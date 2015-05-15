package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;


class ADBDialog extends Dialog implements ActionListener 
{
	
	StandardButton btnInstall;
	Frame mainui;
	
	Label AppInfo;
	Label DeviceInfo;	
	Panel textPanel;
	
	MyDeviceInfo mMyDeviceInfo;
	
	public ADBDialog(Frame f1) {
		super(f1, "ADB Install", true);
		mainui = f1;
		setLayout(new BorderLayout());
		this.add(btnInstall = new StandardButton("ADB Install",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		
        WindowAdapter wa = new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        };       
        this.addWindowListener(wa);
		
        textPanel = new Panel(new GridLayout(1,2));
        
		setResizable( false );
		btnInstall.addActionListener(this);
		textPanel.add(AppInfo = new Label("App Infomation"));
		textPanel.add(DeviceInfo = new Label("Device Information"));
		add(textPanel, BorderLayout.NORTH);
		add(btnInstall,BorderLayout.SOUTH);
		setSize(400,300);
		this.setLocation(MainUI.nPositionX+100, MainUI.nPositionY+100);	
			
		
		mMyDeviceInfo = new MyDeviceInfo();
		
	}
	public void actionPerformed(ActionEvent e)
	{		
		setVisible(false);	
	}
	public void showPlease() {
		setVisible(true);
		
	}
	
public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path					
				Frame window = null;
				ADBDialog dlg = new ADBDialog(window);
				
				dlg.showPlease();
				
			}
		});
	}
}

