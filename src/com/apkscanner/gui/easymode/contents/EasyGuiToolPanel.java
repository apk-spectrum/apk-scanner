package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiToolPanel extends FlatPanel implements ActionListener{
	int HEIGHT = 35;
	int WIDTH = 100;
	int BUTTON_SIZE = 35-6;
	int SHADOW_SIZE = 3;
	static private Color toobarPanelcolor = new Color(232,241,222);
	JPanel toolbartemppanel;
	EasyButton btnsetting;
	JScrollPane scrollPane;
	int buttoncount=0;
	
	public EasyGuiToolPanel(int height, int width) {
		
		HEIGHT = height - SHADOW_SIZE * 2;
		BUTTON_SIZE = HEIGHT - SHADOW_SIZE * 2;
		WIDTH = width;
		init();
		setPreferredSize(new Dimension(0, height));
		maketoolbutton();
	}
		
	private void init() {
		setBackground(toobarPanelcolor);
		setPreferredSize(new Dimension(0, 40));
		setshadowlen(SHADOW_SIZE);
		
		toolbartemppanel = new JPanel();
		
		scrollPane = new JScrollPane(toolbartemppanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));
		scrollPane.getVerticalScrollBar().setUnitIncrement(100);
		
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
	
	private void maketoolbutton() {
		EasyFlatLabel[] buttonlist = {
				new EasyFlatLabel(Resource.IMG_TOOLBAR_INSTALL.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(149, 179, 215)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_INSTALL.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(149, 179, 215)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(195, 214, 155)),

				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				
				
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(250, 192, 144)),
				new EasyFlatLabel(Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(BUTTON_SIZE,BUTTON_SIZE), new Color(204, 193, 218))
				
		};
		
		for(EasyFlatLabel btn : buttonlist) {
			btn.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
			btn.setshadowlen(SHADOW_SIZE);
			btn.setClicklistener(this);		
			toolbartemppanel.add(btn);
			buttoncount++;
		}
		//toolbartemppanel.setPreferredSize(new Dimension(0,300));
		// * buttonlist.length / EasyContentsPanel.WIDTH);
		
		int line = (int)((HEIGHT + 1)* buttoncount / 
				(WIDTH - btnsetting.getPreferredSize().getWidth() - scrollPane.getVerticalScrollBar().getPreferredSize().getWidth()));
		
//		Log.d(""+HEIGHT);
//		Log.d((HEIGHT + 1)* buttoncount+"/"+(WIDTH - btnsetting.getPreferredSize().getWidth() - scrollPane.getVerticalScrollBar().getPreferredSize().getWidth()));
		
		toolbartemppanel.setPreferredSize(new Dimension(0, HEIGHT * (line + 1) + ((line !=0)?SHADOW_SIZE : 0)));
		
	//	Log.d(""+ );
		
		/////////////// end
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		Log.d("tool click");		
	}
}
