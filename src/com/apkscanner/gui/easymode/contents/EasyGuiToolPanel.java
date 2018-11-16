package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiToolPanel extends FlatPanel implements ActionListener{
	int HEIGHT = 35;
	int BUTTON_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	static private Color toobarPanelcolor = new Color(232,241,222);
	JPanel toolbartemppanel;
	public EasyGuiToolPanel(int height) {
		init();
		HEIGHT = height - SHADOW_SIZE * 2;
		BUTTON_SIZE = HEIGHT - SHADOW_SIZE * 2;
		setPreferredSize(new Dimension(0, height));
		maketoolbutton();
	}
	
	public EasyGuiToolPanel() {
		init();
		maketoolbutton();
	}
	
	private void init() {
		setBackground(toobarPanelcolor);
		setPreferredSize(new Dimension(0, 40));
		setshadowlen(SHADOW_SIZE);
		toolbartemppanel = new JPanel();
		add(toolbartemppanel, BorderLayout.CENTER);		
		toolbartemppanel.setLayout((new FlowLayout(FlowLayout.LEFT,1, 1)));
		toolbartemppanel.setOpaque(false);
		
		EasyButton btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
		btnsetting.setPreferredSize(new Dimension(15, 15));
		btnsetting.addActionListener(this);
		add(btnsetting, BorderLayout.EAST);
	}
	
	private void maketoolbutton() {
		
		/////////////// tool sample		
		EasyFlatLabel addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_INSTALL.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(149, 179, 215));
		addtool.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
		addtool.setshadowlen(SHADOW_SIZE);
		addtool.setClicklistener(this);
		
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(195, 214, 155));
		addtool.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
		addtool.setshadowlen(SHADOW_SIZE);
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144));
		addtool.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
		addtool.setshadowlen(SHADOW_SIZE);
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(204, 193, 218));
		addtool.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
		addtool.setshadowlen(SHADOW_SIZE);
		toolbartemppanel.add(addtool);
		/////////////// end
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Log.d("tool click");
	}
}
