package com.apkscanner.gui.easymode.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.apkscanner.util.Log;

public class ImageSliderPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 2003631789889579741L;
	
	private Image img;
	private int imgwidth, imageheight;
	private AnimationTask task;
	private NextAnimationTask NextAnimationTask;
	private int currentviewportindex = 0;
	private int imgcount = 0;
	Point currentpoint;
	BufferedImage sumImg;
		
	class AnimationTask extends TimerTask {
		int Animatevalue;

		public AnimationTask() {
			// TODO Auto-generated constructor stub			
		}

		public void run() {
			Log.d("run animation task");
			
			int nextindex;
			
						
			if((currentviewportindex + 1) > imgcount) {
				nextindex = 0;
			} else {
				nextindex = currentviewportindex + 1; 
			}			
			
			
			//Log.d(currentviewportindex + ":" +nextindex + ":" + imgcount);
			
			NextAnimationTask = new NextAnimationTask(currentviewportindex, nextindex, 34);
			Timer timer = new Timer();
			timer.schedule(NextAnimationTask, 0, 10);
		}
	};

	class NextAnimationTask extends TimerTask {
		int Animatevalue;
		int nextindex;
		Point nextpoint;
		//Point currentpoint;
		int interval;

		public NextAnimationTask(int current, int next, int value) {
			// TODO Auto-generated constructor stub
			this.Animatevalue = value;
			nextindex = next;
			interval = (next * getWidth() - current * getWidth()) / value ;			
		}

		public void run() {
			
			currentpoint.x += interval;
			
			repaint();
    					
        	if(currentpoint.x >= imgwidth || currentpoint.x < -(imgwidth * currentviewportindex)) {
        		Log.d("End NextAnimationTask " + currentviewportindex + " : " + nextindex );
        		currentviewportindex = nextindex;
        		currentpoint.x = 0;
        		//Log.d("End run next animation task");
        		repaint();
        		this.cancel();
        	}
		}
	};

	public ImageSliderPanel(ImageIcon imgtemp) {
		imgwidth = imgtemp.getIconWidth();
		imageheight= imgtemp.getIconHeight();
		setImage(imgtemp.getImage());
		//setLayout(new BorderLayout());


		//sumImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		sumImg = ImageUtils.imageToBufferedImage(imgtemp.getImage());
		currentpoint = new Point();
		//add(imageset, BorderLayout.CENTER);
 
		setDoubleBuffered(true);
	}


	
	public void add(ImageIcon imgicon) {
		//imageset.add(new JLabel(imgicon));
		sumImg = ImageUtils.joinBufferedImage(sumImg, ImageUtils.imageToBufferedImage(imgicon.getImage()));
		imgcount++;
		//Log.d("count : " + imgcount);
	}

	public void start() {
		task = new AnimationTask();
		Timer timer = new Timer();
		timer.schedule(task, 3000, 3000);
	}

	public void setImage(Image imgtemp) {
		img = imgtemp;
		if (img != null) {
			Dimension size = new Dimension(imgwidth, imageheight);
			setMinimumSize(size);
			setMaximumSize(size);
			setPreferredSize(size);
		}
	}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawImage(g);
    }

    private void drawImage(Graphics g) {
		if(currentpoint != null) {
			int sx = currentviewportindex * imgwidth + currentpoint.x;
			//Log.d(currentpoint.x+"   " + "sx = " + sx);
			g.drawImage(sumImg, 0, 0, imgwidth, imageheight, 
					sx, 0, sx + imgwidth, imageheight, this);
			
		}		
		Toolkit.getDefaultToolkit().sync();
    }


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
