package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDnDDlg;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiToolScaleupPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 4941481653470088827L;

	int HEIGHT = 35;
	int WIDTH = 100;
	int BUTTON_IMG_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	static private Color toobarPanelcolor = new Color(232,241,222);
	JPanel toolbartemppanel;
	EasyButton btnsetting;
	ArrayList<ToolEntry> entrys;
	
	public EasyGuiToolScaleupPanel(int height, int width) {
		HEIGHT = height;
		BUTTON_IMG_SIZE = 35;
		WIDTH = width;
		entrys = ToolEntryManager.getShowToolbarList();
		init();
		setPreferredSize(new Dimension(0, height));		
		maketoolbutton();
	}
		
	private void init() {
		setBackground(toobarPanelcolor);
		setPreferredSize(new Dimension(0, HEIGHT));
		setOpaque(false);
		//setshadowlen(SHADOW_SIZE);
		//setshadowlen(1);
		
		
		toolbartemppanel = new JPanel();
		FlowLayout flowlayout = new FlowLayout(FlowLayout.CENTER,0, 0);
		flowlayout.setAlignOnBaseline(true);
		
		add(toolbartemppanel, BorderLayout.NORTH);
		toolbartemppanel.setLayout(flowlayout);
		
		toolbartemppanel.setOpaque(false);

		btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
		btnsetting.setPreferredSize(new Dimension(15, 15));
		btnsetting.addActionListener(this);
		add(btnsetting, BorderLayout.EAST);
		
	}
	
	private void maketoolbutton() {		
		toolbartemppanel.removeAll();
		entrys = ToolEntryManager.getShowToolbarList();
		for(ToolEntry entry : entrys) {
			//EasyFlatLabel btn = new EasyFlatLabel(ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE), new Color(149, 179, 215));
			//Image img = ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE);
			EasyToolIcon btn = new EasyToolIcon(entry.getImage(), toolbartemppanel);
			btn.setAlignmentY(Component.TOP_ALIGNMENT);
			//btn.setText("aaaa");
			//btn.setIcon(new ImageIcon(img));
			//btn.setPreferredSize(new Dimension(BUTTON_IMG_SIZE, BUTTON_IMG_SIZE));
			btn.setScalesize(60);
			//btn.setactionCommand(entry.getTitle());
			//btn.setshadowlen(SHADOW_SIZE);
			//btn.setTooltip(entry.getTitle());
			//btn.setClicklistener(this);
			toolbartemppanel.add(btn);
		}			
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Log.d("Click");
		if(e.getSource().equals(btnsetting)) {
			JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
			EasyToolbarSettingDnDDlg dlg = new EasyToolbarSettingDnDDlg(window, true);
			if(dlg.ischange()) {				
				maketoolbutton();
				//setviewportsize();
			}
		} else {
			Log.d(e.getActionCommand());
			ToolEntryManager.excuteEntry(e.getActionCommand());
		}		
	}
}
