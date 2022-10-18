package com.apkscanner.gui.easymode.test;

import java.awt.*;
import javax.swing.*;

public class DisplayDifferentFonts extends JComponent {
   private static final long serialVersionUID = -8219013563323954697L;

   String[] differentFonts;
   Font[] font;
   static final int IN = 15;
   public DisplayDifferentFonts() {
	  	System.setProperty("awt.useSystemAAFontSettings","on");
	  	System.setProperty("swing.aatext", "true");
   differentFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames();
    font = new Font[differentFonts.length];
  }
  public void paintComponent(Graphics g) {
	 super.paintComponent(g);
    for (int j = 0; j < differentFonts.length; j += 1) {
      if (font[j] == null) {
        font[j] = new Font(differentFonts[j], Font.PLAIN, 16);
      }
      g.setFont(font[j]);
      int p = 15;
      int q = 15+ (IN * j);
      g.drawString(differentFonts[j],p,q);
    }
  }
  public static void main(String[] args) {

    JFrame frame = new JFrame("Different Fonts");
    frame.getContentPane().add(new JScrollPane(new DisplayDifferentFonts()));
    frame.setSize(350, 650);
    frame.setVisible(true);
  }
} 