package com.apkscanner.gui.installer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.apkscanner.resource.RStr;
import com.apkspectrum.core.installer.OptionsBundle;

public class ToggleButtonBar extends JPanel {

    private static final long serialVersionUID = -8497802681135584399L;

    public static final int OPTION_INSTALL = 0;
    public static final int OPTION_NO_INSTALL = 1;
    public static final int OPTION_PUSH = 2;
    public static final int OPTION_IMPOSSIBLE_INSTALL = 3;
    public static final int WAITING = 4;

    public static final int INSTALLED = 0;
    public static final int NOT_INSTALLED = 1;

    public static final String ACT_CMD_PACKAGE_INFO = "BUTTON_TYPE_PACAKGE_INFO";
    public static final String ACT_CMD_BUILD_OPTTIONS = "BUTTON_TYPE_INSTALL_INFO";

    DeviceListData data;
    int status;
    private static final Color[] ColorSet =
            {new Color(0x50AF49), new Color(0xFF7400), new Color(0x5CD1E5), new Color(0x8b0000),
                    new Color(0x8b0000), new Color(0x8b0000), new Color(0x555555)};

    private String[] installtextSet = {RStr.BTN_INSTALLED.get(), RStr.BTN_NOT_INSTALLED.get(), "",
            "", RStr.BTN_WAITING.get()};
    private String[] installOptiontextSet = {"Install", RStr.BTN_NO_INSTALL.get(), "Push",
            RStr.BTN_IMPOSSIBLE_INSTALL.get(), RStr.BTN_WAITING.get()};

    private static final String[] installingtextSet =
            {RStr.LABEL_INSTALLING.get(), RStr.BTN_SUCCESS.get(), RStr.BTN_FAIL.get()};

    AbstractButton btninstalled;
    AbstractButton btnoption;

    public ToggleButtonBar(int state, ActionListener listener) {
        setLayout(new GridLayout(1, 0, 0, 0));

        btninstalled = makeButton("installed", ACT_CMD_PACKAGE_INFO, state, listener);
        btnoption = makeButton("install", ACT_CMD_BUILD_OPTTIONS, state, listener);

        btninstalled.setIcon(new ToggleButtonBarCellIcon());
        btnoption.setIcon(new ToggleButtonBarCellIcon());

        this.add(btnoption);
        this.add(btninstalled);
    }

    private static AbstractButton makeButton(String title, String ActionString, int level,
            ActionListener listener) {
        AbstractButton b = new JButton(title);
        // b.setVerticalAlignment(SwingConstants.CENTER);
        // b.setVerticalTextPosition(SwingConstants.CENTER);
        // b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setActionCommand(ActionString);
        // b.setBackground(new Color(cc));
        b.setForeground(Color.WHITE);

        b.setBackground(ColorSet[level]);
        b.addActionListener(listener);

        return b;
    }

    static class ToggleButtonBarCellIcon implements Icon {
        private static final Color TL = new Color(1f, 1f, 1f, .2f);
        private static final Color BR = new Color(0f, 0f, 0f, .2f);
        private static final Color ST = new Color(1f, 1f, 1f, .4f);
        private static final Color SB = new Color(1f, 1f, 1f, .1f);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Container parent = c.getParent();
            if (parent == null) {
                return;
            }
            int r = 8;
            int w = c.getWidth();
            int h = c.getHeight() - 1;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Path2D p = new Path2D.Double();
            if (c == parent.getComponent(0)) {
                // :first-child
                p.moveTo(x, y + r);
                p.quadTo(x, y, x + r, y);
                p.lineTo(x + w, y);
                p.lineTo(x + w, y + h);
                p.lineTo(x + r, y + h);
                p.quadTo(x, y + h, x, y + h - r);
            } else if (c == parent.getComponent(parent.getComponentCount() - 1)) {
                // :last-child
                w--;
                p.moveTo(x, y);
                p.lineTo(x + w - r, y);
                p.quadTo(x + w, y, x + w, y + r);
                p.lineTo(x + w, y + h - r);
                p.quadTo(x + w, y + h, x + w - r, y + h);
                p.lineTo(x, y + h);
            } else {
                p.moveTo(x, y);
                p.lineTo(x + w, y);
                p.lineTo(x + w, y + h);
                p.lineTo(x, y + h);
            }
            p.closePath();

            Color ssc = TL;
            Color bgc = BR;
            if (c instanceof AbstractButton) {
                ButtonModel m = ((AbstractButton) c).getModel();
                if (m.isSelected() || m.isRollover()) {
                    ssc = ST;
                    bgc = SB;
                }
            }

            Area area = new Area(p);
            g2.setPaint(c.getBackground());
            g2.fill(area);
            g2.setPaint(new GradientPaint(x, y, ssc, x, y + h, bgc, true));
            g2.fill(area);
            g2.setPaint(BR);
            g2.draw(area);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 80;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }


    @Override
    synchronized protected void paintComponent(Graphics g) {

        OptionsBundle bundle = data.getOptionsBundle();

        int optType = WAITING;
        if (data.getState() != DeviceListData.STATUS_CONNECTING_DEVICE) {
            if (bundle.isImpossibleInstallOptions()) {
                optType = OPTION_IMPOSSIBLE_INSTALL;
            } else if (bundle.isPushOptions()) {
                optType = OPTION_PUSH;
            } else if (bundle.isInstallOptions()) {
                optType = OPTION_INSTALL;
            } else if (bundle.isNotInstallOptions()) {
                optType = OPTION_NO_INSTALL;
            }
        }
        btnoption.setBackground(ColorSet[optType]);
        btnoption.setText(installOptiontextSet[optType]);

        int installedType = bundle.isInstalled() ? 0 : 1;

        switch (data.getState()) {
            case DeviceListData.STATUS_SETTING:
                btninstalled.setBackground(ColorSet[installedType]);
                btninstalled.setText(installtextSet[installedType]);
                break;
            case DeviceListData.STATUS_INSTALLING:
                btninstalled.setBackground(ColorSet[4]);
                btninstalled.setText(installingtextSet[0]);
                break;
            case DeviceListData.STATUS_SUCESSED:
                btninstalled.setBackground(ColorSet[0]);
                btninstalled.setText(installingtextSet[1]);
                break;
            case DeviceListData.STATUS_FAILED:
                btninstalled.setBackground(ColorSet[3]);
                btninstalled.setText(installingtextSet[2]);
                break;
            case DeviceListData.STATUS_NO_ACTION:
                btninstalled.setBackground(ColorSet[installedType]);
                btninstalled.setText(installtextSet[installedType]);
                break;
            case DeviceListData.STATUS_CONNECTING_DEVICE:
                btninstalled.setBackground(ColorSet[1]);
                btninstalled.setText(installtextSet[4]);
                break;
        }
        // Log.d("" +data.isinstalled);
    }

    public void setData(DeviceListData value) {
        this.data = value;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
