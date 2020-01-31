package com.apkscanner.gui.easymode.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.apkspectrum.util.Log;

public class ImageSliderPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 2003631789889579741L;
	
	private Image img;
	private int imgwidth, imageheight;
	private ArrayList<EasyRoundButton> arrayroundbutton = new ArrayList<EasyRoundButton>();
	private AnimationTask task;
	private NextAnimationTask NextAnimationTask;
	private int currentviewportindex = 0; 
	private int nextindex;
	private int imgcount = 0;
	private int SEEK_HEIGHT = 30;
	
	private int NEXT_VAL = 25;
	private int NEXT_DEVIDE = 10;
	
	private JPanel seekpanel;
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
			
			NextAnimationTask = new NextAnimationTask(currentviewportindex, nextindex, NEXT_VAL);
			Timer timer = new Timer();
			timer.schedule(NextAnimationTask, 0, NEXT_DEVIDE);
		}
	};

	class NextAnimationTask extends TimerTask {
		int Animatevalue;
		
		Point nextpoint;
		//Point currentpoint;
		int interval;

		public NextAnimationTask(int current, int next, int value) {
			// TODO Auto-generated constructor stub
			this.Animatevalue = value;
			nextindex = next;
			interval = (next * getWidth() - current * getWidth()) / value ;
			//Log.d("interval : " + interval );
		}

		public void run() {
			
			currentpoint.x += interval;
			
			repaint();
    					
        	if(currentpoint.x >= imgwidth || 
        			((interval < 0) && (imgwidth * nextindex) > (imgwidth * currentviewportindex) + currentpoint.x)) {
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
		setLayout(new BorderLayout());
		seekpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		seekpanel.add(makeseekbutton());
		
		add(seekpanel, BorderLayout.SOUTH);
		
		//sumImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		sumImg = ImageUtils.imageToBufferedImage(imgtemp.getImage());
		currentpoint = new Point();
		//add(imageset, BorderLayout.CENTER);
 
		setDoubleBuffered(true);
	}

	private JComponent makeseekbutton() {
		EasyRoundButton button = new EasyRoundButton("    ");
		button.setBackground(Color.GRAY);
		button.setActionCommand(imgcount+"");
		button.addActionListener(this);
		button.setshadowlen(3);
		arrayroundbutton.add(button);
		return button;
	}
	private void setseekbar() {
		EasyRoundButton button;
		
		for(int i=0;i< arrayroundbutton.size(); i++) {
			button = arrayroundbutton.get(i);
			if(i == currentviewportindex) {				
				button.setBackground(Color.DARK_GRAY);
			} else {
				button.setBackground(Color.GRAY);
			}
		}
		
		
		
	}

	
	public void add(ImageIcon imgicon) {
		//imageset.add(new JLabel(imgicon));
		sumImg = ImageUtils.joinBufferedImage(sumImg, ImageUtils.imageToBufferedImage(imgicon.getImage()));
		imgcount++;
		seekpanel.add(makeseekbutton());		
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
			Dimension size = new Dimension(imgwidth, imageheight+SEEK_HEIGHT);
			setMinimumSize(size);
			setMaximumSize(size);
			setPreferredSize(size);
		}
	}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setseekbar();
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
		if(e.getSource() instanceof EasyRoundButton) {
			nextindex = Integer.parseInt(e.getActionCommand());
			task.cancel();
			if(NextAnimationTask != null) NextAnimationTask.cancel();
			
			task = new AnimationTask();
			Timer timer = new Timer();
			timer.schedule(task, 3000, 3000);
			
			Log.d("start: " + currentviewportindex + "  to : " + nextindex);
			
			NextAnimationTask = new NextAnimationTask(currentviewportindex, nextindex, NEXT_VAL);
			Timer timerb = new Timer();
			timerb.schedule(NextAnimationTask, 0, NEXT_DEVIDE);
			
		}
	}
	
	public void clean() {
		if(task != null) task.cancel();
		if(NextAnimationTask != null) NextAnimationTask.cancel();
	}
}
