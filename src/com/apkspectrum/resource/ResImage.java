package com.apkspectrum.resource;

import java.awt.Image;

import javax.swing.ImageIcon;

public interface ResImage<T> extends ResFile<T>
{
	public Image getImage();
	public ImageIcon getImageIcon();
}
