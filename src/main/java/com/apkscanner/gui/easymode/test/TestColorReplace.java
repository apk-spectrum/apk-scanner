package com.apkscanner.gui.easymode.test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TestColorReplace {

    public static void main(String[] args) {
        try {
            BufferedImage img = colorImage(ImageIO.read(new File(System.getProperty("user.dir") + "/res/icons/perm_group_storage.png")));
            ImageIO.write(img, "png", new File("/home/leejinhyeong/Desktop/Test.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static BufferedImage colorImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster raster = image.getRaster();

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int[] pixels = raster.getPixel(xx, yy, (int[]) null);
                pixels[0] = 0;
                pixels[1] = 0;
                pixels[2] = 255;
                raster.setPixel(xx, yy, pixels);
            }
        }
        return image;
    }
}