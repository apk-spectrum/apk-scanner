package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.apkscanner.gui.easymode.contents.EasyToolIcon.EasyToolListner;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.dlg.EasyToolbarSettingDnDDlg;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.GraphicUtil;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiToolScaleupPanel extends JPanel implements ActionListener, EasyToolListner{
	private static final long serialVersionUID = 4941481653470088827L;

	int HEIGHT = 35;
	int WIDTH = 100;
	int BUTTON_IMG_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	static private Color toobarPanelcolor = new Color(232,241,222);
	JPanel toolbartemppanel;
	EasyButton btnsetting;
	ArrayList<ToolEntry> entrys;
	boolean drawtext = false;
	Point  tooliconlocation = new Point();
	String iconlabel = "";
	
	
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
		//setLayout(new BorderLayout());
		//setshadowlen(SHADOW_SIZE);
		//setshadowlen(1);
		
		
		toolbartemppanel = new JPanel();
		FlowLayout flowlayout = new FlowLayout(FlowLayout.CENTER,0, 0);
		//toolbartemppanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
		toolbartemppanel.setLayout(flowlayout);
		((FlowLayout) toolbartemppanel.getLayout()).setAlignOnBaseline(true);
		toolbartemppanel.setOpaque(false);
		add(toolbartemppanel);		
		
		
		
		//toolbartemppanel.setAlignmentY(Component.TOP_ALIGNMENT);

		//((FlowLayout) this.getLayout()).setAlignOnBaseline(true);
		
//		btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
//		btnsetting.setPreferredSize(new Dimension(15, 35));
//		btnsetting.addActionListener(this);		
//		add(btnsetting);
		
	}
	public void paintComponent(Graphics g) {
		//super.paint(g);
		super.paintComponent(g);
//		Graphics2D graphics2D = (Graphics2D) g;
//		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		Font font = new Font("Serif", Font.BOLD, 20);
//		graphics2D.setFont(font);
//		graphics2D.drawString("Support", 100, 80);
		if(drawtext) {
			//int textwidth = iconlabel.length() * 20;
			//GraphicUtil.drawRoundrectText(g, tooliconlocation.x - textwidth/2, 70, textwidth, 30, iconlabel);
			GraphicUtil.drawTextRoundrect(g, tooliconlocation.x, 67, 15, iconlabel);
			
		}
	}
	
	private void maketoolbutton() {		
		toolbartemppanel.removeAll();
		entrys = ToolEntryManager.getShowToolbarList();
		for(ToolEntry entry : entrys) {
			//EasyFlatLabel btn = new EasyFlatLabel(ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE), new Color(149, 179, 215));
			//Image img = ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE);
			final EasyToolIcon btn = new EasyToolIcon(entry.getImage());
			//btn.setAlignmentY(Component.TOP_ALIGNMENT);
			//btn.setText("aaaa");
			//btn.setIcon(new ImageIcon(img));
			//btn.setPreferredSize(new Dimension(BUTTON_IMG_SIZE, BUTTON_IMG_SIZE));
			btn.setScalesize(60);
			btn.setAction(entry.getTitle(), this);
			btn.setEasyToolListner(this);
			btn.setText(entry.getTitle());
			//btn.setOpaque(true);
			//btn.setactionCommand(entry.getTitle());
			
			//btn.setshadowlen(SHADOW_SIZE);
			//btn.setTooltip(entry.getTitle());
			//btn.setClicklistener(this);
		
			
			toolbartemppanel.add(btn);
			toolbartemppanel.updateUI();
		}
		btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15))        {
            @Override
            public int getBaseline(int width, int height) {
                return 0;
            }
        };;
		btnsetting.setPreferredSize(new Dimension(15, 30));
		btnsetting.addActionListener(this);		
		toolbartemppanel.add(btnsetting);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Log.d("Click");
		if(e.getSource().equals(btnsetting)) {
			JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
			EasyToolbarSettingDnDDlg dlg = new EasyToolbarSettingDnDDlg(window, true);
			if(dlg.ischange()) {				
				maketoolbutton();
			}
		} else {
			Log.d(e.getActionCommand());
			ToolEntryManager.excuteEntry(e.getActionCommand());
		}		
	}

	@Override
	public void changestate(int state, EasyToolIcon easyiconlabel) {
		// TODO Auto-generated method stub
		switch(state) {
		case EasyToolListner.STATE_ANIMATION_END:
			drawtext = true;
			tooliconlocation.x = toolbartemppanel.getLocation().x + easyiconlabel.getLocation().x + (int)(easyiconlabel.getBounds().getWidth()/2);
			//Log.d(easyiconlabel.getBounds() + "");
			iconlabel = easyiconlabel.getText();
			updateUI();
			break;
		case EasyToolListner.STATE_ENTER:
			break;
		case EasyToolListner.STATE_EXIT:
			drawtext = false;
			updateUI();
			break;
			
		}
	}


}
