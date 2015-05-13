package com.ApkInfo.TabUI;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
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

public class MyTabUIResource extends JPanel{

    private final Map<String, ImageIcon> imageMap;
    
    private JLabel photographLabel;
    ArrayList<String> nameList;
    JList list;
    
    public MyTabUIResource(String DefaultFilePath) {
    	
    	nameList = new ArrayList<String>();
    	 
    	nameList = CoreApkTool.findfileforResource(new File(DefaultFilePath));
    	 
    	System.out.println("Resource(*.png) Count : " + nameList.size());
    	 
    	
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

        this.setLayout(new GridLayout(1, 2));
        
        this.add(scroll);
        this.add(photographLabel);
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
        	for(int i=0; i< list.size(); i++) {
        		map.put(list.get(i), new ImageIcon(getScaledImage(new ImageIcon(list.get(i)),32,32)));
        	}
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
    		
    		photographLabel.setIcon(new ImageIcon(CoreApkTool.getMaxScaledImage(
    				new ImageIcon(nameList.get(list.getSelectedIndex())),photographLabel.getWidth(),photographLabel.getHeight())));
    		
	    }
    }
}