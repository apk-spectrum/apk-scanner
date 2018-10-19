package com.apkscanner.gui.easymode.contents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.apkscanner.gui.easymode.util.FlatPanel;

public class EasysdkDrawPanel extends FlatPanel {
	private final int ARR_SIZE = 6;
	
	private int minsdkVersion;
	private int targetsdkVersion;
	private int OnlineDeviceVersion;
	
	private final Color linecolor = new Color(128, 100, 162);
	public EasysdkDrawPanel() {
		
	}

    void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,0));
        g.drawLine(0, 0, len-4, 0);
        
        g.drawLine(len, 0, len-ARR_SIZE, -ARR_SIZE);
        
        g.drawLine(len-ARR_SIZE, ARR_SIZE, len, 0);
        //g.drawLine(0, 0, len-4, 0);
        
        
//        g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
//                      new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
    }

    
    private void drawmin(Graphics2D g, int version) {
    	
    	
    	
    	
    	
    }
    
	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D)g;
        
		//setBackground(Color.BLACK); // set background color for this JPanel
		//setForeground(Color.BLACK);
		super.paintComponent(g2); // paint parent's background		
    	
        RenderingHints rh = new RenderingHints(
                 RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        
		AffineTransform at = g2.getTransform();
		
		g2.setColor(linecolor);
        drawArrow(g2, 50, 10, 50, 160);
        
        g2.setTransform(at);
		g2.setFont(new Font(getFont().getName(), Font.PLAIN, 14));
		g2.drawString("min", 0, 30);
        
        //g2.setTransform(at);
        //g.drawOval(0, 0, 10, 10);
		// Your custom painting codes. For example,
//		// Drawing primitive shapes
//		g.setColor(Color.YELLOW); // set the drawing color
//		
//		g.drawOval(150, 180, 10, 10);
//		g.drawRect(200, 210, 20, 30);
//		g.setColor(Color.RED); // change the drawing color
//		g.fillOval(300, 310, 30, 50);
//		g.fillRect(400, 350, 60, 50);
//		// Printing texts
//		g.setColor(Color.WHITE);
//		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
//		g.drawString("Testing custom drawing ...", 10, 20);
	}

}
