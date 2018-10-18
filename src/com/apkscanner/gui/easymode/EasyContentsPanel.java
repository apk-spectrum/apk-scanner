package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.apkscanner.gui.easymode.test.FlatPanel;

public class EasyContentsPanel extends JPanel{
	FlatPanel appiconpanel;
	FlatPanel packagepanel;
	FlatPanel sdkverpanel;
	
	FlatPanel versionpanel;
	FlatPanel featurepanel;
	FlatPanel toolbarpanel;
	
	static private Color IconPanelcolor = new Color(191,191,191);
	static private Color packagePanelcolor = new Color(221,217,195);
	static private Color sdkverPanelcolor = new Color(242,242,242);
	static private Color toobarPanelcolor = new Color(232,241,222);
	
	public EasyContentsPanel() {
		// TODO Auto-generated constructor stub
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		appiconpanel = new FlatPanel();
		appiconpanel.setBackground(IconPanelcolor);
		appiconpanel.setPreferredSize(new Dimension(160, 0));
		appiconpanel.setshadowlen(3);
		add(appiconpanel, BorderLayout.WEST);
		
		JPanel infopanel = new JPanel(new BorderLayout());

		packagepanel = new FlatPanel();
		packagepanel.setBackground(packagePanelcolor);
		packagepanel.setPreferredSize(new Dimension(0, 35));
		packagepanel.setshadowlen(3);
		infopanel.add(packagepanel, BorderLayout.NORTH);

		sdkverpanel = new FlatPanel();
		sdkverpanel.setBackground(sdkverPanelcolor);
		sdkverpanel.setPreferredSize(new Dimension(80, 0));
		sdkverpanel.setshadowlen(3);
		infopanel.add(sdkverpanel, BorderLayout.EAST);
		
		JPanel innerinfopanel = new JPanel(new BorderLayout());
		
		versionpanel = new FlatPanel();
		versionpanel.setBackground(sdkverPanelcolor);
		versionpanel.setPreferredSize(new Dimension(0, 40));
		versionpanel.setshadowlen(3);
		innerinfopanel.add(versionpanel, BorderLayout.NORTH);
		
		featurepanel = new FlatPanel();
		featurepanel.setBackground(sdkverPanelcolor);
		featurepanel.setshadowlen(3);
		innerinfopanel.add(featurepanel, BorderLayout.CENTER);
		
		toolbarpanel = new FlatPanel();
		toolbarpanel.setBackground(toobarPanelcolor);
		toolbarpanel.setPreferredSize(new Dimension(0, 40));
		toolbarpanel.setshadowlen(3);
		innerinfopanel.add(toolbarpanel, BorderLayout.SOUTH);
		
		infopanel.add(innerinfopanel, BorderLayout.CENTER);
		
		add(infopanel,BorderLayout.CENTER);		
	}
	
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(550, 210);
    }
}
