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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.android.ddmlib.IDevice;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.easymode.util.AndroidLikeToast;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFlatLabel;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.EasyRoundLabelCount;
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
	
	JPanel infopanel = new JPanel(new BorderLayout());
	JPanel iconhoverpanel;
	int SHADOWSIZE = 2;
	
	static public int WIDTH = 500;
	static public int HEIGHT = 230;
	
	static private int PACAKGEVERSION_HEIGHT = 35;
	
	//static private Color panelbackgroundcolor = new Color(217,217,217);
	
	
	static private Color panelbackgroundcolor = new Color(217,217,217);
	
	static private Color labelfontcolor = Color.black;
	
	static private Color packagefontcolor = Color.darkGray;
	static private Color versionfontcolor = Color.darkGray;
	
	static private Color sdkverPanelcolor = new Color(232,232,232);
	
	private static String CARD_LAYOUT_EMPTY = "card_empty";
	private static String CARD_LAYOUT_APKINFO = "card_apkinfo";	
	
	JLayeredPane layeredPane;
	
	//EasyRoundLabel applabelpanel;
	EasyRoundLabelCount applabelpanel;
	JPanel labeltemp;
	JPanel iconpanel;
	public EasyContentsPanel() {
		// TODO Auto-generated constructor stub
		Log.d("start EasyContentsPanel ");
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//setBackground(Color.RED);
		setOpaque(false);
		
		contentsCardPanel = new JPanel(new CardLayout());
		contentsCardPanel.add(makeapkinfoPanel(), CARD_LAYOUT_APKINFO);  //3x ms

		iconpanel = makeapkiconPanel();
		add(iconpanel, BorderLayout.WEST);
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
		return appiconpanel;
	}
	
	private void makelabelpanel() {
		labeltemp = new JPanel(new BorderLayout());
		labeltemp.setOpaque(false);
		
		applabelpanel = new EasyRoundLabelCount("", panelbackgroundcolor, labelfontcolor);
		applabelpanel.setPreferredSize(new Dimension(0, PACAKGEVERSION_HEIGHT));
		addClipBoardbutton(applabelpanel);
		applabelpanel.setshadowlen(SHADOWSIZE);
		applabelpanel.setTextFont(new Font(getFont().getName(), Font.PLAIN, 15));
		
		//applabelpanel.setForeground(labelfontcolor);
		
		
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
		//labelcountpanel.setBackground(Color.white);
		labelcountpanel.setOpaque(false);
		//labelcountpanel.setBackground(Color.BLACK);
		labelcountpanel.add(btnlabelcount);
		
		applabelpanel.addCountpanel(labelcountpanel);
		//labeltemp.add(labelcountpanel, BorderLayout.EAST);
		
	}
	
	private void addClipBoardbutton(final EasyRoundLabel panel) {
		//panel.setLayout(new BorderLayout());
//		EasyRoundButton btnshowpermissiondlg = new EasyRoundButton(Resource.IMG_EASY_WINDOW_CLIPBOARD_ICON.getImageIcon(15, 15));
//		
//		btnshowpermissiondlg.setPreferredSize(new Dimension(15, 15));
//		btnshowpermissiondlg.setBackground(panelbackgroundcolor);
//		btnshowpermissiondlg.addActionListener(new ActionListener() {			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				StringSelection stringSelection = new StringSelection(panel.getText());
//				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//				clipboard.setContents(stringSelection, null);
//				AndroidLikeToast.ShowToast("Copying to the clipboard!",panel);
//			}
//		});
//		panel.add(btnshowpermissiondlg, BorderLayout.EAST);
		panel.setMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
		        if (SwingUtilities.isRightMouseButton(e)) {
					StringSelection stringSelection = new StringSelection(panel.getText());
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
					AndroidLikeToast.ShowToast("Copying to the clipboard!",panel);
		        }
		            
		    }
		});
		
		//panel.add(btnshowpermissiondlg);
	}
	
	private void addClipBoardbutton(final EasyRoundLabelCount panel) {
		//panel.setLayout(new BorderLayout());
//		EasyRoundButton btnshowpermissiondlg = new EasyRoundButton(Resource.IMG_EASY_WINDOW_CLIPBOARD_ICON.getImageIcon(15, 15));
//		
//		btnshowpermissiondlg.setPreferredSize(new Dimension(15, 15));
//		btnshowpermissiondlg.setBackground(panelbackgroundcolor);
//		btnshowpermissiondlg.addActionListener(new ActionListener() {			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				StringSelection stringSelection = new StringSelection(panel.getText());
//				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//				clipboard.setContents(stringSelection, null);
//				AndroidLikeToast.ShowToast("Copying to the clipboard!",panel);
//			}
//		});
//		panel.add(btnshowpermissiondlg, BorderLayout.EAST);
		
		panel.setMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
		        if (SwingUtilities.isRightMouseButton(e)) {
					StringSelection stringSelection = new StringSelection(panel.getText());
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
					AndroidLikeToast.ShowToast("Copying to the clipboard!",panel);
		        }
		            
		    }
		});
		//panel.add(btnshowpermissiondlg);
	}
	
	private JComponent makeapkinfoPanel() {
		infopanel = new JPanel(new BorderLayout());
		JPanel packageandlabel = new JPanel(new BorderLayout());
		//packagecontentsCardPanel
		packagepanel = new EasyRoundLabel(" ", panelbackgroundcolor, packagefontcolor);
		packagepanel.setPreferredSize(new Dimension(0, PACAKGEVERSION_HEIGHT));
		addClipBoardbutton(packagepanel);
		packagepanel.setshadowlen(SHADOWSIZE);
		packagepanel.setTextFont(new Font(getFont().getName(), Font.PLAIN, 15));
		
		packageandlabel.add(packagepanel, BorderLayout.CENTER);
		makelabelpanel();
		packageandlabel.add(labeltemp, BorderLayout.NORTH);
		
		infopanel.add(packageandlabel, BorderLayout.NORTH);

		devicepanel = new EasyDevicePanel(50);
		devicepanel.setRoundrectColor(panelbackgroundcolor);
		//sdkverpanel.setPreferredSize(new Dimension(50, 0));
		devicepanel.setshadowlen(SHADOWSIZE);
		infopanel.add(devicepanel, BorderLayout.EAST);
		

		
		
		
		JPanel innerinfopanel = new JPanel(new BorderLayout());
		//version
		ininerversionpanel = new EasyRoundLabel(" ", panelbackgroundcolor, versionfontcolor);
		ininerversionpanel.setPreferredSize(new Dimension(0, PACAKGEVERSION_HEIGHT));
		ininerversionpanel.setTextFont(new Font(getFont().getName(), Font.PLAIN, 15));
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
		infopanel.setBounds(0, 0, WIDTH, HEIGHT);
		infopanel.setOpaque(false);
		
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		layeredPane.add(infopanel,new Integer(1));
		
		EasyGuiDeviceToolPanel toolbarpanel;
		
		toolbarpanel = new EasyGuiDeviceToolPanel(30, 100);
		toolbarpanel.setOpaque(false);
		
		iconhoverpanel = new JPanel(new BorderLayout());
		iconhoverpanel.add(toolbarpanel, BorderLayout.CENTER);		
		iconhoverpanel.setBounds(0, 100, WIDTH, HEIGHT);
		iconhoverpanel.setOpaque(false);
		iconhoverpanel.setVisible(false);
		
		devicepanel.setdevicetoolbar(toolbarpanel);
		
		//iconhoverpanel.setVisible(false);
		layeredPane.add(toolbarpanel,new Integer(2));
		
		return layeredPane;
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
    	appicon.setIcon(Resource.IMG_APP_ICON.getImageIcon(120, 120)); //10 ms
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
		
		EasyMainUI.UIstarttime =System.currentTimeMillis();		
		//package
		packagepanel.setText(apkInfo.manifest.packageName);		
		
		//version
		ininerversionpanel.setText(apkInfo.manifest.versionName + " / " + apkInfo.manifest.versionCode);
		
		//size
		//ininersizepanel.setText(FileUtil.getFileSize(apkInfo.fileSize, FSStyle.FULL));
		//devicepanel.setsdkpanel(apkInfo);		
		
		//feature
		featurepanel.setfeature(apkInfo);
		((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_APKINFO);
		
		//permissions
		//permissionPanel.setPermission(apkInfo);
	}

	public void clear() {
		// TODO Auto-generated method stub
		//devicepanel.clear();
		//permissionPanel.clear();
		//featurepanel.clear();
	}

	public void changeDeivce(IDevice[] devices) {
		// TODO Auto-generated method stub
		devicepanel.changeDevice(devices);
		featurepanel.refreshUI();
	}

	public void changesize(int contentw, int contenth) {
		// TODO Auto-generated method stub
		int w = contentw;
		int h = contenth;
				
		layeredPane.setPreferredSize(new Dimension(w - iconpanel.getWidth(), h));
		infopanel.setBounds(0, 0, w - iconpanel.getWidth(), h);
//		devicepanel.updatetoolbarPosition();
		//iconhoverpanel.setBounds(0, 100, w - iconpanel.getWidth(), h);
		
		//iconhoverpanel.setBounds(0, 0, w, 100);
		//updateUI();
	}
}
