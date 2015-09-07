package com.apkscanner.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyImagePanel extends JPanel{
	private static final long serialVersionUID = -4668877323388291513L;

	private BufferedImage image;

	public MyImagePanel(String FilePath) {
    	setData(FilePath);
    }
    
    public void setData(String FilePath)
    {
        try {                
            image = ImageIO.read(new File(FilePath));
         } catch (IOException ex) {
              // handle exception...
         }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, 100,100, null); // see javadoc for more info on the parameters            
    }

}