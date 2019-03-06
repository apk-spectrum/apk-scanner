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
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.contents.EasyToolIcon.EasyToolListner;
import com.apkscanner.gui.easymode.util.GraphicUtil;
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
	AnimationTask task;
	final int ANIMATION_UP_VALUE = 3;
	final int ANIMATION_DOWN_VALUE = -3;
	
	final int ANIMATION_DELAY =5;
	EasyToolListner eventlistner;
	String text ="";
	
	public interface EasyToolListner {
		public static int STATE_ANIMATION_END = 0;
		public static int STATE_ENTER = 1;
		public static int STATE_EXIT = 2;
		
		void changestate(int state, EasyToolIcon easyiconlabel);
		
	}
	
	class AnimationTask extends TimerTask {
    	EasyToolIcon toolicon;
    	int Animatevalue;
    	public AnimationTask(EasyToolIcon easyToolIcon, int value) {
			// TODO Auto-generated constructor stub
    		this.toolicon = easyToolIcon;
    		this.Animatevalue = value;
		}
    	
        public void run() {
    		width+=Animatevalue;
    		height+=Animatevalue;    		
    		setPreferredSize(new Dimension(width, height));
        	//repaint();
        	updateUI();
        	if(hoversize <= width || width <=originalsize) {
        		if(eventlistner != null && entered)eventlistner.changestate(EasyToolListner.STATE_ANIMATION_END, toolicon );
        		this.cancel();
        	}
        }
      };
	

	public EasyToolIcon(ImageIcon imageIcon, int size) {
		// TODO Auto-generated constructor stub
		image = imageIcon.getImage();
		width = height = originalsize = size;
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
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
//		width = height = hoversize - 5;
//    	setPreferredSize(new Dimension(width, height));
//    	updateUI();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseExited(e);
		
    	//task = new AnimationTask(this, ANIMATION_DOWN_VALUE);
        //  Timer timer = new Timer();
        //  timer.schedule(task, 0, ANIMATION_DELAY);
		//if(task != null) task.cancel();
		//width = height = originalsize;
		setPreferredSize(new Dimension(originalsize, originalsize));
		updateUI();
		
		actionlistener.actionPerformed(new ActionEvent(this, 0, this.ActionCmd));
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		//Log.d("mouse Enter");
    	entered = true;
    	eventlistner.changestate(EasyToolListner.STATE_ENTER, this);
		
//		setPreferredSize(new Dimension(width, height));
//    	repaint();
//    	updateUI();  	
    	width = height = originalsize;
    	
    	if(task != null) task.cancel();
        task = new AnimationTask(this, ANIMATION_UP_VALUE);
          Timer timer = new Timer();
          timer.schedule(task, 0, ANIMATION_DELAY);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		//Log.d("mouse Exit");
    	entered = false;
    	eventlistner.changestate(EasyToolListner.STATE_EXIT, this);    	
    	
    	width = height = hoversize;
    	if(task != null) task.cancel();    	
    	task = new AnimationTask(this, ANIMATION_DOWN_VALUE);
        Timer timer = new Timer();
        timer.schedule(task, 0, ANIMATION_DELAY);
    	
//    	task.cancel();
//    	width = height = (int)(originalsize * scalex);
//    	setPreferredSize(new Dimension(width, height));
//    	//repaint();
//    	updateUI();
	}

	public void setScalesize(int i) {
		// TODO Auto-generated method stub
		hoversize = i;
	}
	
	public void paintComponent(Graphics g) {
		//super.paint(g);
		super.paintComponent(g);
		Log.d("width : " + this.getEasyText() + ":" + width);
		Graphics2D graphics2D = (Graphics2D) g;
	   // Set anti-alias for text
		
		
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);		
		graphics2D.drawImage(image, 0, 0, width, height, this);
				
//		Log.d(getBounds().toString());
		
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

	public void setEasyToolListner(EasyToolListner easyToolListner) {
		// TODO Auto-generated method stub
		this.eventlistner = easyToolListner;
	}

	public void setEasyText(String str) {
		text = str;
	}
	public String getEasyText() {
		return text;
	}
}
