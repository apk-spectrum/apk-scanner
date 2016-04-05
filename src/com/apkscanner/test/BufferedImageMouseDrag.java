//necessary imports
package com.apkscanner.test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class BufferedImageMouseDrag extends JFrame {
	DisplayCanvas canvas;

	public BufferedImageMouseDrag() {
		super();
		Container container = getContentPane();

		canvas = new DisplayCanvas();
		container.add(canvas);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(450, 400);
		setVisible(true);
	}

	public static void main(String arg[]) {
		new BufferedImageMouseDrag();
	}
}

class DisplayCanvas extends JPanel implements MouseListener{
	int x, y;
	int oldx,oldy;
	private float scale = 1;
	BufferedImage bi;

	DisplayCanvas() {
		setBackground(Color.white);
		setSize(450, 400);
		addMouseMotionListener(new MouseMotionHandler());
		addMouseListener(this);
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();
                scale += delta;
                revalidate();
                repaint();
            }

        });
		
		Image image = Resource.IMG_QMG_IMAGE_ICON.getImageIcon().getImage();

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 1);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			System.out.println("Exception while loading image.");
		}

		if (image.getWidth(this) == -1) {
			System.out.println("no gif file");
			System.exit(0);
		}

		bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, this);
	}
	
	public void setImage(ImageIcon img) {
		Image image = img.getImage();
		
		bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, this);		
	}	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;

		AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        at.translate(x, y);
        
		g2D.drawImage(bi, at, this);
	}

	class MouseMotionHandler extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent e) {
			x = e.getX()- oldx;
			y = e.getY()- oldy;
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		oldx = arg0.getX();
		oldy = arg0.getY();	
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}