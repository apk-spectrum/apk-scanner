package com.apkspectrum.swing;

import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;

public class NoCloseCheckBoxMenuItem extends JCheckBoxMenuItem {
	private static final long serialVersionUID = 4982874784585596549L;

	public NoCloseCheckBoxMenuItem() {
    	super();
    }

    @Override
    protected void processMouseEvent(MouseEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
            doClick();
            setArmed(true);
        } else {
            super.processMouseEvent(evt);
        }
    }
}
