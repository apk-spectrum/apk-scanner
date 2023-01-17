package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GraphicUtil {
    public static int getStringWidth(Graphics page, Font f, String s) {
        // Find the size of string s in the font of the Graphics context "page"
        FontMetrics fm = page.getFontMetrics(f);
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
        return (int) Math.round(rect.getWidth());
    }

    public static int getStringHeight(Graphics page, Font f, String s) {
        // Find the size of string s in the font of the Graphics context "page"
        FontMetrics fm = page.getFontMetrics(f);
        java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, page);
        return (int) Math.round(rect.getHeight());
    }

    public static int getStringAscent(Graphics page, Font f, String s) {
        // Find the size of string s in the font of the Graphics context "page"
        FontMetrics fm = page.getFontMetrics(f);
        return fm.getAscent();
    }

    public static void drawCenteredString(Graphics page, String s, int x, int y, int width,
            int height) {
        Font font = page.getFont();
        int textWidth = getStringWidth(page, font, s);
        int textHeight = getStringHeight(page, font, s);
        int textAscent = getStringAscent(page, font, s);

        // Center text horizontally and vertically within provided rectangular
        // bounds
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textHeight) / 2 + textAscent;
        page.drawString(s, textX, textY);
    }

    public static void drawRoundrectText(Graphics page, int boxLeft, int boxTop, int width,
            int height, String text) {

        int margin = Math.min(width, height) / 10; // margin around the window
        int boxWidth = width - 2 * margin, boxHeight = height - 2 * margin;

        Graphics2D graphics2D = (Graphics2D) page;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Set anti-alias for text
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);


        // paint the box
        page.setColor(new Color(64, 64, 64));
        page.fillRoundRect(boxLeft, boxTop, boxWidth, boxHeight, 20, 20);

        // find the largest dimensions of the string that can fit in the window
        int fontSize = 4;
        while (true) {
            Font f = new Font("SansSerif", Font.BOLD, fontSize + 1);
            if ((getStringWidth(page, f, text) >= boxWidth)
                    || (getStringHeight(page, f, text) >= boxHeight))
                break;
            fontSize++;
        }

        // center a string in the box with a font-size of 32 pixels
        page.setColor(Color.WHITE);
        Font f = new Font("SansSerif", Font.BOLD, fontSize);
        page.setFont(f);
        drawCenteredString(page, text, boxLeft, boxTop, boxWidth, boxHeight);
    }

    public static void drawTextRoundrect(Graphics page, int boxLeft, int boxTop, int fontsize,
            String text) {

        int margin = 5;
        int boxWidth; // = width - 2*margin
        int boxHeight; // = height - 2*margin;
        final Color backgroundcolor = new Color(0.25f, 0.25f, 0.25f, 0.7f);
        final Color foregroundcolor = new Color(0.99f, 0.99f, 0.99f, 0.99f);
        Graphics2D graphics2D = (Graphics2D) page;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Set anti-alias for text
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);



        // find the largest dimensions of the string that can fit in the window
        Font f = new Font("SansSerif", Font.BOLD, fontsize);
        boxWidth = getStringWidth(page, f, text) + margin * 2 + 15;
        boxHeight = getStringHeight(page, f, text) + margin * 2;

        // paint the box
        page.setColor(backgroundcolor);
        page.fillRoundRect(boxLeft - boxWidth / 2, boxTop, boxWidth, boxHeight, 30, 30);


        // center a string in the box with a font-size of 32 pixels
        page.setColor(foregroundcolor);
        f = new Font("SansSerif", Font.BOLD, fontsize);
        page.setFont(f);
        drawCenteredString(page, text, boxLeft - boxWidth / 2, boxTop, boxWidth, boxHeight);
    }
}
