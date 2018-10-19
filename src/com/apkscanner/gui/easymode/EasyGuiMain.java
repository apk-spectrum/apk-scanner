package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiMain{
	public static JFrame frame;
	public EasyGuiMain() {
	}

	public static void main(String[] args) {		
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

            	System.setProperty("awt.useSystemAAFontSettings","on");
            	System.setProperty("swing.aatext", "true");
            	
            	frame = new JFrame("APKScanner - DCMHome.apk");
                frame.setUndecorated(true);                
                frame.add(new EasyGuiMainPanel(frame));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
                frame.setVisible(true);                
            }
        });
	}
}