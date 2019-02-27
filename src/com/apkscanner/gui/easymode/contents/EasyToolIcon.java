package com.apkscanner.gui.easymode.contents;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.util.Log;

public class EasyToolIcon extends JLabel implements MouseListener{
	int originalsize;
	int hoversize;
	int width,height;
	float scalex = 1.0f;
	boolean entered = false;
	Image image;
	String ActionCmd = "";
	ActionListener actionlistener = null;
	public EasyToolIcon(int size) {
		originalsize = size;
	}

	public EasyToolIcon(ImageIcon imageIcon) {
		// TODO Auto-generated constructor stub
		image = imageIcon.getImage();
		width = height = originalsize = 30;
		this.addMouseListener(this);
		setPreferredSize(new Dimension(width, height));		
	}

	public void setAction(String cmd, ActionListener listener ) {
		this.ActionCmd = cmd;
		this.actionlistener = listener;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {		
		actionlistener.actionPerformed(new ActionEvent(this, 0, this.ActionCmd));
	}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
    	entered = true;
		width = height = (int)(originalsize *scalex*2);
		setPreferredSize(new Dimension(width, height));
    	repaint();
    	updateUI();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
    	entered = false;
    	width = height = (int)(originalsize *scalex);
    	setPreferredSize(new Dimension(width, height));
    	repaint();
    	updateUI();    	
	}

	public void setScalesize(int i) {
		// TODO Auto-generated method stub
		hoversize = i;
	}
	
	public void paintComponent(Graphics g) {
		//super.paint(g);
		//super.paintComponent(g);
		Log.d("paint EasyTool");
		Graphics2D graphics2D = (Graphics2D) g;
	   // Set anti-alias for text
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, width, height, this);	
	}
}
