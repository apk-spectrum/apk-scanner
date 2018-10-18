package com.apkscanner.gui.easymode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.test.FlatPanel;
import com.apkscanner.util.Log;


public class EasyPermissionPanel extends FlatPanel implements ActionListener{
	
	static private Color bordercolor = new Color(242, 242, 242);
	static private Color dangerouscolor = new Color(181,107,105); 
	static private Color permissionbackgroundcolor = new Color(217,217,217);
	
	static private int HEIGHT = 50;
	static private int SHADOWSIZE = 3;
	static private int PERMISSIONICONSIZE = 43;
	
	public EasyPermissionPanel() {
		// TODO Auto-generated constructor stub
		setBackground(bordercolor);
		//permissionpanel = getContentPanel(); 
		//add(permissionpanel, BorderLayout.CENTER);
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 1));
		setshadowlen(SHADOWSIZE);
		
		FlatPanel permissionicon = new FlatPanel();
		
		BufferedImage berimg;
		try {
			berimg = colorImage(ImageIO.read(new File(System.getProperty("user.dir") + "/res/icons/perm_group_storage.png")), dangerouscolor);
			EasyButton btn = new EasyButton(new ImageIcon(berimg));
			permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
			permissionicon.setshadowlen(SHADOWSIZE);
			permissionicon.setBackground(permissionbackgroundcolor);
			permissionicon.add(btn);
			btn.addActionListener(this);
			add(permissionicon);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE,PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_contacts.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_sms.png")));
		add(permissionicon);
		
		permissionicon = new FlatPanel();
		permissionicon.setPreferredSize(new Dimension(PERMISSIONICONSIZE, PERMISSIONICONSIZE));
		permissionicon.setshadowlen(SHADOWSIZE);
		permissionicon.setBackground(permissionbackgroundcolor);
		permissionicon.add(new EasyButton(new ImageIcon(System.getProperty("user.dir") + "/res/icons/perm_group_personal_info.png")));
		add(permissionicon);
		//permissionpanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		//permissionpanel.setBorder(new FlatBorder(BevelBorder.RAISED));
		
		//setBackground(Color.RED);
		setPreferredSize(new Dimension(0, HEIGHT));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Log.d("click permission");
	}

    private static BufferedImage colorImage(BufferedImage image, Color color) {
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
        return image;
    }
}
