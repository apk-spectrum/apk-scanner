package com.apkscanner.gui.util;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ToolBarButton extends JButton{
	private static final long serialVersionUID = -6788392217820751244L;

	public ImageIcon  mHoverIcon;
	public ImageIcon  mIcon;

	public ToolBarButton(String text, ImageIcon icon, ImageIcon hoverIcon, ActionListener listener)
	{
		super(text, icon);
		this.mHoverIcon = hoverIcon;
		this.mIcon = icon;
		
    	setVerticalTextPosition(JLabel.BOTTOM);
    	setHorizontalTextPosition(JLabel.CENTER);
    	setBorderPainted(false);
    	setOpaque(false);
    	setFocusable(false);
    	addActionListener(listener);

		setPreferredSize(new Dimension(63,65));
		
        this.addMouseListener(new MouseAdapter()
        {
            public void mouseEntered(MouseEvent evt)
            {
            	setIcon(mHoverIcon);
            }
            public void mouseExited(MouseEvent evt)
            {
            	setIcon(mIcon);
            }
        });
	}
}
