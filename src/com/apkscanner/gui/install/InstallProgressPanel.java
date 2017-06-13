package com.apkscanner.gui.install;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.util.Log;

public class InstallProgressPanel extends JPanel
{
	private static final long serialVersionUID = 6145481552592676895L;
	
	JPanel ProgressStepPanel;
	JPanel TextStepPanel;
	private final int STEPMAX = 4;
	private final int STEPWIDTH = 500;
	private final int STEPHEIGHT = 70;
	
	private final int COLOR_STEP_NOTFINISH = 0; 
	private final int COLOR_STEP_PROCESSING = 1; 		
	private final int COLOR_STEP_FINISHED = 2;
	private final int COLOR_STEP_ERROR= 3;
	
	int CurrentProgress=0;
	private final String [] outtexts= {"APK VERIFY", "PACKAGE", "INSTALLING", "FINISH"};
	
	private final Color []Colorset = {new Color(222,228,228), new Color(52,152,220),new Color(46,204,114), new Color(0xFF0000)};
	//private final Color ErrorColor = new Color(0xFF7400);		
	
    private EllipseLayout[] ellipselabel = new EllipseLayout[STEPMAX];			
    private Linelayout[] linelabel = new Linelayout[STEPMAX-1];
    private AnimationLabel[] animatlabel = new AnimationLabel[STEPMAX];
    
	public class ColorBase {
		int state;
        public Timer timer = null;
        public Color currentColor;
        public Boolean isAnimation= false;
        private static final int DELAY = 30;
        private static final int INC = 4;
        public Container childContainer;
        
		// disable 223,227,228
		// ing 52,152,219
		// finish 46,204,113
        public int addColorINC(int base, int current) {
        	
        	if(Math.abs(base-current) < 10) {
        		return base;
        	}
        	
        	if(base >= current) {
        		return current+INC;
        	} else {
        		return current-INC;
        	}
        }
        
        public ColorBase(Container child) {
        	state = 0;
        	childContainer = child;
        	currentColor = new Color(222,228,228);
            timer = new Timer(DELAY, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	int r=0,g=0,b=0;	                	
                		r = addColorINC(Colorset[state].getRed() , currentColor.getRed());
                		g = addColorINC(Colorset[state].getGreen() , currentColor.getGreen());
                		b = addColorINC(Colorset[state].getBlue() , currentColor.getBlue());
                	currentColor = new Color(r,g,b);
                	//if(Math.abs(r-Colorset[state].getRed()) < 10 && Math.abs(g-Colorset[state].getGreen()) < 10 && 
                			//Math.abs(b-Colorset[state].getBlue()) < 10) {
                	if(currentColor.equals(Colorset[state]))	{	                		
                		currentColor = Colorset[state];
                		timer.stop();
                		isAnimation = false;
                	}
                	//ColorBase.this.getParent().repaint();
                	if(childContainer!=null)childContainer.getParent().repaint();
                }
            });
        }
	    public void setAnimation() {
	    	isAnimation = true;
	    	currentColor = Colorset[state];
	    	timer.start();
	    }
	}
	
	
	public class EllipseLayout extends JPanel {
		private static final long serialVersionUID = 5831964884908650735L;
		String /*outtext,*/ intext;
		ColorBase colorbase;
		public EllipseLayout() {
			super();
			//outtext = new String("");
			intext = new String("");
			colorbase = new ColorBase(this);				
			colorbase.state = 0;
		}
				
	  public void drawCenteredString(String s, int w, int h, Graphics g) {
		    FontMetrics fm = g.getFontMetrics();
		    int x = (w - fm.stringWidth(s)) / 2;
		    int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
		    g.drawString(s, x, y);
		  }
		
	    public void paintComponent(Graphics g)
	    {	
	        Graphics2D g2 = (Graphics2D)g;
	        		    	
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        
	    	Dimension size = getSize();
	    	if(colorbase.isAnimation) {
	    		g.setColor(colorbase.currentColor);
	    	} else {
	    		g.setColor(Colorset[colorbase.state]);
	    	}
	    	
	    	if(size.getWidth() <= size.getHeight()) {
	    		//g.fillOval(0,(int)(size.getHeight()/2 - size.getWidth()/2), (int)size.getWidth(), (int)size.getWidth());
	    		Shape theCircle = new Ellipse2D.Double(0,(size.getHeight()/2 - size.getWidth()/2), size.getWidth(), size.getWidth());
	    		g2.fill(theCircle);
	    	} else {
	    		
	    	}
	    	//g.setFont(g.getFont().deriveFont(15f));
	    	//g.drawString(outtext, 0, (int)size.getHeight()-10);
	    	g.setFont(g.getFont().deriveFont(20f));
	    	g.setColor(Color.WHITE);
	    	//g.drawString(intext, (int)(size.getWidth()/2-15), (int)(size.getHeight()/2+ 15));
	    	drawCenteredString(intext, (int)size.getWidth(), (int)size.getHeight(), g);
	    	
	    }
	    public void setEllipseText(String str) {
	    	intext = str;
	    }
	    
	    public void setDescriptionText(String str) {
	    	//outtext = str;
	    }
	    
	    public void setState(int state) {
	    	if(colorbase.state == state) return;
	    	colorbase.setAnimation();		    	
	    	colorbase.state = state;
	    }
	}
	
	public class Linelayout extends JLabel {
		private static final long serialVersionUID = 4192134315491972328L;
		ColorBase colorbase;
		
		public Linelayout() {
			super();
			colorbase = new ColorBase(this);
			colorbase.state = 0;
		}
		public void paintComponent(Graphics g)
	    {
			Dimension size = getSize();
			Graphics2D g2 = (Graphics2D) g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        
	    	if(colorbase.isAnimation) {
	    		g.setColor(colorbase.currentColor);
	    	} else {
	    		g.setColor(Colorset[colorbase.state]);
	    	}
			g2.setStroke(new BasicStroke(8) );				
			//g.drawLine(0, (int)(size.getHeight()/2), (int)size.getWidth(), (int)(size.getHeight()/2));
			
			Shape Line = new Line2D.Double(0, size.getHeight()/2, size.getWidth(), size.getHeight()/2);
    		g2.draw(Line);
			
	    }
	    
	    public void setState(int state) {
	    	
	    	if(colorbase.state == state) return;		    	
	    	colorbase.setAnimation();		    	
	    	colorbase.state = state;
	    }
	}
	
	public class AnimationLabel extends JLabel {
		private static final long serialVersionUID = 4192134315491972328L;
		ColorBase colorbase;
		String str;
		public AnimationLabel(String string, int center) {
			
			super(string, center);			
			colorbase = new ColorBase(this);
			colorbase.state = 0;
		}
		
        private void centerString(Graphics g, Rectangle r, String s, 
                Font font) {
            FontRenderContext frc = 
                    new FontRenderContext(null, true, true);

            if(s==null) {
            	s = "";
            }
            
            Rectangle2D r2D = font.getStringBounds(s, frc);
            int rWidth = (int) Math.round(r2D.getWidth());
            int rHeight = (int) Math.round(r2D.getHeight());
            int rX = (int) Math.round(r2D.getX());
            int rY = (int) Math.round(r2D.getY());

            int a = (r.width / 2) - (rWidth / 2) - rX;
            int b = (r.height / 2) - (rHeight / 2) - rY;

            g.setFont(font);
            g.drawString(s, r.x + a, r.y + b);
        }
		
		public void paintComponent(Graphics g)
	    {
			
			Graphics2D g2 = (Graphics2D) g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        
	    	if(colorbase.isAnimation) {
	    		this.setForeground(colorbase.currentColor);
	    	} else {
	    		this.setForeground(Colorset[colorbase.state]);
	    	}
	    	
	    	//centerString(g2, getBounds(), str, new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
	    	super.paintComponent(g);
	    }
	    public void setState(int state) {
	    	
	    	if(colorbase.state == state) return;		    	
	    	colorbase.setAnimation();
	    	colorbase.state = state;
	    }
	    @Override
	    public void setText(String str) {    	
	    	
	    	this.str = str; 
	    	super.setText(str);
	    }

	}
	
	

	public InstallProgressPanel() {
		super(new BorderLayout());
		setPreferredSize(new Dimension(STEPWIDTH, STEPHEIGHT));
		ProgressStepPanel = new JPanel();
		ProgressStepPanel.setLayout(new GridBagLayout());
		ProgressStepPanel.setBackground(Color.WHITE);
		
		
		TextStepPanel = new JPanel();
		TextStepPanel.setLayout(new GridLayout(1,STEPMAX));
		TextStepPanel.setBackground(Color.WHITE);
		
		
        GridBagConstraints gbc = new GridBagConstraints();            
        gbc.fill = GridBagConstraints.BOTH;
		
                
        for(int i=0; i< STEPMAX; i++) {
        	animatlabel[i] = new AnimationLabel(outtexts[i], SwingConstants.CENTER);
        	animatlabel[i].setFont(new Font(animatlabel[i].getFont().getName(), Font.BOLD, 10));
        	animatlabel[i].setForeground(Colorset[COLOR_STEP_NOTFINISH]);
        	//animatlabel[i].setPreferredSize(new Dimension(STEPWIDTH/STEPMAX, 10));
        	//ellipselabel[i].setOpaque(true);
        	//ellipselabel[i].setBackground(new Color(i*50,100,100));
        	TextStepPanel.add(animatlabel[i]);
        }
        
        JPanel marginlabel = new JPanel();
        marginlabel.setBackground(Color.WHITE);			
		ProgressStepPanel.add(marginlabel, addGrid(gbc, 0, 0, 1, 1, 1, 1));            
		for(int i=0;i < STEPMAX-1; i++) {
			ellipselabel[i] = new EllipseLayout();
			ellipselabel[i].setOpaque(true);
			ellipselabel[i].setDescriptionText(outtexts[i]);
			ellipselabel[i].setEllipseText(""+i);
			ProgressStepPanel.add(ellipselabel[i], addGrid(gbc,  i*2+1, 0, 1, 1, 1, 1));
			
			linelabel[i] = new Linelayout();
			linelabel[i].setOpaque(true);
			ProgressStepPanel.add(linelabel[i], addGrid(gbc,  (i*2+2), 0, 1, 1, 2, 1));
		}

		ellipselabel[STEPMAX-1] = new EllipseLayout();
		ellipselabel[STEPMAX-1].setOpaque(true);
		ellipselabel[STEPMAX-1].setDescriptionText(outtexts[STEPMAX-1]);
		ellipselabel[STEPMAX-1].setEllipseText(""+(STEPMAX-1));
		ProgressStepPanel.add(ellipselabel[STEPMAX-1], addGrid(gbc, STEPMAX*2-1, 0, 1, 1, 1, 1));
		
        JPanel marginlabel2 = new JPanel();
        marginlabel2.setBackground(Color.WHITE);
		ProgressStepPanel.add(marginlabel2, addGrid(gbc, STEPMAX*2, 0, 1, 1, 1, 1));			
		
		ProgressStepPanel.setPreferredSize(new Dimension(0, 70));			
		
		add(ProgressStepPanel, BorderLayout.CENTER);
		add(TextStepPanel, BorderLayout.SOUTH);
		
		// set status
		setStatus(ApkInstallWizard.STATUS_INIT);
		
	}
	public void setEllipselabelText(String str, int index, Boolean ischange) {		
    	if(ischange) {    		
    		animatlabel[index].setText(str);
    	} else {
    		animatlabel[index].setText(outtexts[index]);
    	}
	}
	
	
	private void setProgressColor(int state) {
		Log.d("state : " + state);
		
		if(state==0) {
			for(int i=0; i< STEPMAX; i++) {
				ellipselabel[i].setState(COLOR_STEP_NOTFINISH);
				animatlabel[i].setState(COLOR_STEP_NOTFINISH);
			}
			for(int i=0; i< STEPMAX-1; i++) {
				linelabel[i].setState(COLOR_STEP_NOTFINISH);
			}
			return ;
		}
		
		switch(state) {
//		case 2:
//			String lable = targetDevices[0].model;
//			if(targetDevices.length > 1) {
//				lable = String.format("%1$s 외 %2$d", targetDevices[0].model, targetDevices.length-1);
//				//lable = String.format("%2$d beside %1$s", targetDevices[0].model, targetDevices.length);
//			}
//			animatlabel[0].setText(lable);
//			break;
//		case 3:
//			int packCount = 0;
//			if(installedPackage != null) {
//				for(int i = 0; i < installedPackage.length; i++) {
//					if(installedPackage[i] != null) packCount++;
//				}
//			}
//			animatlabel[1].setText(packCount > 0 ? packCount + " DEVICE" : "NOTHING");
//			break;
//		case 4:
//			animatlabel[2].setText((flag & FLAG_OPT_INSTALL) != 0 ? "INSTALL" : "PUSH");
//			break;
//		default:
//			break;
		}
		
		for(int i=1; i <= state; i++) {
			ellipselabel[i-1].setState(COLOR_STEP_FINISHED);
			animatlabel[i-1].setState(COLOR_STEP_FINISHED);
			if(i!=state)linelabel[i-1].setState(COLOR_STEP_FINISHED);
		}
		for(int i=state; i< STEPMAX; i++) {
			ellipselabel[i].setState(COLOR_STEP_NOTFINISH);
			animatlabel[i].setState(COLOR_STEP_NOTFINISH);
			animatlabel[i-1].setText(outtexts[i-1]);
		}
		
		
		if(state -1 < STEPMAX-1) linelabel[state-1].setState(COLOR_STEP_NOTFINISH);
		ellipselabel[state-1].setState(COLOR_STEP_PROCESSING);
		animatlabel[state-1].setState(COLOR_STEP_PROCESSING);			
	}
	
	public void setStatus(int status) {		
		int newStatus = CurrentProgress;
		switch(status) {
		case ApkInstallWizard.STATUS_INIT:
			newStatus = 0;
			break;
		case ApkInstallWizard.STATUS_PACKAGE_SCANNING:
			newStatus = 1;
			break;
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			newStatus = 2;
			break;
		case ApkInstallWizard.STATUS_INSTALLING:
			newStatus = 3;
			break;
		case ApkInstallWizard.STATUS_COMPLETED:
			newStatus = 4;
			break;
		case ApkInstallWizard.STATUS_APK_VERTIFY_ERROR:
			ellipselabel[CurrentProgress-1].setState(COLOR_STEP_ERROR);
			animatlabel[CurrentProgress-1].setState(COLOR_STEP_ERROR);
			break;
		default:
			break;
		}
		if(CurrentProgress != newStatus) {
			CurrentProgress = newStatus;
			setProgressColor(CurrentProgress);
		}
	}
    
    private GridBagConstraints addGrid(GridBagConstraints gbc, 
            int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
      gbc.gridx = gridx;
      gbc.gridy = gridy;
      gbc.gridwidth = gridwidth;
      gbc.gridheight = gridheight;
      gbc.weightx = weightx;
      gbc.weighty = weighty;
      return gbc;
    }
}
