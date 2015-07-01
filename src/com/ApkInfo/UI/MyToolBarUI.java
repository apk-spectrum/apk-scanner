package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Color;
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
        ImageIcon toolbar_show_manifest=  Resource.IMG_TOOLBAR_SHOW_MANIFEST.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_show_explorer =  Resource.IMG_TOOLBAR_SHOW_EXPLORER.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_unpack  =  Resource.IMG_TOOLBAR_UNPACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_pack  =  Resource.IMG_TOOLBAR_PACK.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_install  =  Resource.IMG_TOOLBAR_INSTALL.getImageIcon(Iconsize,Iconsize);
        ImageIcon toolbar_about  =  Resource.IMG_TOOLBAR_ABOUT.getImageIcon(Iconsize,Iconsize);
        
        ImageIcon toobar_blue_open  =  Resource.IMG_TOOLBAR_OPEN_BLUE.getImageIcon(Iconsize,Iconsize);
        ImageIcon toobar_blue_install  =  Resource.IMG_TOOLBAR_INSTALL_BLUE.getImageIcon(Iconsize,Iconsize);
        
        btn_open = new ToolBarButton("Open",toolbar_open, toobar_blue_open);
        btn_show_manifest = new JButton("manifest",toolbar_show_manifest);
        btn_show_explorer = new JButton("탐색기", toolbar_show_explorer);
        btn_unpack = new JButton("unpack", toolbar_unpack);
        btn_pack = new JButton("pack", toolbar_pack);
        btn_install = new JButton("설치", toolbar_install);
        btn_about = new JButton("about", toolbar_about);
        
        setToolbarButton(btn_open);
        setToolbarButton(btn_show_manifest);
        setToolbarButton(btn_show_explorer);
        setToolbarButton(btn_unpack);
        setToolbarButton(btn_pack);
        setToolbarButton(btn_install);
        setToolbarButton(btn_about);
 
        JSeparator temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.GRAY);
                
        toolbar1.add(btn_open);
        toolbar1.add(temp);
                
        toolbar1.add(btn_show_manifest);
        toolbar1.add(btn_show_explorer);
        
        temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.GRAY);
        
        toolbar1.add(temp);
        
        toolbar1.add(btn_unpack);
        toolbar1.add(btn_pack);
        
        temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.GRAY);
        
        toolbar1.add(temp);
        
        toolbar1.add(btn_install);
        
        temp = new JSeparator(JSeparator.VERTICAL);
        temp.setBackground(Color.GRAY);
        
        toolbar1.add(temp);
        
        toolbar1.add(btn_about);

        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        toolbar1.addSeparator();
        
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

    public void setToolbarButton(JButton temp) {
    	temp.setVerticalTextPosition(JLabel.BOTTOM);
    	temp.setHorizontalTextPosition(JLabel.CENTER);
    	temp.setBorderPainted(false);
    	temp.setOpaque(false);
    	temp.setFocusable(false);
    	temp.addActionListener(this);
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
