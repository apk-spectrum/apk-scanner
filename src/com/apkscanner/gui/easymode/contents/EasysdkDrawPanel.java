package com.apkscanner.gui.easymode.contents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.util.Log;

public class EasysdkDrawPanel extends FlatPanel {
	private final int ARR_SIZE = 6;
	
	private int minsdkVersion;
	private int targetsdkVersion;
	private int OnlineDeviceVersion;
		
	private final Color linecolor = new Color(128, 100, 162);
	private final Color textcolor = new Color(127, 127, 127);
	private final Color textallowcolor = new Color(178, 53, 50);
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

    
    private void drawmin(Graphics2D g2, int version, Rectangle rect) {
    	
    	
    	drawCenteredString(g2, "min", rect, new Font(getFont().getName(), Font.BOLD, 20));
    	    	
//	    int[] nXPoints = new int[] { 0,0,5};
//	    int[] nYPoints = new int[] { 0, 10,5};
//
//	    g2.setColor(textallowcolor);
//	    //g2.fillPolygon(nXPoints, nYPoints, 3);
		
    }
    
    private void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
    	int trianglesize  = font.getSize();
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setColor(textcolor);
        g.setFont(font);
        // Draw the String
        g.drawString(text, 0, y);
        
        int trianglex = metrics.stringWidth(text) + 0;
        int triangley = y - metrics.getAscent();
        
        int[] nXPoints = new int[] { trianglex, trianglex, trianglex + trianglesize/2};
	    int[] nYPoints = new int[] { triangley, y, triangley + metrics.getAscent()/2};

	    g.setColor(textallowcolor);
	    g.fillPolygon(nXPoints, nYPoints, 3);
	    
	    
	    g.setFont(new Font(getFont().getName(), Font.BOLD, 15));
	    g.setColor(textcolor);
	    g.drawString("23", trianglex + trianglesize/2 + 10, y - 2);
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
        
        //g2.setTransform(null);
        
        g2.setTransform(at);
        AffineTransform tt = AffineTransform.getTranslateInstance(0, 30);
        g2.transform(tt);
        
        
        
        //g2.setTransform(at);
        drawmin(g2, 23, new Rectangle(0, 0, getWidth(), 20));
        
        
        
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
