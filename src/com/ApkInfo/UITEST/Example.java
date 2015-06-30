package com.ApkInfo.UITEST;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

        ImageIcon newi =  Resource.IMG_SUCCESS.getImageIcon();
        ImageIcon open =  Resource.IMG_QUESTION.getImageIcon();
        ImageIcon save =  Resource.IMG_WARNING.getImageIcon();
        ImageIcon exit  =  Resource.IMG_SUCCESS.getImageIcon();

        JButton newb = new JButton("aaaaa",newi);
        JButton openb = new JButton("bbbbbb",open);
        JButton saveb = new JButton("cccccc", save);
        JButton eeeeb = new JButton("cccccc", save);
        JButton bbbbb = new JButton("cccccc", save);
        
        newb.setVerticalTextPosition(JLabel.BOTTOM);
        newb.setHorizontalTextPosition(JLabel.CENTER);
        
        openb.setVerticalTextPosition(JLabel.BOTTOM);
        openb.setHorizontalTextPosition(JLabel.CENTER);
        
        saveb.setVerticalTextPosition(JLabel.BOTTOM);
        saveb.setHorizontalTextPosition(JLabel.CENTER);
        
        eeeeb.setVerticalTextPosition(JLabel.BOTTOM);
        eeeeb.setHorizontalTextPosition(JLabel.CENTER);
        
        bbbbb.setVerticalTextPosition(JLabel.BOTTOM);
        bbbbb.setHorizontalTextPosition(JLabel.CENTER);
        
        toolbar1.add(newb);
        toolbar1.addSeparator();
        toolbar1.add(openb);
        toolbar1.add(saveb);
        toolbar1.addSeparator();
        toolbar1.add(eeeeb);
        toolbar1.add(bbbbb);
        
        toolbar1.setAlignmentX(0);
        toolbar1.setFloatable(false);

        panel.add(toolbar1);        
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
