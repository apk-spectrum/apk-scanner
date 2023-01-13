package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.android.ddmlib.IDevice;
import com.apkscanner.gui.easymode.contents.EasyToolIcon.EasyToolListner;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.GraphicUtil;
import com.apkscanner.gui.easymode.util.RoundPanel;

public class EasyGuiDeviceToolPanel extends JPanel implements ActionListener, EasyToolListner {
    private static final long serialVersionUID = 4941481653470088827L;

    int HEIGHT = 35;
    int WIDTH = 100;
    int BUTTON_IMG_SIZE = 35 - 6;
    int SHADOW_SIZE = 3;
    JPanel toolbartemppanel;
    ArrayList<ToolEntry> entrys;
    boolean drawtext = false;
    Point tooliconlocation = new Point();
    String iconlabel = "";
    IDevice selecteddevice;

    private ActionListener actionListener;

    public EasyGuiDeviceToolPanel(int height, int width) {
        HEIGHT = height;
        BUTTON_IMG_SIZE = 35;
        WIDTH = width;
        init();

        setPreferredSize(new Dimension(0, height));
        maketoolbutton();
    }

    public void setActionListener(ActionListener listener) {
        actionListener = listener;
    }

    private void init() {
        // setBackground(toobarPanelcolor);
        // setRoundrectColor(Color.LIGHT_GRAY);
        setPreferredSize(new Dimension(0, HEIGHT));
        setOpaque(false);
        setLayout(new BorderLayout());
        // setshadowlen(SHADOW_SIZE);
        // setshadowlen(1);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 20));

        RoundPanel roundpanel = new RoundPanel();
        roundpanel.setPreferredSize(new Dimension(0, 45));
        roundpanel.setOpaque(false);
        roundpanel.setRoundrectColor(Color.LIGHT_GRAY);
        toolbartemppanel = new JPanel();
        FlowLayout flowlayout = new FlowLayout(FlowLayout.CENTER, 3, 4);
        // toolbartemppanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        toolbartemppanel.setLayout(flowlayout);
        ((FlowLayout) toolbartemppanel.getLayout()).setAlignOnBaseline(true);
        toolbartemppanel.setOpaque(false);

        roundpanel.add(toolbartemppanel, BorderLayout.NORTH);
        add(toolbartemppanel, BorderLayout.NORTH);
    }

    public void paintComponent(Graphics g) {
        // super.paint(g);
        super.paintComponent(g);
        if (drawtext) {
            // int textwidth = iconlabel.length() * 20;
            // GraphicUtil.drawRoundrectText(g, tooliconlocation.x - textwidth/2, 70, textwidth, 30,
            // iconlabel);
            GraphicUtil.drawTextRoundrect(g, tooliconlocation.x, 38, 10, iconlabel);

        }
    }

    public void setSelecteddevice(IDevice device) {
        this.selecteddevice = device;
    }

    public IDevice getSelecteddevice() {
        return selecteddevice;
    }

    private void maketoolbutton() {
        toolbartemppanel.removeAll();
        entrys = ToolEntryManager.getDeviceToolbarList();
        for (ToolEntry entry : entrys) {
            // EasyFlatLabel btn = new
            // EasEasyGuiDeviceToolPanelyFlatLabel(ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE),
            // new Color(149, 179, 215));
            // Image img =
            // ImageUtils.getScaledImage(entry.getImage(),BUTTON_IMG_SIZE,BUTTON_IMG_SIZE);
            final EasyToolIcon btn = new EasyToolIcon(entry.getImage(), 25);
            btn.setScalesize(27);
            btn.setAction(entry.getActionCommand(), this);
            btn.setEasyToolListner(this);
            btn.setEasyText(entry.getTitle());

            toolbartemppanel.add(btn);
            toolbartemppanel.updateUI();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (actionListener == null) return;
        e.setSource(this);
        actionListener.actionPerformed(e);
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
