package com.ApkInfo.UITEST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.ApkInfo.Resource.Resource;


public class Example extends JPanel {

    public Example() {
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
        
        
        JButton btn_open = new JButton("Open",toolbar_open);
        JButton btn_show_manifest = new JButton("manifest",toolbar_show_manifest);
        JButton btn_show_explorer = new JButton("탐색기", toolbar_show_explorer);
        JButton btn_unpack = new JButton("unpack", toolbar_unpack);
        JButton btn_pack = new JButton("pack", toolbar_pack);
        JButton btn_install = new JButton("설치", toolbar_install);
        JButton btn_about = new JButton("about", toolbar_about);
        
        btn_open.setVerticalTextPosition(JLabel.BOTTOM);
        btn_open.setHorizontalTextPosition(JLabel.CENTER);
        btn_open.setBorderPainted(false);
        btn_open.setOpaque(false);
        
        btn_show_manifest.setVerticalTextPosition(JLabel.BOTTOM);
        btn_show_manifest.setHorizontalTextPosition(JLabel.CENTER);
        btn_show_manifest.setBorderPainted(false);
        btn_show_manifest.setOpaque(false);
        
        btn_show_explorer.setVerticalTextPosition(JLabel.BOTTOM);
        btn_show_explorer.setHorizontalTextPosition(JLabel.CENTER);
        btn_show_explorer.setBorderPainted(false);
        btn_show_explorer.setOpaque(false);
        
        btn_unpack.setVerticalTextPosition(JLabel.BOTTOM);
        btn_unpack.setHorizontalTextPosition(JLabel.CENTER);
        btn_unpack.setBorderPainted(false);
        btn_unpack.setOpaque(false);
        
        btn_pack.setVerticalTextPosition(JLabel.BOTTOM);
        btn_pack.setHorizontalTextPosition(JLabel.CENTER);
        btn_pack.setBorderPainted(false);
        btn_pack.setOpaque(false);
        
        btn_install.setVerticalTextPosition(JLabel.BOTTOM);
        btn_install.setHorizontalTextPosition(JLabel.CENTER);
        btn_install.setBorderPainted(false);
        btn_install.setOpaque(false);
        
        btn_about.setVerticalTextPosition(JLabel.BOTTOM);
        btn_about.setHorizontalTextPosition(JLabel.CENTER);
        btn_about.setBorderPainted(false);
        btn_about.setOpaque(false);
        
        toolbar1.add(btn_open);
        toolbar1.add(new JSeparator(JSeparator.VERTICAL));
        
        toolbar1.add(btn_show_manifest);
        toolbar1.add(btn_show_explorer);
        toolbar1.add(new JSeparator(JSeparator.VERTICAL));
        
        toolbar1.add(btn_unpack);
        toolbar1.add(btn_pack);
        
        toolbar1.add(new JSeparator(JSeparator.VERTICAL));
        
        toolbar1.add(btn_install);
        toolbar1.add(new JSeparator(JSeparator.VERTICAL));
        
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

        //setTitle("Toolbars");
        //setSize(360, 250);
        //setLocationRelativeTo(null);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Example ex = new Example();
                ex.setVisible(true);
            }
        });
    }
}
