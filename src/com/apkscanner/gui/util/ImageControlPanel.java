package com.apkscanner.gui.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.annotation.Resources;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class ImageControlPanel extends JPanel implements MouseListener{
	private static final long serialVersionUID = -391185152837196160L;
	
	int x, y;
	int beforx,befory;
	private float scale = 1;
	BufferedImage bi;
	Image imageBackground;
	public ImageControlPanel() {
		setBackground(Color.white);		
		addMouseMotionListener(new MouseMotionHandler());
		addMouseListener(this);
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = -0.1f * e.getPreciseWheelRotation();
                scale += delta;
                revalidate();
                repaint();
            }            
        });
		
		//Image image = null;
		
		imageBackground = Resource.IMG_RESOURCE_IMG_BACKGROUND.getImageIcon().getImage();
		
		
		
	}
	
	public void setImage(ImageIcon img) {
		Image image = img.getImage();
		
		bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		
		//imageBackground = new ImageIcon(Resource.IMG_RESOURCE_IMG_BACKGROUND.getImageIcon()).getImage();
				
		
		big.drawImage(imageBackground, 0, 0, this);
		big.drawImage(image, 0, 0, this);
		
		x = 0;
		y = 0;
		
		beforx = befory = 0;
		
		scale = 1;
		
	}	

	@SuppressWarnings("deprecation")
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;

		AffineTransform at = new AffineTransform();
		
		if(bi!=null) {
	        String text = "W : " + bi.getWidth() + "      H : " + bi.getHeight();
	        g2D.setColor(Color.WHITE);
	        g2D.drawChars(text.toCharArray(), 0, text.length(), 10,10);
			
			Rectangle Rect = g2D.getClipRect();
			at.translate(Rect.getWidth()/2-bi.getWidth()/2 + x, Rect.getHeight()/2-bi.getHeight()/2 + y);
			at.scale(scale, scale);
		}
		 
		g2D.drawImage(bi, at, this);
		
	}

	class MouseMotionHandler extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent e) {
			
			x += ( e.getX()- beforx);
			y += ( e.getY()- befory);
			
			beforx = e.getX();
			befory = e.getY();
			
			repaint();
		}
	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub
		beforx = arg0.getX();
		befory = arg0.getY();
	}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub
	}
}