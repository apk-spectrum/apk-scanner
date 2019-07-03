package com.apkscanner.gui.easymode.test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
 
@SuppressWarnings("serial")
public class OverlayLayoutExample extends JFrame
{
  JPanel p1;
  JButton btn1, btn2, btn3;
  public OverlayLayoutExample()
  {
    Container c = getContentPane();
    
    p1 = new JPanel();
    
    OverlayLayout overlay = new OverlayLayout(p1);
    p1.setLayout(overlay);
 
    btn1 = new JButton("OK");
    Dimension d1 = new Dimension(75, 50);                                                                              
    btn1.setMaximumSize(d1);
    btn1.setBackground(Color.cyan);
    //  btn1.setAlignmentX(0.0f);
    //  btn1.setAlignmentY(0.0f);    
    p1.add(btn1);
 
    btn2 = new JButton("RETRY");
    Dimension d2 = new Dimension(125, 75);                                                                              
    btn2.setMaximumSize(d2);
    btn2.setBackground(Color.pink);
    //  btn2.setAlignmentX(0.0f);
    //  btn2.setAlignmentY(0.0f);    
    p1.add(btn2);
 
    btn3 = new JButton("CANCEL");
    Dimension d3 = new Dimension(150, 100);                                                                              
    btn3.setMaximumSize(d3);
    btn3.setBackground(Color.lightGray);
    //  btn3.setAlignmentX(0.0f);
    //  btn3.setAlignmentY(0.0f);    
    p1.add(btn3);
 
    c.add(p1, "Center");
 
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(300, 300);
    setVisible(true); 
  }
  public static void main(String args[])
  {
    new OverlayLayoutExample();
  } 
}