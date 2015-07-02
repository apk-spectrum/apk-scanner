package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.ApkInfo.Resource.Resource;
import com.ApkInfo.UI.DeviceUIManager;
import com.ApkInfo.UI.MainUI;
import com.ApkInfo.UIUtil.ToolBarButton;


public class MyToolBarUI extends JPanel implements ActionListener{

    ToolBarButton btn_open;
    JButton btn_show_manifest;
    JButton btn_show_explorer;
    JButton btn_unpack;
    JButton btn_pack;
    JButton btn_install;
    JButton btn_about;
	
    public MyToolBarUI() {
        initUI();
    }

    public final void initUI() {

        JToolBar toolbar1 = new JToolBar();
        

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        int Iconsize = 40;
        
        ImageIcon toolbar_open =  Resource.IMG_TOOLBAR_OPEN.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_manifest=  Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_explorer =  Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack  =  Resource.IMG_TOOLBAR_UNPACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack  =  Resource.IMG_TOOLBAR_PACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_install  =  Resource.IMG_TOOLBAR_INSTALL.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_about  =  Resource.IMG_TOOLBAR_ABOUT.getImageIcon(Iconsize,Iconsize);
        
        ImageIcon toobar_open_hover  =  Resource.IMG_TOOLBAR_OPEN_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_manifest_hover =  Resource.IMG_TOOLBAR_MANIFEST_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_explorer_hover =  Resource.IMG_TOOLBAR_EXPLORER_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack_hover  =  Resource.IMG_TOOLBAR_UNPACK_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack_hover  =  Resource.IMG_TOOLBAR_PACK_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_install_hover  =  Resource.IMG_TOOLBAR_INSTALL_HOVER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_about_hover  =  Resource.IMG_TOOLBAR_ABOUT_HOVER.getImageIcon(Iconsize,Iconsize);
        
        btn_open = new ToolBarButton("Open",toolbar_open, toobar_open_hover, this);
        btn_show_manifest = new ToolBarButton("manifest",toolbar_manifest, toolbar_manifest_hover, this);
        btn_show_explorer = new ToolBarButton("탐색기", toolbar_explorer, toolbar_explorer_hover, this);
        btn_unpack = new ToolBarButton("unpack", toolbar_unpack, toolbar_unpack_hover, this);
        btn_pack = new ToolBarButton("pack", toolbar_pack, toolbar_pack_hover, this);
        btn_install = new ToolBarButton("설치", toolbar_install, toolbar_install_hover, this);
        btn_about = new ToolBarButton("about", toolbar_about, toolbar_about_hover, this);

              
        toolbar1.add(btn_open);
        toolbar1.add(getNewSeparator());
                
        toolbar1.add(btn_show_manifest);
        toolbar1.add(btn_show_explorer);
        
        toolbar1.add(getNewSeparator());
        
        toolbar1.add(btn_unpack);
        toolbar1.add(btn_pack);
        
        toolbar1.add(getNewSeparator());
        
        toolbar1.add(btn_install);
    
        toolbar1.add(getNewSeparator());
        
        toolbar1.add(btn_about);
        
        toolbar1.addSeparator(new Dimension(138,0));

        toolbar1.setAlignmentX(0);
        toolbar1.setFloatable(false);
        toolbar1.setOpaque(true);

        panel.add(toolbar1,BorderLayout.WEST);
        add(panel, BorderLayout.NORTH);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        //setTitle("Toolbars");
        //setSize(360, 250);
        //setLocationRelativeTo(null);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    private JSeparator getNewSeparator()
    {
        JSeparator temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.gray);
        temp.setForeground(Color.gray);
        temp.setPreferredSize(new Dimension(1,0));
    	return temp;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	MyToolBarUI ex = new MyToolBarUI();
                ex.setVisible(true);
            }
        });
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	        JButton b = (JButton) e.getSource();
	        
	        System.out.println(b.getText());
	        
	        if (b.getText().equals("Open")) {
	        	
	        	
				JFileChooser jfc = new JFileChooser();
				//jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));
								
				jfc.showOpenDialog(null);
				File dir = jfc.getSelectedFile();

				if(dir!=null) JOptionPane.showMessageDialog(null, dir.getPath(), "Open", JOptionPane.INFORMATION_MESSAGE);
				
	        } else if(b.getText().equals("manifest")) {
				  if(System.getProperty("os.name").indexOf("Window") >-1) {
					  try {
						new ProcessBuilder("notepad", MainUI.GetMyApkInfo().strWorkAPKPath + File.separator + "AndroidManifest.xml").start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
				  } else {  //for linux
					  try {
						  new ProcessBuilder("gedit", MainUI.GetMyApkInfo().strWorkAPKPath + File.separator + "AndroidManifest.xml").start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	
				  }	
	        } else if(b.getText().equals("탐색기")) { 
				  if(System.getProperty("os.name").indexOf("Window") >-1) {
					  try {
						Process oProcess = new ProcessBuilder("explorer", MainUI.GetMyApkInfo().strWorkAPKPath).start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				  } else {  //for linux
					  try {
						  Process oProcess = new ProcessBuilder("nautilus", MainUI.GetMyApkInfo().strWorkAPKPath).start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				  }
	        } else if(b.getText().equals("unpack")) {
	        	JOptionPane.showMessageDialog(null, "unpack", "unpack", JOptionPane.INFORMATION_MESSAGE);
	        } else if(b.getText().equals("pack")) {
	        	JOptionPane.showMessageDialog(null, "pack", "pack", JOptionPane.INFORMATION_MESSAGE);
	        } else if(b.getText().equals("설치")) {
			  btn_install.setEnabled(false);
			  DeviceUIManager mMyDeviceManager = new DeviceUIManager(MainUI.GetMyApkInfo().strPackageName, MainUI.GetMyApkInfo().strAPKPath);
			  
	        } else if(b.getText().equals("about")) {
	        	JOptionPane.showMessageDialog(null, "about", "about", JOptionPane.INFORMATION_MESSAGE);
	        }
	}
}
