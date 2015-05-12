package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

public class MyButtonPanel extends JPanel{
	StandardButton btnShowManifest;
	StandardButton btnShowBrowser;
	StandardButton btnInstall;
	
	
	MyButtonPanel() {
		this.add(btnShowManifest = new StandardButton("Manifest 보기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		this.add(btnShowBrowser = new StandardButton("탐색기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		
		this.add(btnInstall = new StandardButton("설치",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.EAST);
		
		btnShowManifest.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    // display/center the jdialog when the button is pressed
		    try {
				Desktop.getDesktop().open(new File(CoreApkTool.DefaultPath+File.separator+"AndroidManifest.xml"));				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  }
		});
		btnShowBrowser.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
			    // display/center the jdialog when the button is pressed
				  
				  if(System.getProperty("os.name").indexOf("Window") > -1) {
					  try {
						Process oProcess = new ProcessBuilder("explorer", CoreApkTool.DefaultPath).start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				  } else {  //for linux
					  try {
						  Process oProcess = new ProcessBuilder("nautilus", CoreApkTool.DefaultPath).start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				  }
			  }
		});
		btnInstall.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
				    // display/center the jdialog when the button is pressed
				  System.out.println("click install");
				  }
			});
		
	}
}
