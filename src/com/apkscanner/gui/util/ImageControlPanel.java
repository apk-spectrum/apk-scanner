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

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.apkscanner.resource.Resource;

public class ImageControlPanel extends JPanel implements MouseListener{
	private static final long serialVersionUID = -391185152837196160L;
	
	int x, y;
	int beforx,befory;
	private float scale = 1;
	BufferedImage bi;
	BufferedImage bgbi;

	public ImageControlPanel() {
		setBackground(Color.white);		
		addMouseMotionListener(new MouseMotionHandler());
		addMouseListener(this);
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = (-e.getPreciseWheelRotation() * 0.05 + 1);
                scale *= delta;
                
                if(scale > 20) scale = 20f;
                else if(scale < 0.02f) scale = 0.02f;
                
                revalidate();
                repaint();
            }
        });
	}

	public void setImage(ImageIcon img) {
		Image image = img.getImage();
		
		bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, this);

		x = 0;
		y = 0;

		beforx = befory = 0;
		scale = 1;
	}
	
	public BufferedImage getBackgroundImage(int width, int height) {
		if(bgbi == null) {
	        Image imageBackground = Resource.IMG_RESOURCE_IMG_BACKGROUND.getImageIcon().getImage();
			bgbi = new BufferedImage(imageBackground.getWidth(this), imageBackground.getHeight(this), BufferedImage.TYPE_INT_ARGB);
			bgbi.createGraphics().drawImage(imageBackground, 0, 0, this);
		}
		
		while(bgbi.getWidth() < width) {
			BufferedImage newbgbi = new BufferedImage(bgbi.getWidth()*3, bgbi.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D big = newbgbi.createGraphics();
			
	        int w = newbgbi.getWidth();  
	        for (int x = 0; x < w; x += bgbi.getWidth()) {  
	            big.drawImage(bgbi, x, 0, this); 
	        }  
	        bgbi = newbgbi;
		}
		while(bgbi.getHeight() < height) {
			BufferedImage newbgbi = new BufferedImage(bgbi.getWidth(), bgbi.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
			Graphics2D big = newbgbi.createGraphics();
 
	        int h = newbgbi.getHeight();  
  	        for (int y = 0; y < h; y += bgbi.getHeight()) {  
  	            big.drawImage(bgbi, 0, y, this);  
	        }  
	        bgbi = newbgbi;
		}

		BufferedImage bg = bgbi.getSubimage((int)(bgbi.getWidth()/2 - width/2), 
                (int)(bgbi.getHeight()/2 - height/2),
                width,
                height);

		return bg;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;

		AffineTransform at = new AffineTransform();
		if(bi!=null) {
			Rectangle Rect = g2D.getClipBounds();
			at.translate((Rect.getWidth()-bi.getWidth() * scale)/2 + x, (Rect.getHeight()-bi.getHeight() * scale)/2 + y);
	        BufferedImage bg = getBackgroundImage((int)(bi.getWidth() * scale), (int)(bi.getHeight() * scale)); 
			g2D.drawImage(bg, at, this);
	        at.scale(scale, scale);
			String text = "W : " + bi.getWidth() + "      H : " + bi.getHeight() + "  " + Math.round(scale * 100) + "%";		
			g2D.drawImage(bi, at, this);
	        g2D.setColor(Color.WHITE);
	        g2D.drawChars(text.toCharArray(), 0, text.length(), 10,10);
		}
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