package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class GUI extends JFrame {
    protected GUI() {
        super("Simple Rearrangeable List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //createPanel();
        setBounds(10, 10, 350, 500);
        setVisible(true);
    }

    public static void main(String[] argv) {
    	JEditorPane jep = new JEditorPane("text/html", "The rain in <a href='http://foo.com/'>"
    	+"Spain</a> falls mainly on the <a href='http://bar.com/'>plain</a>.");
    	jep.setEditable(false);
    	jep.setOpaque(false);
    	jep.addHyperlinkListener(new HyperlinkListener() {
    	public void hyperlinkUpdate(HyperlinkEvent hle) {
    	if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
    	System.out.println(hle.getURL());
    	}
    	}
    	});
    	  
    	JPanel p = new JPanel();
    	p.add( new JLabel("Foo.") );
    	p.add( jep );
    	p.add( new JLabel("Bar.") );
    	 
    	JFrame f = new JFrame("HyperlinkListener");
    	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	f.getContentPane().add(p, BorderLayout.CENTER);
    	f.setSize(400, 150);
    	f.setVisible(true);
    	}
}