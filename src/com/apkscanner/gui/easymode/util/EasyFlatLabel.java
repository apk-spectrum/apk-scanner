package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

import com.apkscanner.util.Log;

public class EasyFlatLabel extends FlatPanel{
	
	EasyTextField textlabel = null;
	EasyButton button = null;
	boolean entered = false;
	private final Color btnhovercolor = new Color(200, 200, 200);
	
	public EasyFlatLabel(String str, Color backgroundColor, Color foregroundColor) {		
		textlabel = new EasyTextField(str);		
		setBackground(backgroundColor);		
		textlabel.setForeground(foregroundColor);
		setEasyTextField(textlabel);
		add(textlabel);
	}
	
	public EasyFlatLabel(ImageIcon icon, Color backgroundColor) {
		button = new EasyButton(icon);
		button.setBackground(backgroundColor);
		button.setOpaque(true);
		add(button);
	}
	public void setClicklistener(ActionListener listener) {
		button.addActionListener(listener);
	}
	private void setEasyTextField(JTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		textfield.setEditable(false);
		textfield.setOpaque(false);
		textfield.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
	}
	public void setText(String str) {
		textlabel.setText(str);
	}
	
	public void setTextFont(Font font) {
		textlabel.setFont(font);
	}
	
	public void setHorizontalAlignment(int jtextfield) {
		textlabel.setHorizontalAlignment(jtextfield);
	}
	
	public void Addlistener() {		
		textlabel.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		    	//this.setBackground(new Color(255,255,255));
		    	//setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		    	entered = true;
		    	repaint();
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    	//setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		    	entered = false;
		    	repaint();
		    }
		});
    }
	
	public void paint(Graphics g) {
		super.paint(g);
		if (entered) {			
			g.setColor(btnhovercolor);
			g.drawRect(0, 0, this.getWidth() - 3, this.getHeight() - 3);
		}
	}

}
