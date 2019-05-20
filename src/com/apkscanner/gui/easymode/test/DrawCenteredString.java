package com.apkscanner.gui.easymode.test;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class DrawCenteredString extends JComponent {
   
   public static void main(String[] args) {
      JComponent myGraphics = new DrawCenteredString(); // Your class name goes here!
      launch(myGraphics, 500, 300); // Set the initial dimensions here!
   }
   
  public static int getStringWidth(Graphics page, Font f, String s) {
    // Find the size of string s in the font of the Graphics context "page"
    FontMetrics fm   = page.getFontMetrics(f);
    java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
    return (int)Math.round(rect.getWidth());
  }

  public static int getStringHeight(Graphics page, Font f, String s) {
    // Find the size of string s in the font of the Graphics context "page"
    FontMetrics fm   = page.getFontMetrics(f);
    java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
    return (int)Math.round(rect.getHeight());
  }

  public static int getStringAscent(Graphics page, Font f, String s) {
    // Find the size of string s in the font of the Graphics context "page"
    FontMetrics fm   = page.getFontMetrics(f);
    return fm.getAscent();
  }
    
  public static void drawCenteredString(Graphics page, String s, int x, int y, int width, int height) {
    Font font = page.getFont();
    int textWidth  = getStringWidth(page,font,s);
    int textHeight = getStringHeight(page,font,s);
    int textAscent = getStringAscent(page,font,s);

    // Center text horizontally and vertically within provided rectangular bounds
    int textX = x + (width - textWidth)/2;
    int textY = y + (height - textHeight)/2 + textAscent;
    page.drawString(s, textX, textY);
  }
  
  public void paint(Graphics page) {
    int width  = getWidth();
    int height = getHeight();
    int margin = Math.min(width,height)/10; // margin around the window
    int boxLeft = margin, boxTop = margin;
    int boxWidth = width - 2*margin, boxHeight = height - 2*margin;
    
    String s = "Carpe diem";
    
	Graphics2D graphics2D = (Graphics2D) page;
	graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // paint the box
    page.setColor(Color.orange);
    page.fillRoundRect(boxLeft,boxTop,boxWidth,boxHeight, 50,50);
    
    // find the largest dimensions of the string that can fit in the window
    int fontSize = 6;
    while (true) {
        Font f = new Font("SansSerif",Font.BOLD,fontSize+1);
        if ((getStringWidth(page,f,s) >= boxWidth) ||
            (getStringHeight(page,f,s) >= boxHeight))
            break;
        fontSize++;
    }

    // center a string in the box with a font-size of 32 pixels
    page.setColor(Color.black);
    Font f = new Font("SansSerif",Font.BOLD,fontSize);
    page.setFont(f);
    drawCenteredString(page,s,boxLeft,boxTop,boxWidth,boxHeight);
  }
   
   //////////////////////////////////////////////////////////////////////////////
   /////////////////           END OF YOUR CODE             /////////////////////
   /////////////////  (you may ignore all the code below!!! /////////////////////
   //////////////////////////////////////////////////////////////////////////////

   public static void launch(JComponent jc, int width, int height) {
      JFrame frame = new JFrame(jc.getClass().getName());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JPanel cp = new JPanel();
      cp.setLayout(new BorderLayout());
      cp.add(jc);
      frame.setContentPane(cp);
      frame.setSize(new Dimension(width,height));
      frame.setVisible(true);
   }
}