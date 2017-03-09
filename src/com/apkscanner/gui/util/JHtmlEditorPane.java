package com.apkscanner.gui.util;

import java.awt.AlphaComposite;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.apkscanner.util.Log;

public class JHtmlEditorPane extends JEditorPane implements HyperlinkListener
{
	private static final long serialVersionUID = 7856109068620039501L;
	
	private String head = null;
	private String body = null;

	private String tooltip;
	
	private StyleSheet ssh;
	private Image backgroundimg = null;
    private boolean mouseIn;
    
    private final int RADIUS = 30;
    private int iw;
    private int ih;
    private int x;
    private int y;
    
	public abstract interface HyperlinkClickListener {
		public abstract void hyperlinkClick(String id);
	}
	
	private HyperlinkClickListener hyperlinkClickListener;
	
	public JHtmlEditorPane()
	{
		this("", "", "");
	}
	
	public JHtmlEditorPane(String head, String style, String body)
	{
		super("text/html", "");
		addHyperlinkListener(this);
		
        HTMLEditorKit kit = new HTMLEditorKit();
        setEditorKit(kit);
        
        ssh = kit.getStyleSheet();
        ssh.addRule(style);
        setOpaque(false);
        setHtml(head, body);
        
        addMouseMotionListener(new MyMouseAdapter());
        addMouseListener(new MyMouseAdapter());
	}
	public void setBackgroundImg(Image img) {
		this.backgroundimg = img;
	}
	
    @Override
    protected void paintComponent(Graphics g) {

        
    	
        if(backgroundimg!=null) {
        	
//            Graphics2D g2d = (Graphics2D) g.create();
//
//            int midX = (getWidth() - iw) / 2;
//            int midY = (getHeight() - ih) / 2;
//
//            BufferedImage bi = new BufferedImage(getWidth(),
//                    getHeight(), BufferedImage.TYPE_INT_ARGB);
//            Graphics2D bigr = bi.createGraphics();
//
//            if (mouseIn) {
//                bigr.setPaint(Color.white);
//                bigr.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2,
//                        RADIUS * 2);
//                bigr.setComposite(AlphaComposite.SrcAtop);
//                
//                bigr.drawImage(backgroundimg, midX, midY, iw, ih, this);
//            }
//
//            
//            bigr.setComposite(AlphaComposite.SrcOver.derive(1.0f));
//            bigr.drawImage(backgroundimg, midX, midY, iw, ih, this);
//            bigr.dispose();
//
//            g2d.drawImage(bi, 0, 0, getWidth(), getHeight(), this);
//            
//            g2d.dispose();
//            
            
        	
            super.paintComponent(g);
            
        	//doDrawing(g);
        	
            
            Graphics2D g2d = (Graphics2D) g;
	        AlphaComposite acomp = AlphaComposite.getInstance(
	                AlphaComposite.SRC_OVER, 0.2f);
	        g2d.setComposite(acomp);
	        g2d.drawImage(backgroundimg, 50, 10, null);
	        
	        acomp = AlphaComposite.getInstance(
	                AlphaComposite.SRC_OVER, 1.0f);
	        g2d.setComposite(acomp);
	        
            
	    }
        else {
        	super.paintComponent(g);
        }

        
        //doDrawing(g);
    }
	
    @SuppressWarnings("unused")
	private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        int midX = (getWidth() - iw) / 2;
        int midY = (getHeight() - ih) / 2;

        BufferedImage bi = new BufferedImage(getWidth(),
                getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bigr = bi.createGraphics();

        if (mouseIn) {
            //bigr.setPaint(Color.white);
            bigr.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2,
                    RADIUS * 2);
            bigr.setComposite(AlphaComposite.SrcAtop);
            bigr.drawImage(backgroundimg, midX, midY, iw, ih, this);
        }

        bigr.setComposite(AlphaComposite.SrcOver.derive(0.1f));
        bigr.drawImage(backgroundimg, midX, midY, iw, ih, this);
        bigr.dispose();

        g2d.drawImage(bi, 0, 0, getWidth(), getHeight(), this);

        g2d.dispose();
    }
    
	public void setHtml(String head, String body) 
	{
		if(head != null && body != null && head.equals(this.head) && body.equals(this.body)) {
			Log.v("same content to pre");
			return;
		}
		this.head = head;
		this.body = body;

		setText(makeHtml(head, body));
	}
	
	public void setHead(String head)
	{
		setHtml(head, body);
	}
	
	public void setStyle(String style)
	{
        ssh.addRule(style);
	}
	
	public void setBody(String body)
	{
		setHtml(head, body);
	}
	
	private static String makeHtml(String head, String body)
	{
		return "<html><head>" + head + "</head><body>" + body + "</body></html>";
	}
	
	public void setHyperlinkClickListener(HyperlinkClickListener listener)
	{
		hyperlinkClickListener = listener;
	}

   private class MyMouseAdapter extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            mouseIn = false;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            mouseIn = true;
        }

        @Override
        public void mouseMoved(MouseEvent e) {

            x = e.getX();
            y = e.getY();

            repaint();
        }
    }
	
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
    	JEditorPane editor = (JEditorPane) e.getSource();

        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
        	if(e.getDescription().isEmpty()) return;
        	if(!e.getDescription().startsWith("@")) {
	        	try {
					Desktop.getDesktop().browse(new URI(e.getURL().toString()));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
        	} else {
        		Element elem = e.getSourceElement();
        		if (elem != null) {
            		AttributeSet attr = elem.getAttributes();
            		AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
            		if (a != null && hyperlinkClickListener != null) {
           				hyperlinkClickListener.hyperlinkClick((String) a.getAttribute(HTML.Attribute.ID));
            		}
        		}
        	}
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        	tooltip = editor.getToolTipText();
        	Element elem = e.getSourceElement();
        	if (elem != null) {
        		AttributeSet attr = elem.getAttributes();
        		AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
        		if (a != null && a.getAttribute(HTML.Attribute.TITLE) != null) {
        			String htmlToolTip = "<html><body>" + ((String) a.getAttribute(HTML.Attribute.TITLE)).replaceAll("\n", "<br/>") + "</body></html>";
        			editor.setToolTipText(htmlToolTip);
        		}
        	}
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
        	editor.setToolTipText(tooltip);
        }
    }
}
