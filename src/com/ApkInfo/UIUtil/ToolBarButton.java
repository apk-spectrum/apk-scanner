package com.ApkInfo.UIUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ToolBarButton extends JButton{
	public ImageIcon  mHoverIcon;
	public ImageIcon  mIcon;

	public ToolBarButton(String text, ImageIcon icon, ImageIcon hoverIcon)
	{
		super(text, icon);
		this.mHoverIcon = hoverIcon;
		this.mIcon = icon;
		
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
