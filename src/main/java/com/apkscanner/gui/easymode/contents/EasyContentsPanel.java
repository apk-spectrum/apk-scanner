package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.SwingUtilities;

import com.android.ddmlib.IDevice;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.easymode.util.AndroidLikeToast;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.EasyRoundLabel;
import com.apkscanner.gui.easymode.util.EasyRoundLabelCount;
import com.apkscanner.gui.easymode.util.EasyTextField;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.MessageBoxPane;

public class EasyContentsPanel extends JPanel
{
    private static final long serialVersionUID = -1377658950522176543L;

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
    private static String CARD_LAYOUT_LOADING = "card_loading";

    JLayeredPane layeredPane;

    //EasyRoundLabel applabelpanel;
    EasyRoundLabelCount applabelpanel;
    JPanel labeltemp;
    JPanel iconpanel;

    public EasyContentsPanel(ActionListener listener) {
        Log.d("start EasyContentsPanel ");
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //setBackground(Color.RED);
        setOpaque(false);

        contentsCardPanel = new JPanel(new CardLayout());
        contentsCardPanel.add(makeapkinfoPanel(listener), CARD_LAYOUT_APKINFO);  //3x ms

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
                showDialog(mutiLabels, RStr.LABEL_APP_NAME_LIST.getString(), new Dimension(300, 200), null);
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
//		EasyRoundButton btnshowpermissiondlg = new EasyRoundButton(RImg.EASY_WINDOW_CLIPBOARD_ICON.getImageIcon(15, 15));
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
//		EasyRoundButton btnshowpermissiondlg = new EasyRoundButton(RImg.EASY_WINDOW_CLIPBOARD_ICON.getImageIcon(15, 15));
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

    private JComponent makeapkinfoPanel(ActionListener listener) {
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
        layeredPane.add(infopanel, Integer.valueOf(1));

        EasyGuiDeviceToolPanel toolbarpanel;

        toolbarpanel = new EasyGuiDeviceToolPanel(30, 100);
        toolbarpanel.setActionListener(listener);
        toolbarpanel.setOpaque(false);

        iconhoverpanel = new JPanel(new BorderLayout());
        iconhoverpanel.add(toolbarpanel, BorderLayout.CENTER);
        iconhoverpanel.setBounds(0, 100, WIDTH, HEIGHT);
        iconhoverpanel.setOpaque(false);
        iconhoverpanel.setVisible(false);

        devicepanel.setdevicetoolbar(toolbarpanel);

        //iconhoverpanel.setVisible(false);
        layeredPane.add(toolbarpanel, Integer.valueOf(2));

        return layeredPane;
    }

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(HEIGHT, WIDTH);
//    }

    public void setLoadingpanel(String msg) {
        Log.d("setLoadingpanel");
        contentsCardPanel.add(new EasyGuiLoadingPanel(msg), CARD_LAYOUT_LOADING);
        ((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_LOADING);
    }

    public void setEmptypanel() {
        Log.d("contents emptypanel=)" + emptypanel);
        if(emptypanel ==null) {
            emptypanel = new EasyGuiEmptyPanel();
            Log.d("contents new (EasyGuiEmptyPanel=)");
            contentsCardPanel.add(new EasyGuiEmptyPanel(), CARD_LAYOUT_EMPTY);
        }
        appicon.setIcon(RImg.APP_ICON.getImageIcon(120, 120)); //10 ms
        //apptitlelabel.setText(Rstr.APP_NAME.get()); // 20-30ms
        ((CardLayout)contentsCardPanel.getLayout()).show(contentsCardPanel,CARD_LAYOUT_EMPTY);
    }

    private ImageIcon getAppicon(ApkInfo apkInfo) {
        String iconPath = null;
        if(apkInfo.manifest.application.icons != null && apkInfo.manifest.application.icons.length > 0) {
            ResourceInfo[] iconList = apkInfo.manifest.application.icons;
            for(int i=iconList.length-1; i >= 0; i--) {
                iconPath = iconList[i].name;
                if(iconPath == null || iconPath.endsWith(".xml")) continue;
                if(iconPath.toLowerCase().endsWith(".webp")) {
                    iconPath = ImageUtils.covertWebp2Png(iconPath, apkInfo.tempWorkPath);
                }
                if(iconPath != null) {
                    Log.d("icon Path is " + iconPath);
                    break;
                }
            }
        }
        //appicon set
        //String temppath = apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length - 1].name;
        ImageIcon icon;
        if(iconPath == null) {
            icon = RImg.DEF_APP_ICON.getImageIcon(110, 110);
        } else if(iconPath.toLowerCase().endsWith(".qmg")) {
            icon = RImg.QMG_IMAGE_ICON.getImageIcon(110, 110);
        } else {
            try {
                icon = new ImageIcon(ImageUtils.getScaledImage(new ImageIcon(ImageIO.read(new URL(iconPath))),110,110));
            } catch (IOException e) {
                Log.e(e.getMessage());
                icon = RImg.DEF_APP_ICON.getImageIcon();
            }
        }
        return icon;
    }

    private void showDialog(String content, String title, Dimension size, Icon icon) {
        MessageBoxPane.showTextAreaDialog(this, content, title, MessageBoxPane.INFORMATION_MESSAGE, icon, size);
    }

    private String setAppLabel(ResourceInfo[] labels, String packageName) {
        String appName = null;
        StringBuilder labelBuilder = new StringBuilder();
        if(labels != null && labels.length > 0) {
            appName = ApkInfoHelper.getResourceValue(labels, RProp.S.PREFERRED_LANGUAGE.get());
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
    public void setsignfeature(ApkInfo apkInfo) {
        featurepanel.setfeature(apkInfo);
    }
    
    
    public void clear() {
        //devicepanel.clear();
        //permissionPanel.clear();
        //featurepanel.clear();
    }

    public void changeDeivce(IDevice[] devices) {
        devicepanel.changeDevice(devices);
        featurepanel.refreshUI();
    }

    public void changesize(int contentw, int contenth) {
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
