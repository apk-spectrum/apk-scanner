package com.apkscanner.gui.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ImageScaler
{
    public static Image getScaledImage(ImageIcon temp, int w, int h)
    {
		Image srcImg = temp.getImage();
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		return resizedImg;
    }
    
	public static Image getMaxScaledImage(ImageIcon temp, int Maxw, int Maxh)
	{
		Image srcImg = temp.getImage();
		
		int width = temp.getIconWidth();
		int height = temp.getIconHeight();
		
		float scalex = (float)Maxw / (float)width;
		float scaley = (float)Maxh / (float)height;
		
		float scale = (scalex < scaley) ? scalex : scaley;
		
		width = (int)((float)scale * (float)width);
		height = (int)((float)scale * (float)height);
		
		BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, width, height, null);
		g2.dispose();
		return resizedImg;
	}
}
