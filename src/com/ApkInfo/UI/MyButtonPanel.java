package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Desktop;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ApkInfo.Core.CoreApkTool;
import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;

public class MyButtonPanel extends JPanel{
	StandardButton btnShowManifest;
	StandardButton btnShowBrowser;
	static public StandardButton btnInstall;
	Label lbVersion;
	
	MyButtonPanel() {
        ImageIcon icon = Resource.IMG_LOADING.getImageIcon();
        ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        
		this.add(btnShowManifest = new StandardButton("Manifest 보기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);
		this.add(btnShowBrowser = new StandardButton("탐색기",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.WEST);		
		this.add(btnInstall = new StandardButton("설치",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED),BorderLayout.EAST);
		this.add(lbVersion = new Label(Resource.STR_APP_VERSION.getValue()));
		
		
		btnShowManifest.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    // display/center the jdialog when the button is pressed
		    
				//Desktop.getDesktop().open(new File(CoreApkTool.DefaultPath+File.separator+"AndroidManifest.xml"));
			  if(System.getProperty("os.name").indexOf("Window") >-1) {
				  try {
					new ProcessBuilder("notepad", MainUI.FolderDefault + File.separator + "AndroidManifest.xml").start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			  } else {  //for linux
				  try {
					  new ProcessBuilder("gedit", MainUI.FolderDefault + File.separator + "AndroidManifest.xml").start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			  }
		  }
		});
		btnShowBrowser.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
			    // display/center the jdialog when the button is pressed
				  
				  if(System.getProperty("os.name").indexOf("Window") >-1) {
					  try {
						Process oProcess = new ProcessBuilder("explorer", MainUI.FolderDefault).start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				  } else {  //for linux
					  try {
						  Process oProcess = new ProcessBuilder("nautilus", MainUI.FolderDefault).start();
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
				  //ADBDialog dt = new ADBDialog(MainUI.window);
				  //dt.showPlease();
				  btnInstall.setEnabled(false);
				  DeviceUIManager mMyDeviceManager = new DeviceUIManager(MainUI.GetMyApkInfo().strPackageName, MainUI.apkFilePath);
				  }
			});
		
		//btnInstall.setEnabled(false);
	}
}
