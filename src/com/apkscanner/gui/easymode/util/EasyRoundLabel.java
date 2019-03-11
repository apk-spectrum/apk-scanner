package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

import com.apkscanner.util.Log;

public class EasyRoundLabel extends RoundPanel implements MouseListener{
	
	EasyTextField textlabel = null;
	boolean entered = false;
	
	private Color backgroundcolor;
	
	public EasyRoundLabel(String str, Color backgroundColor, Color foregroundColor) {		
		textlabel = new EasyTextField(str);		
		//setBackground(backgroundColor);
		this.backgroundcolor = backgroundColor; 
		setRoundrectColor(backgroundColor);
		//setOpaque(false);
		textlabel.setForeground(foregroundColor);
		setEasyTextField(textlabel);
		add(textlabel);
	}
	
	public void AddMouselistener() {		
		textlabel.addMouseListener(this);
    }

	private void setEasyTextField(JTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
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

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		StringSelection stringSelection = new StringSelection(textlabel.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
		AndroidLikeToast.ShowToast("Copying to the clipboard!",this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
    	entered = true;
    	setRoundrectColor(backgroundcolor.darker().darker());
    	super.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		entered = false;
		setRoundrectColor(backgroundcolor);
    	super.repaint();
	}

}
