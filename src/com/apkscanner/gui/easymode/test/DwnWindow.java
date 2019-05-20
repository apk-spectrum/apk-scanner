package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class DwnWindow extends JFrame
{
    public static void main(String... args)
    throws Exception
    {
        setDefaultLookAndFeelDecorated(true);

        DwnWindow f = new DwnWindow();
        f.setSize(500, 600);
        f.setBackground(new Color(0,0,0,0));
        f.setTitle("Hello");
        TranslucentPanel panel = new TranslucentPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("My background is blurry!");
        label.setFont(new Font("Dialog", Font.BOLD, 48));
        label.setPreferredSize(new Dimension(500, 500));
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(new JLabel("dddddd"), BorderLayout.SOUTH);
        
        f.add(panel);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.setVisible(true);

//        HWND hwnd = new HWND(Native.getWindowPointer(f));
//        Dwmapi.DWM_BLURBEHIND pBlurBehind = new Dwmapi.DWM_BLURBEHIND();
//        pBlurBehind.dwFlags = Dwmapi.DWM_BB_ENABLE;
//        pBlurBehind.fEnable = true;
//        pBlurBehind.fTransitionOnMaximized = false;
//        Dwmapi.INSTANCE.DwmEnableBlurBehindWindow(hwnd, pBlurBehind);
    }

    private static class TranslucentPanel extends JPanel
    {
        @Override
        protected void paintComponent(Graphics g) 
        {
        	//super.paintComponent(g);
        	//
            if (g instanceof Graphics2D) {
                final int R = 255;
                final int G = 255;
                final int B = 255;

                //Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 0), 0.0f, getHeight(), new Color(R, G, B, 255), true);
                Graphics2D g2d = (Graphics2D)g;
                //g2d.setPaint(p);
                
                g2d.setColor(new Color(R,G,B,0));
                g2d.fillRect(0, 0, 500, 500);
                
                g2d.setColor(new Color(0,0,0,255));
                g2d.fillRect(0, 500, 500, 100);
                //
            }
            
        }
    }

    public static interface Dwmapi extends StdCallLibrary
    {
        Dwmapi INSTANCE = (Dwmapi)Native.loadLibrary("Dwmapi", Dwmapi.class, W32APIOptions.UNICODE_OPTIONS);

        int DWM_BB_ENABLE = 0x00000001;

        boolean DwmEnableBlurBehindWindow(HWND hWnd, DWM_BLURBEHIND pBlurBehind);

        public static class DWM_BLURBEHIND extends Structure 
        {
            public int dwFlags;
            public boolean fEnable;
            public IntByReference hRgnBlur;
            public boolean fTransitionOnMaximized;

            @Override
            protected List getFieldOrder() 
            {
                return Arrays.asList("dwFlags", "fEnable", "hRgnBlur", "fTransitionOnMaximized");
            }
        }
    }
}