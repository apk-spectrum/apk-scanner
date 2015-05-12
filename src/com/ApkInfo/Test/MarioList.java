package com.ApkInfo.Test;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ApkInfo.Core.CoreApkTool;

public class MarioList extends JPanel{

    private final Map<String, ImageIcon> imageMap;
    
    private JLabel photographLabel;
    ArrayList<String> nameList;
    JList list;
    
    public MarioList(String DefaultFilePath) {
    	
    	nameList = new ArrayList<String>();
    	 
    	nameList = CoreApkTool.findfileforResource(new File("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/"));
    	 
    	 for(int i=0;i < nameList.size(); i++) {
    		 System.out.println(nameList.get(i));
    	 }
    	
        imageMap = createImageMap(nameList);
        
        list = new JList(nameList.toArray());
        list.setCellRenderer(new MarioListRenderer());
        list.addListSelectionListener(new JListHandler());
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(300, 400));
                
        photographLabel = new JLabel();
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        

        JFrame frame = new JFrame();

        frame.setLayout(new GridLayout(1, 2));
        
        frame.add(scroll);
        frame.add(photographLabel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);        
    }

    public class MarioListRenderer extends DefaultListCellRenderer {

        //Font font = new Font("helvitica", Font.BOLD, 10);

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(imageMap.get((String) value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            //label.setFont(font);            
            return label;
        }
    }

    private Map<String, ImageIcon> createImageMap(ArrayList<String> list) {
        Map<String, ImageIcon> map = new HashMap<>();
        try {        	
            //map.put("Mario", new ImageIcon(getScaledImage("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png")));
           //map.put("Luigi", new ImageIcon(getScaledImage("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/artist_link_icon.png")));
            //map.put("Bowser", new ImageIcon(getScaledImage("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png")));
            //map.put("Koopa", new ImageIcon(getScaledImage("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png")));
        	
        	for(int i=0; i< list.size(); i++) {
        		map.put(list.get(i), new ImageIcon(getScaledImage(new ImageIcon(list.get(i)),32,32)));
        	}
        	
        	
//            map.put("Mario", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/popup_dmusic_norm.png"),32,32)));
//            map.put("Luigi", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png"),32,32)));
//            map.put("Bowser", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/popup_dvideo_norm.png"),32,32)));
//            map.put("Koopa", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/popup_dmarket_logo.png"),32,32)));
//            map.put("Princess", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png"),32,32)));
//            map.put("Princesswwd", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png"),32,32)));
//            map.put("Princess2d", new ImageIcon(getScaledImage(new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/res/sampleimage/drawable-xxxhdpi/button_dmarket_off.png"),32,32)));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }


    private class JListHandler implements ListSelectionListener {
    // 리스트의 항목이 선택이 되면
    public void valueChanged(ListSelectionEvent event) {
    	
    		String imagepath;
    		System.out.println("valueChanged : " + list.getSelectedIndex() + " event : "+ event.getSource());
    		    		
    		
    		System.out.println(" " + nameList.get(list.getSelectedIndex()));
    		
    		photographLabel.setIcon(new ImageIcon(getScaledImage(new ImageIcon(nameList.get(list.getSelectedIndex())),200,200)));
    		
	    }
    }
    
    private Image getScaledImage(ImageIcon temp, int w, int h){
    	
    	Image srcImg = temp.getImage();
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MarioList("res/sampleimage/");
            }
        });
    }
}