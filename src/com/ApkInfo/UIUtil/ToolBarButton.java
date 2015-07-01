package com.ApkInfo.UIUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ToolBarButton extends JButton{
	public ImageIcon  mEntericon;
	public ImageIcon  mExiticon;

	public ToolBarButton(String text, ImageIcon Entericon, ImageIcon Exiticon)
	{
		super(text, Exiticon);
		this.mEntericon = Entericon;
		this.mExiticon = Exiticon;
		
        this.addMouseListener(new MouseAdapter()
        {
            public void mouseEntered(MouseEvent evt)
            {
            	setIcon(mEntericon);

            }
            public void mouseExited(MouseEvent evt)
            {
            	setIcon(mExiticon);
            }
        });
	}
}
