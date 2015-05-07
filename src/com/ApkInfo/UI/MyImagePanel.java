package com.ApkInfo.UI;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyImagePanel extends JPanel{

    private BufferedImage image;
    private int mwidth, mheight;
    public MyImagePanel(String FilePath) {
    	    	
    	
       try {                
          image = ImageIO.read(new File(FilePath));
       } catch (IOException ex) {
            // handle exception...
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, 200,200, null); // see javadoc for more info on the parameters            
    }

}