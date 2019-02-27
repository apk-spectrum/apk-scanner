package com.apkscanner.gui.easymode.contents;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.util.ImageUtils;
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
    public int getBaseline(int width, int height) {
        return 0;
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
    	
		width = height = (int)(originalsize * scalex*2);
		setPreferredSize(new Dimension(width, height));
    	repaint();
    	updateUI();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
    	entered = false;
    	width = height = (int)(originalsize * scalex);
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
		super.paintComponent(g);
		//Log.d("paint EasyTool");
		Graphics2D graphics2D = (Graphics2D) g;
	   // Set anti-alias for text
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		graphics2D.drawImage(image, 0, 0, width, height, this);
		
		Log.d(getBounds().toString());
		
//	      BufferedImage myImage = ImageUtils.imageToBufferedImage(image);
//
//	      BufferedImage filteredImage = new BufferedImage(myImage.getWidth(null), myImage
//	          .getHeight(null), BufferedImage.TYPE_INT_ARGB);
//
//	      Graphics g1 = filteredImage.getGraphics();
//	      g1.drawImage(myImage, myImage.getWidth(), myImage.getHeight(), null);
//
//	      float weight = 1.0f;
//	      
//	      float[] blurKernel = { weight / 9f, weight / 9f, weight / 9f, weight / 9f, weight / 9f, weight / 9f, weight / 9f, weight / 9f, weight / 9f };
//
//
//
//		Map map = new HashMap();
//		
//		map.put(RenderingHints.KEY_INTERPOLATION,
//		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		
//		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		
//		map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		
//		RenderingHints hints = new RenderingHints(map);
//		BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
//	      
//	    //  BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
//	      myImage = blur.filter(myImage, null);
//	      g1.dispose();
//		graphics2D.drawImage(myImage, 0, 0, width, height, this);	
	}
}
