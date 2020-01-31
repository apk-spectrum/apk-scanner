package com.apkspectrum.resource;

import java.awt.Image;

import javax.swing.ImageIcon;

public interface ResImage<T> extends ResFile<T>
{
	public Image getImage();
	public Image getImage(int w, int h);
	public ImageIcon getImageIcon();
	public ImageIcon getImageIcon(int w, int h);
}
