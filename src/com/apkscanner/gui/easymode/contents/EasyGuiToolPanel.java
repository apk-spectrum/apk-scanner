package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.apkscanner.gui.easymode.EasyGuiMain;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDlg;
import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDnDDlg;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiToolPanel extends FlatPanel implements ActionListener{
	int HEIGHT = 35;
	int WIDTH = 100;
	int BUTTON_IMG_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	static private Color toobarPanelcolor = new Color(232,241,222);
	JPanel toolbartemppanel;
	EasyButton btnsetting;
	JScrollPane scrollPane;	
	ArrayList<ToolEntry> entrys;
	
	public EasyGuiToolPanel(int height, int width) {
		HEIGHT = height - SHADOW_SIZE * 2;
		BUTTON_IMG_SIZE = HEIGHT - SHADOW_SIZE * 2;
		WIDTH = width;
		entrys = ToolEntryManager.getShowToolbarList();
		init();
		setPreferredSize(new Dimension(0, height));		
		maketoolbutton();
	}
		
	private void init() {
		setBackground(toobarPanelcolor);
		setPreferredSize(new Dimension(0, HEIGHT - SHADOW_SIZE * 2));
		setshadowlen(SHADOW_SIZE);
		
		toolbartemppanel = new JPanel();
		
		scrollPane = new JScrollPane(toolbartemppanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));
		scrollPane.getVerticalScrollBar().setUnitIncrement(HEIGHT+1);
		scrollPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {	}
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub				
				setviewportsize();
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		
		add(scrollPane, BorderLayout.CENTER);
		toolbartemppanel.setLayout((new FlowLayout(FlowLayout.LEFT,1, 1)));
		toolbartemppanel.setOpaque(false);

		btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
		btnsetting.setPreferredSize(new Dimension(15, 15));
		btnsetting.addActionListener(this);
		add(btnsetting, BorderLayout.EAST);
		
	}
	
	private void setviewportsize() {
		WIDTH = scrollPane.getViewport().getWidth();
		int line = (int)((HEIGHT + 1)* entrys.size() /	WIDTH);
		toolbartemppanel.setPreferredSize(new Dimension(0, HEIGHT * (line+1) + ((line !=0)?SHADOW_SIZE : 0)));
		toolbartemppanel.updateUI();
	}
	private void maketoolbutton() {		
		toolbartemppanel.removeAll();
		entrys = ToolEntryManager.getShowToolbarList();
		for(ToolEntry entry : entrys) {
			EasyFlatLabel btn = new EasyFlatLabel(ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE), new Color(149, 179, 215));

			btn.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
			btn.setactionCommand(entry.getTitle());
			btn.setshadowlen(SHADOW_SIZE);
			btn.setTooltip(entry.getTitle());
			btn.setClicklistener(this);			
			toolbartemppanel.add(btn);			
		}			
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Log.d("Click");
		if(e.getSource().equals(btnsetting)) {
			EasyToolbarSettingDnDDlg dlg = new EasyToolbarSettingDnDDlg(EasyGuiMain.frame, true);
			if(dlg.ischange()) {				
				maketoolbutton();
				setviewportsize();
			}
		} else {
			Log.d(e.getActionCommand());
			ToolEntryManager.excuteEntry(e.getActionCommand());
		}		
	}
}
