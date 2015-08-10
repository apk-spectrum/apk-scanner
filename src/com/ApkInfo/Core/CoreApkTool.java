package com.ApkInfo.Core;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class CoreApkTool
{
	public static ArrayList<String> findFiles(File f, String subffix, String filterRegex)
	{
		ArrayList<String> tempList = new ArrayList<String>();
		File[] list = f.listFiles();
		if(list == null) {
			System.err.println("list null");
			return tempList;
		}
		for (int i=0; i<list.length; i++) {
			if(filterRegex != null && !list[i].getAbsolutePath().matches(filterRegex)) {
				continue;
			}
			if (list[i].isDirectory()) {
				tempList.addAll(findFiles(list[i], subffix, filterRegex));
			} else {
				if(list[i].getName().endsWith(subffix)) {					
					tempList.add(list[i].getAbsolutePath());
				}		    	  
			}
		}
		return tempList;
	}
	
	public static String getTempPath()
	{
		String tempPath = System.getProperty("java.io.tmpdir");
		String separator = File.separator + (File.separator.equals("\\") ? File.separator : "");

		if(!tempPath.matches(".*"+separator+"$")) tempPath += File.separator;
		tempPath += "ApkScanner";
		
		return tempPath;
	}
	
	public static String makeTempPath(String apkFilePath)
	{
		String tempPath = getTempPath();

		tempPath += apkFilePath.substring(apkFilePath.indexOf(File.separator),apkFilePath.lastIndexOf(".")).replaceAll("#", "");
		
		if((new File(tempPath)).exists()) {
			int n;
			for(n=1; (new File(tempPath+"_"+n)).exists(); n++) ;
			tempPath += "_" + n;
		}
		
		return tempPath;
	}

	public static Boolean makeFolder(String FilePath)
	{
		File newDirectory = new File(FilePath);
		if(!newDirectory.exists()) {
			newDirectory.mkdir();
			return true;
		}
		return false;
	}

    public static boolean deleteDirectory(File path)
    {
		if(!path.exists()) {
			return false;
		}
		if(path.isDirectory()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {            	
					file.delete();
				}
			}
		}
		return path.delete();
    }

	public enum FSStyle
	{
		NONE(-1),
		BYTES(0),
		KB(1),
		MB(2),
		GB(3),
		TB(4),
		SHORT(5),
		FULL(6);

		private final int value;
	    private FSStyle(int value)
	    {
	        this.value = value;
	    }

	    static FSStyle fromValue(int value)
	    { 
	        for (FSStyle my: FSStyle.values()) { 
	            if (my.value == value) { 
	                return my; 
	            } 
	        } 
	        return null; 
	    } 
	    
	    public int getValue()
	    {
	        return value;
	    }
	    
		public String toString()
		{
			if(this.equals(BYTES))
				return new String("Bytes");
			else
				return super.toString();
		}
	}

	public static String getFileSize(File file, FSStyle style) {
		if (!file.exists() || !file.isFile()) {
			return "-1";
		}
		return getFileSize(file.length(), style);
	}

	public static String getFileSize(long length, FSStyle style) {
		double LengthbyUnit = (double) length;
		int Unit = 0;
		String strUnit = null;

		DecimalFormat df = new DecimalFormat("#,##0.00");
		StringBuilder result = new StringBuilder(df.format(LengthbyUnit).length());
		
		switch(style) {
		case BYTES: case KB: case MB: case GB: case TB:
			Unit = style.getValue();
			while (Unit-- > 0) {
				LengthbyUnit = LengthbyUnit / 1024;
			}
			strUnit = " " + style.toString();
			break;
		case SHORT: case FULL:
			Unit = 0;
			while (LengthbyUnit > 1024 && Unit < 5) {
				LengthbyUnit = LengthbyUnit / 1024;
				Unit++;
			}
			strUnit = " " + FSStyle.fromValue(Unit).toString();
			break;
		case NONE: default:
			strUnit = "";
			df = new DecimalFormat("#,##0");
			break;
		}
		result.append(df.format(LengthbyUnit) + strUnit);
		
		if(style.equals(FSStyle.FULL)) {
			df = new DecimalFormat("#,##0");
			result.append(" (" + df.format(length) +" Bytes)");
		}

		return result.toString();
	}

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
