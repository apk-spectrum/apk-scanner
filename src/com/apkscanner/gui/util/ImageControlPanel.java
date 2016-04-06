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

public class ImageControlPanel extends JPanel implements MouseListener{
	int x, y;
	int oldx,oldy;
	int beforx,befory;
	private float scale = 1;
	BufferedImage bi;

	public ImageControlPanel() {
		setBackground(Color.white);		
		addMouseMotionListener(new MouseMotionHandler());
		addMouseListener(this);
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = -0.05f * e.getPreciseWheelRotation();
                scale += delta;
                revalidate();
                repaint();
            }

        });
		
		Image image = null;

//		MediaTracker mt = new MediaTracker(this);
//		mt.addImage(image, 1);
//		try {
//			mt.waitForAll();
//		} catch (Exception e) {
//			System.out.println("Exception while loading image.");
//		}
//
//		if (image.getWidth(this) == -1) {
//			System.out.println("no gif file");
//			System.exit(0);
//		}

		//bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		//Graphics2D big = bi.createGraphics();
		//big.drawImage(image, 0, 0, this);
	}
	
	public void setImage(ImageIcon img) {
		Image image = img.getImage();
		
		bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, this);
		
		x = oldx = 0;
		y = oldy = 0;
		
		beforx = befory = 0;
		
		scale = 1;
		
	}	

	@SuppressWarnings("deprecation")
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;

		AffineTransform at = new AffineTransform();
		
		if(bi!=null) {
	        String text = "Width : " + bi.getWidth() + "      Heiget : " + bi.getHeight();
			g2D.drawChars(text.toCharArray(), 0, text.length(), 10,10);
			Rectangle Rect = g2D.getClipRect();
			at.translate(Rect.getWidth()/2-bi.getWidth()/2 + beforx+x, Rect.getHeight()/2-bi.getHeight()/2 + befory+y);
			at.scale(scale, scale);	        
		}
		 
		g2D.drawImage(bi, at, this);
		
	}

	class MouseMotionHandler extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent e) {
			
			//Log.i("Oldx = "+ oldx + "Oldx = "+ oldx);
			
			//Log.i("x = "+  e.getX() + "y = "+  e.getY());
			
			x = e.getX()- oldx;
			y = e.getY()- oldy;
			
			//Log.i("beforx+x = "+  (beforx+x));
			//Log.i("befory+y = "+  (beforx+y));
			
			
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
		oldx = arg0.getX();
		oldy = arg0.getY();
	}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent arg0) {
		// TODO Auto-generated method stub
        beforx += x;
        befory += y;
	}
}