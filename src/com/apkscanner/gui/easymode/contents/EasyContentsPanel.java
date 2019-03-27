package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.android.ddmlib.IDevice;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.gui.easymode.EasyGuiMain;
import com.apkscanner.gui.easymode.util.AndroidLikeToast;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.EasyTextField;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.FileUtil.FSStyle;

public class EasyContentsPanel extends JPanel{
	//FlatPanel appiconpanel;
	EasyFeatureHtmlPanel featurepanel;
	//EasyGuiToolPanel toolbarpanel;
	EasyPermissionPanel permissionPanel;
	EasyDevicePanel devicepanel;
	
	EasyRoundLabel packagepanel;
	EasyRoundLabel ininerversionpanel;
	//EasyTextField ininersizepanel;
	EasyTextField apptitlelabel;
	EasyGuiEmptyPanel emptypanel;
	JLabel appicon;
	EasyRoundButton btnlabelcount;
	JPanel contentsCardPanel;
	JPanel labelcountpanel;
	String mutiLabels = "";
	
	
	int SHADOWSIZE = 2;
	
	static public int WIDTH = 500;
	static public int HEIGHT = 220;
	
	static private int PERMISSION_HEIGHT = 46;
	static private int PACAKGEVERSION_HEIGHT = 35;
	
	static private Color IconPanelcolor = new Color(220,220,220);
	
	static private Color labelfontcolor = new Color(50,186,40);
	
	static private Color packagePanelcolor = new Color(220,230,242);
	static private Color packagefontcolor = new Color(130,114,196);
	
	static private Color versionfontcolor = new Color(237, 126, 83);
	static private Color sdkverPanelcolor = new Color(232,232,232);
	
	static private Color ininerinfotcolor = new Color(121,121,121);
	static private Color ininerversiontcolor = new Color(121,121,121);
	
	private static String CARD_LAYOUT_EMPTY = "card_empty";
	private static String CARD_LAYOUT_APKINFO = "card_apkinfo";	
	
	
	//EasyRoundLabel applabelpanel;
	JTextField applabelpanel;
	public EasyContentsPanel() {
		// TODO Auto-generated constructor stub
		Log.d("start EasyContentsPanel ");
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setBackground(Color.WHITE);
		
		contentsCardPanel = new JPanel(new CardLayout());
		contentsCardPanel.add(makeapkinfoPanel(), CARD_LAYOUT_APKINFO);  //3x ms

		
		add(makeapkiconPanel(), BorderLayout.WEST);
		add(contentsCardPanel,BorderLayout.CENTER);
		
		//setEmptypanel();
		Log.d("End EasyContentsPanel ");
	}
	
	private JPanel makeapkiconPanel() {
		//appicon
		RoundPanel appiconpanel = new RoundPanel();		
		//appiconpanel.setBackground(sdkverPanelcolor);
		appiconpanel.setRoundrectColor(sdkverPanelcolor);
		
		appiconpanel.setPreferredSize(new Dimension(130, 0));
		appiconpanel.setshadowlen(SHADOWSIZE);
		appicon = new JLabel();
		appicon.setHorizontalAlignment(JLabel.CENTER);
		appicon.setVerticalAlignment(JLabel.CENTER);
		appiconpanel.add(appicon, BorderLayout.CENTER);
		
		//applabel		
//		JPanel applabelpanel = new JPanel(new BorderLayout());
//		applabelpanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//		applabelpanel.setBackground(sdkverPanelcolor);
//		applabelpanel.setPreferredSize(new Dimension(0, 60));
//		applabelpanel.setOpaque(false);
//		
//		apptitlelabel = new EasyTextField(" ");
//		setEasyTextField(apptitlelabel);
//		apptitlelabel.setForeground(labelfontcolor);		
//		apptitlelabel.setHorizontalAlignment(JTextField.CENTER);
//		apptitlelabel.setPreferredSize(new Dimension(0, 35));
//		apptitlelabel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
//		applabelpanel.add(apptitlelabel, BorderLayout.CENTER);
		
		JPanel templabelpanel = new JPanel(new BorderLayout());
		//templabelpanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		templabelpanel.setBackground(sdkverPanelcolor);
		templabelpanel.setPreferredSize(new Dimension(0, 40));
		templabelpanel.setOpaque(false);
		
		//applabelpanel = new EasyRoundLabel(" ", sdkverPanelcolor, labelfontcolor);
//		//applabelpanel = new EasyRoundLabel(" ", Color.cyan, labelfontcolor);
//		applabelpanel.setPreferredSize(new Dimension(0, 60));
//		applabelpanel.setOpaque(false);
//		applabelpanel.setTextFont(new Font(getFont().getName(), Font.BOLD, 15));
//		applabelpanel.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel labeltemp = new JPanel(new BorderLayout());
		//labeltemp.setOpaque(false);
		labeltemp.setBackground(Color.RED);
		applabelpanel = new JTextField("");
		applabelpanel.setForeground(labelfontcolor);
		applabelpanel.setFont(new Font(getFont().getName(), Font.BOLD, 15));
		applabelpanel.setHorizontalAlignment(JTextField.CENTER);
				
		labeltemp.add(applabelpanel, BorderLayout.CENTER);
		
		btnlabelcount = new EasyRoundButton("");
		btnlabelcount.setPreferredSize(new Dimension(15, 15));		
		btnlabelcount.setBackground(Color.darkGray);
		btnlabelcount.setForeground(Color.WHITE);		
		btnlabelcount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				showDialog(mutiLabels, Resource.STR_LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200), null);
			}
		});
		labelcountpanel = new JPanel();
		labelcountpanel.setOpaque(false);
		//labelcountpanel.setBackground(Color.blue);
		labelcountpanel.add(btnlabelcount);
		labelcountpanel.setVisible(false);
		
		labeltemp.add(labelcountpanel, BorderLayout.EAST);
		
		templabelpanel.add(labeltemp, BorderLayout.CENTER);
		
		
		appiconpanel.add(templabelpanel, BorderLayout.SOUTH);
		
		return appiconpanel;
	}
	
	private void addClipBoardbutton(final EasyRoundLabel panel) {
		//panel.setLayout(new BorderLayout());
		
		
		EasyRoundButton btnshowpermissiondlg = new EasyRoundButton(Resource.IMG_EASY_WINDOW_CLIPBOARD_ICON.getImageIcon(20, 20));
		
		btnshowpermissiondlg.setPreferredSize(new Dimension(25, 25));
		btnshowpermissiondlg.setBackground(new Color(217, 217, 217));
		btnshowpermissiondlg.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection stringSelection = new StringSelection(panel.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
				AndroidLikeToast.ShowToast("Copying to the clipboard!",panel);
			}
		});
		panel.add(btnshowpermissiondlg, BorderLayout.EAST);
		//panel.add(btnshowpermissiondlg);
	}
	
	private JPanel makeapkinfoPanel() {
		JPanel infopanel = new JPanel(new BorderLayout());
		//package
		packagepanel = new EasyRoundLabel(" ", new Color(217, 217, 217), packagefontcolor);
		packagepanel.setPreferredSize(new Dimension(0, PACAKGEVERSION_HEIGHT));
		addClipBoardbutton(packagepanel);
		//packagepanel.setMouseHoverEffect(true);
		//packagepanel.setclipboard(true);
		
		packagepanel.setshadowlen(SHADOWSIZE);
		packagepanel.setTextFont(new Font(getFont().getName(), Font.BOLD, 15));
		
		infopanel.add(packagepanel, BorderLayout.NORTH);

		devicepanel = new EasyDevicePanel(50);
		devicepanel.setRoundrectColor(new Color(217, 217, 217));
		//sdkverpanel.setPreferredSize(new Dimension(50, 0));
		
		devicepanel.setshadowlen(SHADOWSIZE);
		infopanel.add(devicepanel, BorderLayout.EAST);
		
		JPanel innerinfopanel = new JPanel(new BorderLayout());
		
		//version
		ininerversionpanel = new EasyRoundLabel(" ", new Color(217, 217, 217), versionfontcolor);
		ininerversionpanel.setPreferredSize(new Dimension(0, PACAKGEVERSION_HEIGHT));
		ininerversionpanel.setTextFont(new Font(getFont().getName(), Font.BOLD, 15));
		ininerversionpanel.setshadowlen(SHADOWSIZE);
		
		addClipBoardbutton(ininerversionpanel);
		//ininerversionpanel.setMouseHoverEffect(true);
		//ininerversionpanel.setclipboard(true);
		
		innerinfopanel.add(ininerversionpanel, BorderLayout.NORTH);
		
		featurepanel = new EasyFeatureHtmlPanel();
		featurepanel.setshadowlen(SHADOWSIZE);
		innerinfopanel.add(featurepanel, BorderLayout.CENTER);
		
		//toolbarpanel = new EasyGuiToolPanel(35, WIDTH - 80 - 160);
		
		//innerinfopanel.add(toolbarpanel, BorderLayout.SOUTH);
		infopanel.add(innerinfopanel, BorderLayout.CENTER);
		
		permissionPanel = new EasyPermissionPanel(PERMISSION_HEIGHT);
		infopanel.add(permissionPanel, BorderLayout.SOUTH);
		
		return infopanel;
	}
	
	private void setEasyTextField(EasyTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		//textfield.setEditable(false);
		textfield.setOpaque(false);
		textfield.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
	}
	
//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(HEIGHT, WIDTH);
//    }
    
    public void setEmptypanel() {
    	Log.d("contents emptypanel=)" + emptypanel);
    	if(emptypanel ==null) {    		
    		emptypanel = new EasyGuiEmptyPanel();
    		Log.d("contents new (EasyGuiEmptyPanel=)");
    		contentsCardPanel.add(new EasyGuiEmptyPanel(), CARD_LAYOUT_EMPTY);
    	}
    	appicon.setIcon(Resource.IMG_APP_ICON.getImageIcon(140, 140)); //10 ms
    	//apptitlelabel.setText(Resource.STR_APP_NAME.getString()); // 20-30ms
    	((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_EMPTY);
    }
    private ImageIcon getAppicon(ApkInfo apkInfo) {
		String iconPath = null;
		if(apkInfo.manifest.application.icons != null && apkInfo.manifest.application.icons.length > 0) {
			ResourceInfo[] iconList = apkInfo.manifest.application.icons;
			for(int i=iconList.length-1; i >= 0; i--) {
				if(iconList[i].name.endsWith(".xml")) continue;
				iconPath = iconList[i].name;
				if(iconPath.toLowerCase().endsWith(".webp")) {
					iconPath = ImageUtils.covertWebp2Png(iconPath, apkInfo.tempWorkPath);
				}
				if(iconPath != null) {
					Log.d("icon Path is null");
					break;
				}
			}
		}		
		//appicon set
		//String temppath = apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length - 1].name;
		try {
			ImageIcon icon;
			icon = new ImageIcon(ImageUtils.getScaledImage(new ImageIcon(ImageIO.read(new URL(iconPath))),110,110));
			return icon;
		} catch (IOException e) {
			ImageIcon icon;
			// TODO Auto-generated catch block
			icon = Resource.IMG_WARNING.getImageIcon();
			e.printStackTrace();
			return icon;
		}
    }
    
	private void showDialog(String content, String title, Dimension size, Icon icon) {
		MessageBoxPane.showTextAreaDialog(this, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
	}
	
	private String setAppLabel(ResourceInfo[] labels, String packageName) {
		String appName = null;
		StringBuilder labelBuilder = new StringBuilder();
		if(labels != null && labels.length > 0) {
			appName = ApkInfoHelper.getResourceValue(labels, (String)Resource.PROP_PREFERRED_LANGUAGE.getData(""));
			if(appName != null && appName.isEmpty()) appName = null;

			for(ResourceInfo r: labels) {
				if(r.configuration == null || r.configuration.isEmpty() || "default".equals(r.configuration)) {
					labelBuilder.append(r.name != null ? r.name : packageName);
					if(appName == null && r.name != null) appName = r.name;
				} else {
					labelBuilder.append("[").append(r.configuration).append("] ").append(r.name);
				}
				labelBuilder.append("\n");
			}
		}
		return labelBuilder.toString();
	}
	
	public void setContents(ApkInfo apkInfo) {
		appicon.setIcon(getAppicon(apkInfo));
		//apptitlelabel.setText((apkInfo.manifest.application.labels !=null)?apkInfo.manifest.application.labels[0].name : "");
		if(apkInfo.manifest.application.labels !=null) {
			applabelpanel.setText(apkInfo.manifest.application.labels[0].name);
			
			if(apkInfo.manifest.application.labels.length > 1){
				btnlabelcount.setText(apkInfo.manifest.application.labels.length +"");
				mutiLabels = setAppLabel(apkInfo.manifest.application.labels, apkInfo.manifest.packageName);
				labelcountpanel.setVisible(true);
			} else {
				labelcountpanel.setVisible(false);
			}
		}
		
		EasyGuiMain.UIstarttime =System.currentTimeMillis();		
		//package
		packagepanel.setText(apkInfo.manifest.packageName);		
		
		//version
		ininerversionpanel.setText(apkInfo.manifest.versionName + " / " + apkInfo.manifest.versionCode);
		
		//size
		//ininersizepanel.setText(FileUtil.getFileSize(apkInfo.fileSize, FSStyle.FULL));
		devicepanel.setsdkpanel(apkInfo);		
		
		//feature
		featurepanel.setfeature(apkInfo);
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_APKINFO);
		
		//permissions
		permissionPanel.setPermission(apkInfo);
	}

	public void clear() {
		// TODO Auto-generated method stub
		//devicepanel.clear();
		permissionPanel.clear();
		//featurepanel.clear();
	}

	public void changeDeivce(IDevice[] devices) {
		// TODO Auto-generated method stub
		devicepanel.changeDevice(devices);
	}
}
