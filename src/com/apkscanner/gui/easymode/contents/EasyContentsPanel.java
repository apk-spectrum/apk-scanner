package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.border.EmptyBorder;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.EasyGuiEmptyPanel;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.EasyTextField;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyContentsPanel extends JPanel{
	//FlatPanel appiconpanel;
	EasyFeaturePanel featurepanel;
	EasyGuiToolPanel toolbarpanel;
	
	EasysdkNotDrawPanel sdkverpanel;
	
	EasyFlatLabel packagepanel;
	EasyFlatLabel ininerversionpanel;
	EasyTextField ininersizepanel;
	EasyTextField apptitlelabel;
	
	JLabel appicon;
	
	JPanel contentsCardPanel;
	
	static private Color IconPanelcolor = new Color(220,220,220);
	
	static private Color labelfontcolor = new Color(84,130,53);
	
	static private Color packagePanelcolor = new Color(220,230,242);
	static private Color packagefontcolor = new Color(130,114,196);
	
	static private Color versionfontcolor = new Color(237, 126, 83);
	static private Color sdkverPanelcolor = new Color(242,242,242);
	
	static private Color ininerinfotcolor = new Color(121,121,121);
	static private Color ininerversiontcolor = new Color(121,121,121);
	
	private static String CARD_LAYOUT_EMPTY = "card_empty";
	private static String CARD_LAYOUT_APKINFO = "card_apkinfo";
	
	public EasyContentsPanel() {
		// TODO Auto-generated constructor stub
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setBackground(Color.WHITE);
		
		contentsCardPanel = new JPanel(new CardLayout());
		contentsCardPanel.add(makeapkinfoPanel(), CARD_LAYOUT_APKINFO);
		contentsCardPanel.add(new EasyGuiEmptyPanel(), CARD_LAYOUT_EMPTY);
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_EMPTY);
		
		add(makeapkiconPanel(), BorderLayout.WEST);
		add(contentsCardPanel,BorderLayout.CENTER);
		setEmptypanel();
	}
	
	private JPanel makeapkiconPanel() {
		JPanel apkiconpanel = new JPanel();
		
		//appicon
		FlatPanel appiconpanel = new FlatPanel();		
		appiconpanel.setBackground(sdkverPanelcolor);
		appiconpanel.setPreferredSize(new Dimension(160, 0));
		appiconpanel.setshadowlen(3);
		appicon = new JLabel();
		appicon.setHorizontalAlignment(JLabel.CENTER);
		appicon.setVerticalAlignment(JLabel.CENTER);
		appiconpanel.add(appicon, BorderLayout.CENTER);
		
		//applabel
		
		JPanel applabelpanel = new JPanel(new BorderLayout());
		applabelpanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		applabelpanel.setBackground(sdkverPanelcolor);
		applabelpanel.setPreferredSize(new Dimension(0, 60));
		applabelpanel.setOpaque(false);
		
		apptitlelabel = new EasyTextField(" ");
		setEasyTextField(apptitlelabel);
		apptitlelabel.setForeground(labelfontcolor);		
		apptitlelabel.setHorizontalAlignment(JTextField.CENTER);
		apptitlelabel.setPreferredSize(new Dimension(0, 35));
		apptitlelabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		applabelpanel.add(apptitlelabel, BorderLayout.CENTER);
		
		//size
		ininersizepanel = new EasyTextField(" ");
		setEasyTextField(ininersizepanel);
		ininersizepanel.setPreferredSize(new Dimension(0, 15));
		ininersizepanel.setHorizontalAlignment(JTextField.RIGHT);		
		ininersizepanel.setFont(new Font(getFont().getName(), Font.BOLD, 10));
		applabelpanel.add(ininersizepanel, BorderLayout.SOUTH);
		
		appiconpanel.add(applabelpanel, BorderLayout.SOUTH);
		
		return appiconpanel;
	}
	
	
	private JPanel makeapkinfoPanel() {
		JPanel infopanel = new JPanel(new BorderLayout());
		//package
		packagepanel = new EasyFlatLabel(" ", sdkverPanelcolor, packagefontcolor);
		packagepanel.setPreferredSize(new Dimension(0, 35));		
		packagepanel.setshadowlen(3);
		packagepanel.setTextFont(new Font(getFont().getName(), Font.BOLD, 15));
		infopanel.add(packagepanel, BorderLayout.NORTH);

		sdkverpanel = new EasysdkNotDrawPanel();
		sdkverpanel.setBackground(sdkverPanelcolor);
		sdkverpanel.setPreferredSize(new Dimension(80, 0));
		
		sdkverpanel.setshadowlen(3);
		infopanel.add(sdkverpanel, BorderLayout.EAST);
		
		JPanel innerinfopanel = new JPanel(new BorderLayout());
		
		//version
		ininerversionpanel = new EasyFlatLabel(" ", sdkverPanelcolor, versionfontcolor);
		ininerversionpanel.setPreferredSize(new Dimension(0, 35));
		ininerversionpanel.setshadowlen(3);
		
		innerinfopanel.add(ininerversionpanel, BorderLayout.NORTH);
		
		//versionpanel.add(ininerversionpanel);
		//versionpanel.add(ininersizepanel);
		
		featurepanel = new EasyFeaturePanel();
		
		toolbarpanel = new EasyGuiToolPanel();
		
		innerinfopanel.add(toolbarpanel, BorderLayout.SOUTH);		
		infopanel.add(innerinfopanel, BorderLayout.CENTER);
		return infopanel;
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

    public void setEmptypanel() {
    	appicon.setIcon(Resource.IMG_APP_ICON.getImageIcon(140, 140));
    	apptitlelabel.setText(Resource.STR_APP_NAME.getString());
    	((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_EMPTY);
    	
    }
    
	public void setContents(ApkInfo apkInfo) {		
		//appicon set
		String temppath = apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length - 1].name;
		try {
			ImageIcon icon;
			icon = new ImageIcon(ImageUtils.getScaledImage(new ImageIcon(ImageIO.read(new URL(temppath))),110,110));
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
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_APKINFO);
	}

	public void clear() {
		// TODO Auto-generated method stub
		sdkverpanel.clear();
		featurepanel.clear();
	}
}
