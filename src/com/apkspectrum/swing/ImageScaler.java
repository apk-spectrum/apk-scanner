package com.apkspectrum.swing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ImageScaler
{
	public static Image getScaledImage(ImageIcon source, int w, int h)
	{
		return getScaledImage(source.getImage(), w, h, true);
	}

	public static Image getScaledImage(ImageIcon source, int w, int h, boolean flush)
	{
		return getScaledImage(source.getImage(), w, h, flush);
	}

	public static Image getScaledImage(Image source, int w, int h)
	{
		return getScaledImage(source, w, h, true);
	}

	public static Image getScaledImage(Image source, int w, int h, boolean flush)
	{
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(source, 0, 0, w, h, null);
		g2.dispose();
		if(flush) source.flush();
		return resizedImg;
	}

	public static ImageIcon getScaledImageIcon(Image source, int w, int h)
	{
		return new ImageIcon(getScaledImage(source, w, h, true));
	}

	public static ImageIcon getScaledImageIcon(Image source, int w, int h, boolean flush)
	{
		return new ImageIcon(getScaledImage(source, w, h, flush));
	}

	public static ImageIcon getScaledImageIcon(ImageIcon source, int w, int h)
	{
		return new ImageIcon(getScaledImage(source.getImage(), w, h, true));
	}

	public static ImageIcon getScaledImageIcon(ImageIcon source, int w, int h, boolean flush)
	{
		return new ImageIcon(getScaledImage(source.getImage(), w, h, flush));
	}

	public static Image getMaintainAspectRatioImage(ImageIcon source, int Maxw, int Maxh) {
		return getMaintainAspectRatioImage(source.getImage(), Maxw, Maxh, true);
	}

	public static Image getMaintainAspectRatioImage(ImageIcon source, int Maxw, int Maxh, boolean flush) {
		return getMaintainAspectRatioImage(source.getImage(), Maxw, Maxh, flush);
	}

	public static Image getMaintainAspectRatioImage(Image source, int Maxw, int Maxh) {
		return getMaintainAspectRatioImage(source, Maxw, Maxh, true);
	}

	public static Image getMaintainAspectRatioImage(Image source, int Maxw, int Maxh, boolean flush) {
		int width = source.getWidth(null);
		int height = source.getHeight(null);

		float scalex = (float)Maxw / (float)width;
		float scaley = (float)Maxh / (float)height;

		float scale = (scalex < scaley) ? scalex : scaley;

		width = (int)((float)scale * (float)width);
		height = (int)((float)scale * (float)height);

		BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(source, 0, 0, width, height, null);
		g2.dispose();
		if(flush) source.flush();
		return resizedImg;
	}

	public static ImageIcon getMaintainAspectRatioImageIcon(Image source, int Maxw, int Maxh) {
		return new ImageIcon(getMaintainAspectRatioImage(source, Maxw, Maxh, true));
	}

	public static ImageIcon getMaintainAspectRatioImageIcon(Image source, int Maxw, int Maxh, boolean flush) {
		return new ImageIcon(getMaintainAspectRatioImage(source, Maxw, Maxh, flush));
	}

	public static ImageIcon getMaintainAspectRatioImageIcon(ImageIcon source, int Maxw, int Maxh) {
		return new ImageIcon(getMaintainAspectRatioImage(source.getImage(), Maxw, Maxh, true));
	}

	public static ImageIcon getMaintainAspectRatioImageIcon(ImageIcon source, int Maxw, int Maxh, boolean flush) {
		return new ImageIcon(getMaintainAspectRatioImage(source.getImage(), Maxw, Maxh, flush));
	}

	@Deprecated
	public static Image getMaxScaledImage(ImageIcon temp, int Maxw, int Maxh)
	{
		return getMaintainAspectRatioImage(temp, Maxw, Maxh, true);
	}

	@Deprecated
	public static Image getMaxScaledImage(ImageIcon temp, int Maxw, int Maxh, boolean flush)
	{
		return getMaintainAspectRatioImage(temp, Maxw, Maxh, flush);
	}
}
