package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.apkscanner.util.FileUtil;

public class ImageUtils {

	public static BufferedImage imageToBufferedImage(Image im) {
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
		return bi;
	}
	
	public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {
		int offset = 0;
		int width = img1.getWidth() + img2.getWidth() + offset;
		int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		Color oldColor = g2.getColor();
		g2.setPaint(Color.BLACK);
		g2.fillRect(0, 0, width, height);
		g2.setColor(oldColor);
		g2.drawImage(img1, null, 0, 0);
		g2.drawImage(img2, null, img1.getWidth() + offset, 0);
		g2.dispose();
		return newImage;
	}

	public static String covertWebp2Png(final String imagePath, final String tempPath) {
		String[] path = imagePath.split("!");
		String convetPath = imagePath;
		try {
			String apkPath = path[0].replaceAll("^(jar:)?file:", "");
			ZipFile zipFile = new ZipFile(apkPath);
			ZipEntry entry = zipFile.getEntry(path[1].replaceAll("^/", ""));
			if(entry != null) {
				  //String tempPath = FileUtil.makeTempPath(apkPath.substring(apkPath.lastIndexOf(File.separator)));
				  FileUtil.makeFolder(tempPath);
				  String tempImg = tempPath + File.separator + path[1].replaceAll(".*/", "") + ".png";
	              File out = new File(tempImg);
	              InputStream is = zipFile.getInputStream(entry);
	              BufferedImage image = ImageIO.read(is);
	              ImageIO.write(image, "png", out);
	              if(out.exists()) {
	            	  convetPath = "file:"+out.getAbsolutePath();
	              }
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convetPath;
	}
	
    public static Image getScaledImage(ImageIcon temp, int w, int h)
    {
		Image srcImg = temp.getImage();
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		//srcImg.flush();
		return resizedImg;
    }
	
	public static BufferedImage readImageFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}

	public static void writeImageToPNG(File file, BufferedImage bufferedImage) throws IOException {
		ImageIO.write(bufferedImage, "png", file);
	}

	public static void writeImageToJPG(File file, BufferedImage bufferedImage) throws IOException {
		ImageIO.write(bufferedImage, "jpg", file);
	}
	
	public static ImageIcon setcolorImage(ImageIcon icon, Color color) {
		BufferedImage image = imageToBufferedImage(icon.getImage());
        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = image.getRaster();

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int[] pixels = raster.getPixel(xx, yy, (int[]) null);
                pixels[0] = color.getRed();
                pixels[1] = color.getGreen();
                pixels[2] = color.getBlue();
                raster.setPixel(xx, yy, pixels);
            }
        }
        icon.setImage(image);
        return icon;
    }
}