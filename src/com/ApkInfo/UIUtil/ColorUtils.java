package com.ApkInfo.UIUtil;

/**
 * Copyright (c) 2012, Dhilshuk Reddy All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute SwingJD software is freely
 * granted, provided that this notice is preserved.
 */
import java.awt.Color;
import java.awt.GradientPaint;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * 
 * @author dhilshuk
 * 
 */
public class ColorUtils {

	//private Map<Integer, GradientPaint> colorMap = new HashMap<Integer, GradientPaint>();

	private static ColorUtils colorUtils;

	private GradientPaint buttonColor;

	private GradientPaint gradientBtnColor;

	private GradientPaint glossyTopBtnColor;

	private GradientPaint glossyBtnColor;

	private GradientPaint[] glossyColors = new GradientPaint[2];

	/**
	 * 
	 * @return
	 */
	public static ColorUtils getInStance() {
		if (colorUtils == null) {
			colorUtils = new ColorUtils();
		}
		return colorUtils;
	}

	/**
	 * 
	 * @param theme
	 * @param height
	 * @param button
	 * @return
	 */
	public GradientPaint getStandardColor(int theme, int height, JButton button) {
		switch (theme) {
		case Theme.STANDARD_DARKGREEN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(0, 140, 0), 0,
					3 * height / 4, new Color(0, 85, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_BLUEGREEN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(31, 175, 114), 0,
					height, new Color(20, 113, 74));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_GREEN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(102, 223, 36), 0,
					height, new Color(68, 154, 23));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_LIGHTGREEN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(121, 232, 98), 0,
					3 * height / 4, new Color(61, 208, 31));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_OLIVEGREEN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(117, 198, 6), 0,
					height, new Color(68, 116, 4));
			button.setForeground(Color.WHITE);
			break;

		case Theme.STANDARD_LIME_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(181, 223, 38), 0,
					3 * height / 4, new Color(137, 170, 26));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_RED_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(255, 100, 100), 0,
					height, new Color(255, 0, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_DARKRED_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(255, 0, 6), 0,
					height, new Color(181, 0, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_ORANGE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(251, 139, 62), 0,
					height, new Color(255, 102, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_LIGHTORANGE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(247, 174, 24), 0,
					height, new Color(255, 133, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_DARKYELLOW_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(185, 181, 0), 0,
					height, new Color(123, 120, 0));
			button.setForeground(Color.WHITE);
			break;

		case Theme.STANDARD_GREENYELLOW_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(253, 247, 11), 0,
					height, new Color(211, 204, 2));
			button.setForeground(Color.BLACK);
			break;

		case Theme.STANDARD_GOLD_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(255, 233, 18), 0,
					height, new Color(255, 213, 0));
			button.setForeground(Color.BLACK);
			break;
		case Theme.STANDARD_YELLOW_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(255, 255, 166), 0,
					height, new Color(255, 255, 56));
			button.setForeground(Color.BLACK);
			break;

		case Theme.STANDARD_BROWN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(202, 62, 2), 0,
					3 * height / 4, new Color(118, 35, 1));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_LIGHTBROWN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(232, 194, 125), 0,
					3 * height / 4, new Color(212, 151, 37));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_PALEBROWN_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(248, 234, 203), 0,
					3 * height / 4, new Color(236, 205, 132));
			button.setForeground(Color.BLACK);
			break;
		case Theme.STANDARD_NAVYBLUE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(44, 105, 180), 0,
					height, new Color(5, 25, 114));
			button.setForeground(Color.WHITE);
			break;

		case Theme.STANDARD_INDIGO_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(49, 120, 206), 0,
					height, new Color(35, 84, 146));
			button.setForeground(Color.WHITE);

			break;

		case Theme.STANDARD_BLUE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(58, 92, 252), 0,
					height, new Color(3, 37, 188));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_SKYBLUE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(17, 136, 255), 0,
					height, new Color(0, 96, 194));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_LIGHTBLUE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(51, 191, 238), 0,
					height, new Color(17, 160, 208));

			button.setForeground(Color.WHITE);

			break;

		case Theme.STANDARD_DARKPURPLE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(82, 0, 164), 0,
					height, new Color(44, 0, 89));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_PURPLE_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(203, 64, 239), 0,
					height, new Color(186, 0, 255));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_LAVENDER_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(165, 117, 239), 0,
					height, new Color(107, 60, 173));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_DARKPINK_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(170, 0, 128), 0,
					height, new Color(115, 0, 85));
			button.setForeground(Color.WHITE);

			break;

		case Theme.STANDARD_PINK_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(238, 83, 133), 0,
					height, new Color(220, 22, 86));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_PALEPINK_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(255, 174, 235), 0,
					height, new Color(255, 128, 223));
			button.setForeground(Color.WHITE);

			break;

		case Theme.STANDARD_BLACK_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(90, 90, 90), 0,
					height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;

		case Theme.STANDARD_GRAY_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(90, 90, 90), 0,
					height, new Color(70, 70, 70));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_LIGHTGRAY_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(163, 163, 163), 0,
					height, new Color(128, 128, 128));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_METALLICGRAY_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(151, 164, 170), 0,
					3 * height / 4, new Color(120, 137, 145));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_BLUEGRAY_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(68, 113, 153), 0,
					height, new Color(32, 53, 72));
			button.setForeground(Color.WHITE);

			break;
		case Theme.STANDARD_VOILET_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(148, 148, 255), 0,
					height, new Color(98, 98, 255));
			button.setForeground(Color.WHITE);
			break;
		case Theme.STANDARD_SILVER_THEME:
			buttonColor = new GradientPaint(0, 0, new Color(236, 241, 242), 0,
					height, new Color(206, 220, 223));
			button.setForeground(Color.BLACK);
			break;
		default:
			buttonColor = new GradientPaint(0, 0, new Color(149, 159, 207), 0,
					height, new Color(85, 134, 194));
			button.setForeground(Color.BLACK);
			break;

		}
		;
		return buttonColor;
	}

	/**
	 * Returns Gradient Color
	 * 
	 * @param theme
	 *            theme
	 * @param height
	 *            height of the button
	 * @param button
	 *            button
	 * @return
	 */
	public GradientPaint getGradientColor(int theme, int height,
			JComponent button) {
		switch (theme) {
		case Theme.GRADIENT_DARKGREEN_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(136, 255, 136), 0, height, new Color(1, 54, 2));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_BLUEGREEN_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(170, 240, 210), 0, height, new Color(12, 69, 45));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_GREEN_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(73, 252, 7),
					0, height, new Color(0, 64, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_OLIVEGREEN_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(185, 234, 36),
					0, height, new Color(68, 116, 4));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_LIME_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(217, 242, 138), 0, height,
					new Color(168, 216, 24));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GRADIENT_LIGHTGREEN_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(159, 255, 159), 0, height, new Color(61, 208, 31));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GRADIENT_RED_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(249, 200, 0),
					0, height, new Color(242, 40, 30));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_DARKRED_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(249, 200, 0),
					0, height, new Color(181, 0, 0));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_ORANGE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(255, 197, 63),
					0, height, new Color(255, 102, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_LIGHTORANGE_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(255, 133, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_DARKYELLOW_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(123, 120, 0));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GRADIENT_GREENYELLOW_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(211, 204, 2));
			button.setForeground(Color.BLACK);
			break;
		case Theme.GRADIENT_GOLD_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height,
					new Color(255, 201, 14));
			button.setForeground(Color.BLACK);
			break;
		case Theme.GRADIENT_YELLOW_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height,
					new Color(255, 255, 56));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GRADIENT_NAVYBLUE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(71, 232, 252),
					0, height, new Color(5, 25, 114));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_INDIGO_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(34, 85, 146));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_BLUE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(71, 232, 252),
					0, height, new Color(3, 37, 188));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_SKYBLUE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(71, 232, 252),
					0, height, new Color(6, 113, 196));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_LIGHTBLUE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(71, 232, 252),
					0, height, new Color(17, 160, 208));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_DARKPURPLE_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(186, 0, 255),
					0, height, new Color(44, 0, 89));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_VOILET_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(170, 170, 255), 0, height, new Color(98, 98, 255));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_PURPLE_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height,
					new Color(186, 60, 255));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_LAVENDER_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(192, 128,
							255));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_DARKPINK_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(115, 0, 85));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GRADIENT_PINK_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(220, 22, 86));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_PALEPINK_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height, new Color(255, 128,
							223));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_BLACK_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(150, 150, 150), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_SILVER_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(218, 228, 231), 0, height, new Color(255, 0, 0));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GRADIENT_BROWN_THEME:
			gradientBtnColor = new GradientPaint(0, 0, new Color(202, 62, 2),
					0, height, new Color(118, 35, 1));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GRADIENT_LIGHTBROWN_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(232, 194, 125), 0, height,
					new Color(212, 151, 37));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GRADIENT_PALEBROWN_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(248, 234, 203), 0, height, new Color(236, 205,
							132));
			button.setForeground(Color.BLACK);
			break;

		/*
		 * case ThemeType.GRADIENT_BLUEGRAY_THEME: gradientBtnColor = new
		 * GradientPaint(0, 0, new Color(128, 128, 255), 0, height, new
		 * Color(32, 53, 72)); button.setForeground(Color.WHITE); break;
		 */case Theme.GRADIENT_GRAY_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(200, 200, 200), 0, height, new Color(70, 70, 70));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GRADIENT_LIGHTGRAY_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(183, 183, 183), 0, height, new Color(128, 128,
							128));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GRADIENT_METALLICGRAY_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(205, 210, 214), 0, 3 * height / 4, new Color(120,
							137, 145));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GRADIENT_BLUEGRAY_THEME:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(141, 175, 205), 0, height, new Color(32, 53, 72));
			button.setForeground(Color.WHITE);

			break;
		default:
			gradientBtnColor = new GradientPaint(0, 0,
					new Color(149, 159, 207), 0, height,
					new Color(85, 134, 194));
			button.setForeground(Color.BLACK);
			break;

		}
		;

		return gradientBtnColor;
	}

	/**
	 * 
	 * @param theme
	 * @param height
	 * @param button
	 * @return
	 */
	public GradientPaint[] getGlossyColor(int theme, int height,
			JComponent button) {
		switch (theme) {
		case Theme.GLOSSY_DARKGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(3, 167, 7),
					0, height / 2, new Color(2, 117, 5, 150));
			glossyBtnColor = new GradientPaint(0, height, new Color(1, 54, 2),
					0, height, new Color(1, 54, 2));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_BLUEGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(72, 223, 159), 0, height / 2, new Color(41, 218,
							142, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(20,
					113, 74), 0, height, new Color(20, 113, 74));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_LIGHTGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(219, 255, 202), 0, height / 2, new Color(219,
							255, 187, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(97,
					204, 0), 0, height, new Color(97, 204, 0));
			button.setForeground(Color.BLACK);

			break;

		case Theme.GLOSSY_GREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(211, 237, 194), 0, height / 2, new Color(109,
							176, 71));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(68,
					154, 23), 0, height, new Color(68, 154, 23));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_LIME_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(231, 247, 183), 0, height / 2, new Color(192,
							234, 68));
			glossyBtnColor = new GradientPaint(0, height, new Color(168, 216,
					24), 0, height, new Color(168, 216, 24));
			button.setForeground(Color.BLACK);

			break;
		case Theme.GLOSSY_OLIVEGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(138, 234, 9),
					0, height / 2, new Color(128, 216, 7, 100));
			glossyBtnColor = new GradientPaint(0, 0, new Color(68, 116, 4), 0,
					height, new Color(68, 116, 4));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_RED_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(247, 166, 166), 0, height / 2, new Color(242,
							107, 107));
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 233, 232), 0, height / 2, new Color(255,
							160, 160));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(233, 0,
					0), 0, height, new Color(233, 0, 0));
			glossyBtnColor = new GradientPaint(0, 0, new Color(255, 0, 0), 0,
					height, new Color(255, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_DARKRED_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 191, 191), 0, height / 2, new Color(255,
							174, 174, 150));
			/*
			 * glossyTopBtnColor = new GradientPaint(0, 0, new Color(255, 233,
			 * 232), 0, height / 2, new Color(255, 160, 160));
			 */
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(233, 0,
					0), 0, height, new Color(233, 0, 0));
			glossyBtnColor = new GradientPaint(0, 0, new Color(181, 0, 0), 0,
					height, new Color(181, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_ORANGE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(240, 240, 240), 0, height / 2, new Color(246,
							147, 90, 200));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(255,
					102, 0), 0, height, new Color(255, 102, 0));

			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_LIGHTORANGE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(250, 250, 250), 0, height / 2, new Color(255,
							216, 176, 150));
			glossyBtnColor = new GradientPaint(0, height,
					new Color(255, 153, 0), 0, height, new Color(255, 153, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_GREENYELLOW_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(253, 247, 15), 0, height / 2, new Color(253, 247,
							15, 150));
			glossyBtnColor = new GradientPaint(0, height,
					new Color(211, 204, 2), 0, height, new Color(211, 204, 2));
			button.setForeground(Color.BLACK);

			break;

		case Theme.GLOSSY_DARKYELLOW_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(221, 216, 0),
					0, height / 2, new Color(187, 183, 0, 150));
			glossyBtnColor = new GradientPaint(0, height,
					new Color(123, 120, 0), 0, height, new Color(123, 120, 0));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_GOLD_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height / 2, new Color(255,
							230, 108));
			glossyBtnColor = new GradientPaint(0, height,
					new Color(255, 213, 0), 0, height, new Color(255, 213, 0));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GLOSSY_YELLOW_THEME:
			glossyBtnColor = new GradientPaint(0, 0, new Color(254, 188, 16),
					0, height, new Color(252, 201, 56));
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(254, 239, 192), 0, height / 2, new Color(254,
							227, 147, 150));
			button.setForeground(Color.BLACK);
			break;
		case Theme.GLOSSY_BROWN_THEME:
			glossyBtnColor = new GradientPaint(0, 0, new Color(118, 35, 1), 0,
					height, new Color(118, 35, 1));
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(254, 173, 139), 0, height / 2, new Color(253,
							115, 55, 100));

			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_LIGHTBROWN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(240, 215, 166), 0, height / 2, new Color(226,
							179, 88));

			glossyBtnColor = new GradientPaint(0, 0, new Color(212, 151, 37),
					0, height, new Color(212, 151, 37));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_PALEBROWN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(253, 250, 242), 0, height / 2, new Color(242,
							221, 170));

			glossyBtnColor = new GradientPaint(0, 0, new Color(236, 205, 132),
					0, height, new Color(236, 205, 132));
			button.setForeground(Color.BLACK);
			break;

		case Theme.GLOSSY_NAVYBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(188, 200, 252), 0, height / 2, new Color(188,
							200, 252, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(5, 25,
					114), 0, height, new Color(5, 25, 114));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_BLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(121, 145, 223), 0, height / 2, new Color(121,
							145, 223, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(3, 37,
					188), 0, height, new Color(3, 37, 188));
			button.setForeground(Color.WHITE);
			break;

		case Theme.GLOSSY_INDIGO_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(150, 177, 211), 0, height / 2, new Color(40, 91,
							149));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 59,
					127), 0, height, new Color(34, 85, 146));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_SKYBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(206, 231, 255), 0, height / 2, new Color(206,
							231, 255, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 96,
					194), 0, height, new Color(0, 96, 194));

			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_LIGHTBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(167, 227, 248), 0, height / 2, new Color(167,
							227, 248, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(17,
					160, 208), 0, height, new Color(17, 106, 208));

			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_VOILET_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(206, 206, 255), 0, height / 2, new Color(170,
							170, 255, 100));
			glossyBtnColor = new GradientPaint(0, 0, new Color(108, 108, 255),
					0, height, new Color(108, 108, 255));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_DARKPURPLE_THEME:
			/*
			 * glossyTopBtnColor = new GradientPaint(0, 0, new
			 * Color(202,149,255), 0, height / 2, new Color(160, 66, 255,150));
			 */
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(202, 149, 255), 0, height / 2, new Color(135, 15,
							255, 100));

			glossyBtnColor = new GradientPaint(0, height / 2, new Color(44, 0,
					89), 0, height, new Color(44, 0, 89));

			button.setForeground(Color.WHITE);
			break;

		case Theme.GLOSSY_PURPLE_THEME:
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(186, 0,
					255), 0, height, new Color(186, 0, 255));
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(238, 200, 224), 0, height / 2, new Color(222,
							152, 198, 150));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_LAVENDER_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(208, 190, 233), 0, height / 2, new Color(147,
							105, 203, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(107,
					60, 173), 0, height, new Color(107, 60, 173));

			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_DARKPINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 191, 239), 0, height / 2, new Color(255,
							191, 239, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(115, 0,
					85), 0, height, new Color(115, 0, 85));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_PINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(251, 215, 226), 0, height / 2, new Color(251,
							215, 226, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(220,
					22, 86), 0, height, new Color(220, 22, 86));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_PALEPINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 236, 251), 0, height / 2, new Color(255,
							236, 251, 100));
			glossyBtnColor = new GradientPaint(0, height, new Color(255, 128,
					223), 0, height, new Color(255, 128, 223));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_SILVER_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(250, 251, 253), 0, height / 2, new Color(238,
							243, 248));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(209,
					223, 237), 0, height, new Color(191, 210, 228));
			button.setForeground(Color.BLACK);
			break;
		case Theme.GLOSSY_BLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(170, 170, 170), 0, height / 2, new Color(150,
							130, 130, 130));
			glossyBtnColor = new GradientPaint(0, 0, new Color(0, 0, 0), 0,
					height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_GRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(197, 197, 197), 0, height / 2, new Color(128,
							128, 128, 150));
			glossyBtnColor = new GradientPaint(0, height,
					new Color(91, 91, 91), 0, height, new Color(91, 91, 91));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_LIGHTGRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(215, 215, 215), 0, height / 2, new Color(215,
							215, 215, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(159,
					159, 159), 0, height, new Color(159, 159, 159));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_METALIC_GRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(200, 205, 209), 0, height / 2, new Color(120,
							137, 145, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(73, 92,
					105), 0, height, new Color(73, 92, 105));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_BLUEGRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(200, 205, 209), 0, height / 2, new Color(120,
							137, 145, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(32, 53,
					72), 0, height, new Color(32, 53, 72));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_METALIC_BLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height / 2, new Color(85, 134,
							194));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(1, 31,
					99), 0, height, new Color(137, 255, 255));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_ORANGERED_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 255, 255), 0, height / 2, new Color(85, 134,
							194));
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 197, 63), 0, height / 2, new Color(255, 197,
							63, 100));

			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_ORANGEBLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 197, 63), 0, height / 2, new Color(255, 0,
							0, 100));
			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_BLUEBLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(251, 139, 62), 0, height / 2, new Color(255, 102,
							0, 100));
			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_GREENBLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(192, 234, 68), 0, height / 2, new Color(168, 216,
							24, 100));
			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_GOLDBLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(255, 213, 0),
					0, height / 2, new Color(255, 213, 0, 100));

			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(0, 0, 0));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(71, 232, 252), 0, height / 2, new Color(71, 232,
							252, 50));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(3, 37,
					188), 0, height, new Color(3, 37, 188));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_MULTIRED_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 193, 193), 0, height / 2, new Color(255,
							102, 102));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(255, 0,
					0), 0, height, new Color(255, 233, 232));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_MULTIDARKRED_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 191, 191), 0, height / 2, new Color(255,
							174, 174, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(181, 0,
					0), 0, height, new Color(255, 191, 191));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_MULTIGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(184, 226, 156), 0, height / 2, new Color(109,
							176, 71));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(61,
					135, 20), 0, height, new Color(103, 223, 38));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIDARKGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(0, 242, 0),
					0, height / 2, new Color(0, 155, 0));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 64,
					0), 0, height, new Color(0, 242, 0));
			button.setForeground(Color.WHITE);

			break;

		case Theme.GLOSSY_MULTIBLUEGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(0, 240, 156),
					0, height / 2, new Color(0, 183, 119));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(20,
					113, 74), 0, height, new Color(0, 221, 143));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTILIGHTGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(219, 255, 202), 0, height / 2, new Color(219,
							255, 187, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(97,
					204, 0), 0, height, new Color(201, 255, 151));
			button.setForeground(Color.BLACK);

			break;
		case Theme.GLOSSY_MULTILIME_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(224, 244, 162), 0, height / 2, new Color(181,
							231, 31));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(130,
					167, 18), 0, height, new Color(191, 234, 62));

			break;
		case Theme.GLOSSY_MULTIOLIVEGREEN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0, new Color(138, 234, 9),
					0, height / 2, new Color(128, 216, 7, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(68,
					116, 4), 0, height, new Color(148, 247, 15));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIORANGE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(250, 194, 160), 0, height / 2, new Color(255,
							153, 85));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(255,
					102, 0), 0, height, new Color(255, 218, 193));
			break;
		case Theme.GLOSSY_MULTIGOLD_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 248, 223), 0, height / 2, new Color(255,
							226, 91));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(223,
					184, 0), 0, height, new Color(255, 239, 164));
			break;
		case Theme.GLOSSY_MULTINAVYBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(188, 200, 252), 0, height / 2, new Color(188,
							200, 252, 130));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(5, 25,
					114), 0, height, new Color(188, 200, 252));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIINDIGO_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(150, 177, 211), 0, height / 2, new Color(40, 91,
							149));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 59,
					127), 0, height, new Color(150, 177, 211));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTISKYBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(206, 231, 255), 0, height / 2, new Color(206,
							231, 255, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 96,
					194), 0, height, new Color(206, 231, 255));
			break;
		case Theme.GLOSSY_MULTILIGHTBLUE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(167, 227, 248), 0, height / 2, new Color(167,
							227, 248, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(17,
					160, 208), 0, height, new Color(255, 255, 255));
			break;
		case Theme.GLOSSY_MULTIDARKPURPLE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(202, 149, 255), 0, height / 2, new Color(135, 15,
							255, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(44, 0,
					89), 0, height, new Color(202, 149, 255));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_MULTIPURPLE_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(238, 200, 224), 0, height / 2, new Color(222,
							152, 198, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(186, 0,
					255), 0, height, new Color(238, 200, 224));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTILAVENDER_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(208, 190, 233), 0, height / 2, new Color(147,
							105, 203, 200));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(107,
					60, 173), 0, height, new Color(208, 190, 233));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIVOILET_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(206, 206, 255), 0, height / 2, new Color(170,
							170, 255, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(108,
					108, 255), 0, height, new Color(206, 206, 255));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIDARKPINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 191, 239), 0, height / 2, new Color(255,
							191, 239, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(115, 0,
					85), 0, height, new Color(255, 191, 239));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIPINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(251, 215, 226), 0, height / 2, new Color(251,
							215, 226, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(220,
					22, 86), 0, height, new Color(251, 215, 226));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIPALEPINK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(255, 236, 251), 0, height / 2, new Color(255,
							236, 251, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(255,
					128, 223), 0, height, new Color(255, 236, 251));
			break;
		case Theme.GLOSSY_MULTIBROWN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(254, 173, 139), 0, height / 2, new Color(253,
							115, 55, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(118,
					35, 1), 0, 2 * height / 2, new Color(254, 173, 139));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTILIGHTBROWN_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(240, 215, 166), 0, height / 2, new Color(226,
							179, 88));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(212,
					151, 37), 0, height, new Color(240, 215, 166));
			break;
		case Theme.GLOSSY_MULTIBLUEGRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(200, 205, 209), 0, height / 2, new Color(120,
							137, 145, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(32, 53,
					72), 0, height, new Color(200, 205, 209));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTIGRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(197, 197, 197), 0, height / 2, new Color(128,
							128, 128, 150));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(91, 91,
					91), 0, height, new Color(197, 197, 197));
			button.setForeground(Color.WHITE);

			break;
		case Theme.GLOSSY_MULTILIGHTGRAY_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(215, 215, 215), 0, height / 2, new Color(215,
							215, 215, 100));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(159,
					159, 159), 0, height, new Color(215, 215, 215));
			break;
		case Theme.GLOSSY_MULTIBLACK_THEME:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(130, 130, 130), 0, height / 2, new Color(100,
							100, 100, 100));
			glossyBtnColor = new GradientPaint(0, height / 2,
					new Color(0, 0, 0), 0, height, new Color(170, 170, 170));
			button.setForeground(Color.WHITE);
			break;
		case Theme.GLOSSY_MULTIBLUECOLOR_THEME:
			// Blue
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(34, 144, 255), 0, height / 2, new Color(0, 101,
							202));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(0, 82,
					164), 0, height, new Color(206, 231, 255));

			// textColor = Color.WHITE;
			break;
		default:
			glossyTopBtnColor = new GradientPaint(0, 0,
					new Color(149, 159, 207), 0, height / 2, new Color(85, 134,
							194));
			glossyBtnColor = new GradientPaint(0, height / 2, new Color(1, 31,
					99), 0, height, new Color(17, 213, 255));
			button.setForeground(Color.WHITE);
			break;
		}
		;
		glossyColors[0] = glossyTopBtnColor;
		glossyColors[1] = glossyBtnColor;
		return glossyColors;
	}
}