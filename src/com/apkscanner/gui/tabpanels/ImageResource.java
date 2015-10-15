package com.apkscanner.gui.tabpanels;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.data.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.resource.Resource;

import java.util.ArrayList;

public class ImageResource extends JPanel implements TabDataObject
{
	private static final long serialVersionUID = -934921813626224616L;

	private Map<String, ImageIcon> imageMap = new HashMap<>();
    
	private JLabel photographLabel;
	ArrayList<String> nameList = new ArrayList<String>();
	private String apkFilePath = null;
	
	JList<Object> list = null;
    
	public ImageResource()
	{

	}
	
	@Override
	public void initialize()
	{
		list = new JList<Object>();
		list.setCellRenderer(new MarioListRenderer());
		list.addListSelectionListener(new JListHandler());

		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(300, 400));
		scroll.repaint();
		        
		photographLabel = new JLabel();
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		        
		this.setLayout(new GridLayout(1, 2));        
		this.add(scroll);
		this.add(photographLabel);
	}

	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(list == null)
			initialize();
		
		nameList.clear();
		imageMap.clear();
		list.clearSelection();
		
		this.apkFilePath = apkInfo.ApkPath; 
		
		if(apkInfo.ImageList == null) return;
		
		nameList.addAll(apkInfo.ImageList);
		createImageMap(nameList);
		
		list.setListData(nameList.toArray());
	}
    
	public class MarioListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 2674069622264059360L;
		//Font font = new Font("helvitica", Font.BOLD, 10);

		@Override
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(
			        list, value, index, isSelected, cellHasFocus);
			
			label.setIcon(imageMap.get((String)value));
			label.setHorizontalTextPosition(JLabel.RIGHT);
			//label.setFont(font);            
			return label;
        }
    }

	private Map<String, ImageIcon> createImageMap(ArrayList<String> list) {
		//Map<String, ImageIcon> map = new HashMap<>();
		String jarPath = "jar:file:"+apkFilePath.replaceAll("#", "%23")+"!/";
		try {        	
			for(int i=0; i< list.size(); i++) {
				if(list.get(i).endsWith(".qmg")) {
					imageMap.put(list.get(i), new ImageIcon(ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32)));
				} else {
					imageMap.put(list.get(i), new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(new URL(jarPath+list.get(i))),32,32)));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return imageMap;
	}

    private class JListHandler implements ListSelectionListener
    {
    	// 리스트의 항목이 선택이 되면
    	public void valueChanged(ListSelectionEvent event)
    	{
    		//Log.i("valueChanged : " + list.getSelectedIndex() + " event : "+ event.getSource());
    		if(list.getSelectedIndex() < 0)
    			return;
    		String imgPath = "jar:file:"+apkFilePath.replaceAll("#", "%23")+"!/" + nameList.get(list.getSelectedIndex());
    		if(imgPath.endsWith(".qmg")) {
    			imgPath = Resource.IMG_QMG_IMAGE_ICON.getPath();
    		}
    		try {
				photographLabel.setIcon(new ImageIcon(ImageScaler.getMaxScaledImage(
						new ImageIcon(new URL(imgPath)),photographLabel.getWidth(),photographLabel.getHeight())));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
    	}
    }

	@Override
	public void reloadResource() {
		
	}
}