package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.EasyTextField;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyContentsPanel extends JPanel{
	FlatPanel appiconpanel;
	EasyFeaturePanel featurepanel;
	FlatPanel toolbarpanel;
	
	EasysdkNotDrawPanel sdkverpanel;
	
	EasyFlatLabel packagepanel;
	EasyFlatLabel ininerversionpanel;
	EasyFlatLabel ininersizepanel;
	EasyTextField apptitlelabel;
	
	JLabel appicon;
	
	
	static private Color IconPanelcolor = new Color(220,220,220);
	static private Color packagePanelcolor = new Color(220,230,242);
	static private Color sdkverPanelcolor = new Color(242,242,242);
	static private Color toobarPanelcolor = new Color(232,241,222);
	static private Color packagefontcolor = new Color(89,89,89);
	static private Color ininerinfotcolor = new Color(121,121,121);
	static private Color featurefontcolor = new Color(217,217,217);
	
	public EasyContentsPanel() {
		// TODO Auto-generated constructor stub
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setBackground(Color.WHITE);
		
		//appicon
		appiconpanel = new FlatPanel();
		appiconpanel.setBackground(IconPanelcolor);
		appiconpanel.setPreferredSize(new Dimension(160, 0));
		appiconpanel.setshadowlen(3);
		appicon = new JLabel();
		appiconpanel.add(appicon, BorderLayout.CENTER);
		
		//applabel
		apptitlelabel = new EasyTextField(" ");
		setEasyTextField(apptitlelabel);
		apptitlelabel.setForeground(packagefontcolor);		
		apptitlelabel.setHorizontalAlignment(JTextField.CENTER);
		apptitlelabel.setPreferredSize(new Dimension(0, 30));
		apptitlelabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		appiconpanel.add(apptitlelabel, BorderLayout.SOUTH);
			
		
		add(appiconpanel, BorderLayout.WEST);
		
		JPanel infopanel = new JPanel(new BorderLayout());

		//package
		packagepanel = new EasyFlatLabel(" ", packagePanelcolor, packagefontcolor);
		packagepanel.setPreferredSize(new Dimension(0, 35));
		packagepanel.setshadowlen(3);
		
		infopanel.add(packagepanel, BorderLayout.NORTH);

		sdkverpanel = new EasysdkNotDrawPanel();
		sdkverpanel.setBackground(sdkverPanelcolor);
		sdkverpanel.setPreferredSize(new Dimension(80, 0));
		
		sdkverpanel.setshadowlen(3);
		infopanel.add(sdkverpanel, BorderLayout.EAST);
		
		JPanel innerinfopanel = new JPanel(new BorderLayout());
		JPanel versionpanel = new JPanel(new GridLayout(0, 2));
		versionpanel.setBackground(sdkverPanelcolor);
		versionpanel.setPreferredSize(new Dimension(0, 40));
		innerinfopanel.add(versionpanel, BorderLayout.NORTH);

		//version
		ininerversionpanel = new EasyFlatLabel(" ", sdkverPanelcolor, ininerinfotcolor);
		ininerversionpanel.setPreferredSize(new Dimension(0, 35));
		ininerversionpanel.setshadowlen(3);

		//size
		ininersizepanel = new EasyFlatLabel(" ", sdkverPanelcolor, ininerinfotcolor);
		ininersizepanel.setPreferredSize(new Dimension(0, 35));
		ininersizepanel.setshadowlen(3);
		ininersizepanel.setHorizontalAlignment(JTextField.CENTER);
		
		
		versionpanel.add(ininerversionpanel);
		versionpanel.add(ininersizepanel);
		
		featurepanel = new EasyFeaturePanel();
		/////////////// end
		innerinfopanel.add(featurepanel, BorderLayout.CENTER);
		
		toolbarpanel = new FlatPanel();
		toolbarpanel.setBackground(toobarPanelcolor);
		toolbarpanel.setPreferredSize(new Dimension(0, 40));
		toolbarpanel.setshadowlen(3);
		
		
		JPanel toolbartemppanel = new JPanel(new FlowLayout(FlowLayout.LEFT,1, 1));
		toolbartemppanel.setOpaque(false);
		//toolbarpanel.setLayout(new FlowLayout(FlowLayout.LEFT,1, 1));
		
		
		
		/////////////// tool sample		
		EasyFlatLabel addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_INSTALL.getImageIcon(29,29), new Color(149, 179, 215));
		addtool.setPreferredSize(new Dimension(35, 35));
		addtool.setshadowlen(3);
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(29,29), new Color(195, 214, 155));
		addtool.setPreferredSize(new Dimension(35, 35));
		addtool.setshadowlen(3);
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(29,29), new Color(250, 192, 144));
		addtool.setPreferredSize(new Dimension(35, 35));
		addtool.setshadowlen(3);
		toolbartemppanel.add(addtool);
		
		addtool = new EasyFlatLabel(Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(29,29), new Color(204, 193, 218));
		addtool.setPreferredSize(new Dimension(35, 35));
		addtool.setshadowlen(3);
		toolbartemppanel.add(addtool);		
		/////////////// end
		
		
		toolbarpanel.add(toolbartemppanel, BorderLayout.CENTER);
		
		EasyButton btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
		btnsetting.setPreferredSize(new Dimension(15, 15));
		toolbarpanel.add(btnsetting, BorderLayout.EAST);
		
		
		innerinfopanel.add(toolbarpanel, BorderLayout.SOUTH);		
		infopanel.add(innerinfopanel, BorderLayout.CENTER);		
		add(infopanel,BorderLayout.CENTER);
	}
	
	private void setEasyTextField(JTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		textfield.setEditable(false);
		textfield.setOpaque(false);
		textfield.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
	}
	
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(550, 210);
    }

	public void setContents(ApkInfo apkInfo) {		
		//appicon set
		String temppath = apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length - 1].name;
		try {
			ImageIcon icon;
			icon = new ImageIcon(ImageUtils.getScaledImage(new ImageIcon(ImageIO.read(new URL(temppath))),153,153));
			appicon.setIcon(icon);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//apptitle
		apptitlelabel.setText(apkInfo.manifest.application.labels[0].name);
		//package
		packagepanel.setText(apkInfo.manifest.packageName);		
		//version
		ininerversionpanel.setText(apkInfo.manifest.versionName + " / " + apkInfo.manifest.versionCode);		
		//size
		ininersizepanel.setText(apkInfo.fileSize.toString());
		sdkverpanel.setsdkpanel(apkInfo);
		
		//feature
		featurepanel.setfeature(apkInfo);
	}

	public void clear() {
		// TODO Auto-generated method stub
		sdkverpanel.clear();
		featurepanel.clear();
	}
}
