package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.easymode.contents.EasyToolIcon.EasyToolListner;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.EasyRoundButton;
import com.apkscanner.gui.easymode.util.GraphicUtil;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkspectrum.logback.Log;

public class EasyGuiToolScaleupPanel extends JPanel
        implements EasyToolListner, PropertyChangeListener {
    private static final long serialVersionUID = 4941481653470088827L;

    int HEIGHT = 35;
    int WIDTH = 100;
    int BUTTON_IMG_SIZE = 35 - 6;
    int SHADOW_SIZE = 3;
    static private Color toobarPanelcolor = new Color(232, 241, 222);
    JPanel toolbartemppanel;
    EasyRoundButton btnsetting;
    ArrayList<ToolEntry> entrys;
    boolean drawtext = false;
    Point tooliconlocation = new Point();
    String iconlabel = "";
    String defaultApk = "Detail APK";
    EasyToolIcon open_detail_apk_btn;

    private ActionListener actionListener;

    public EasyGuiToolScaleupPanel(int height, int width, ActionListener listener) {
        HEIGHT = height;
        BUTTON_IMG_SIZE = 35;
        WIDTH = width;
        entrys = ToolEntryManager.getShowToolbarList();
        this.actionListener = listener;
        init();
        setPreferredSize(new Dimension(0, height));
        maketoolbutton();
    }

    private void init() {
        setBackground(toobarPanelcolor);
        setPreferredSize(new Dimension(0, HEIGHT));
        setOpaque(false);
        setLayout(new BorderLayout());
        // setshadowlen(SHADOW_SIZE);
        // setshadowlen(1);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 20));

        toolbartemppanel = new JPanel();
        FlowLayout flowlayout = new FlowLayout(FlowLayout.CENTER, 3, 0);
        // toolbartemppanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        toolbartemppanel.setLayout(flowlayout);
        ((FlowLayout) toolbartemppanel.getLayout()).setAlignOnBaseline(true);
        toolbartemppanel.setOpaque(false);
        add(toolbartemppanel, BorderLayout.CENTER);



        open_detail_apk_btn = new EasyToolIcon(RImg.EASY_WINDOW_SPREAD.getImageIcon(), 30);
        open_detail_apk_btn.setScalesize(60);

        open_detail_apk_btn.setAction(UiEventHandler.ACT_CMD_CHANGE_UI_MODE, actionListener);
        open_detail_apk_btn.setEasyToolListner(this);
        open_detail_apk_btn.setEasyText(defaultApk);
        JPanel temp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        ((FlowLayout) temp.getLayout()).setAlignOnBaseline(true);
        temp.setOpaque(false);
        temp.add(open_detail_apk_btn);
        // temp.setPreferredSize(new Dimension(30, 30));
        add(temp, BorderLayout.EAST);
        // add(open_detail_apk_btn, BorderLayout.LINE_END);
        // toolbartemppanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // ((FlowLayout) this.getLayout()).setAlignOnBaseline(true);

        // btnsetting = new EasyButton(Resource.IMG_EASY_WINDOW_SETTING.getImageIcon(15, 15));
        // btnsetting.setPreferredSize(new Dimension(15, 35));
        // btnsetting.addActionListener(this);
        // add(btnsetting);

    }

    public void paintComponent(Graphics g) {
        // super.paint(g);
        // super.paintComponent(g);
        // Graphics2D graphics2D = (Graphics2D) g;
        // graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        // RenderingHints.VALUE_ANTIALIAS_ON);
        // Font font = new Font("Serif", Font.BOLD, 20);
        // graphics2D.setFont(font);
        // graphics2D.drawString("Support", 100, 80);
        if (drawtext) {
            // int textwidth = iconlabel.length() * 20;
            // GraphicUtil.drawRoundrectText(g, tooliconlocation.x - textwidth/2, 70, textwidth, 30,
            // iconlabel);
            GraphicUtil.drawTextRoundrect(g, tooliconlocation.x, 67, 15, iconlabel);

        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void maketoolbutton() {
        toolbartemppanel.removeAll();
        entrys = ToolEntryManager.getShowToolbarList();
        for (ToolEntry entry : entrys) {
            // EasyFlatLabel btn = new
            // EasyFlatLabel(ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE),
            // new Color(149, 179, 215));
            // Image img =
            // ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE);
            final EasyToolIcon btn = new EasyToolIcon(entry.getImage(), 30);
            // btn.setAlignmentY(Component.TOP_ALIGNMENT);
            // btn.setText("aaaa");
            // btn.setIcon(new ImageIcon(img));
            // btn.setPreferredSize(new Dimension(BUTTON_IMG_SIZE, BUTTON_IMG_SIZE));
            btn.setScalesize(60);
            btn.setAction(entry.getActionCommand(), actionListener);
            btn.setEasyToolListner(this);
            btn.setEasyText(entry.getTitle());
            // btn.setOpaque(true);
            // btn.setactionCommand(entry.getTitle());

            // btn.setshadowlen(SHADOW_SIZE);
            // btn.setTooltip(entry.getTitle());
            // btn.setClicklistener(this);


            toolbartemppanel.add(btn);
            toolbartemppanel.updateUI();
        }
        btnsetting = new EasyRoundButton(RImg.EASY_WINDOW_SETTING.getImageIcon(20, 20)) {
            private static final long serialVersionUID = 2208337293930490795L;

            @Override
            public int getBaseline(int width, int height) {
                return 0;
            }
        };
        btnsetting.setActionCommand(UiEventHandler.ACT_CMD_EDIT_EASY_TOOLBAR);
        btnsetting.setPreferredSize(new Dimension(30, 30));
        btnsetting.addActionListener(actionListener);
        toolbartemppanel.add(btnsetting);

        RProp.EASY_GUI_TOOLBAR.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.v("propertyChange()" + evt);
        maketoolbutton();
    }

    @Override
    public void changestate(int state, EasyToolIcon easyiconlabel) {
        switch (state) {
            case EasyToolListner.STATE_ANIMATION_END:
                drawtext = true;
                tooliconlocation.x =
                        easyiconlabel.getParent().getLocation().x + easyiconlabel.getLocation().x
                                + (int) (easyiconlabel.getBounds().getWidth() / 2);

                // Log.d(easyiconlabel.getBounds() + "");
                // Log.d(toolbartemppanel.getLocation().x + " : " + easyiconlabel.getLocation().x);

                iconlabel = easyiconlabel.getEasyText();
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
