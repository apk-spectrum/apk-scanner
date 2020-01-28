package com.apkspectrum.swing.tabbedpaneui;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.apkspectrum.util.ClassFinder;

public class TabbedPaneUIManager
{
	public static final String THEMES_PACKAGE = PlasticTabbedPaneUI.class.getPackage().getName();

	public static final String DEFAULT_TABBED_UI = PlasticTabbedPaneUI.class.getName();

	public static final String TABBED_UI_NONE = "None";

	public static class TabbedPaneUIInfo {
		Class<?> clazz;

		TabbedPaneUIInfo(Class<?> clazz) {
			this.clazz = clazz;
		}

		public String getName() {
			String name = null;
			if(BasicTabbedPaneUI.class.equals(clazz)) {
				name = TABBED_UI_NONE;
			} else {
				UIThemeName annotation = clazz.getAnnotation(UIThemeName.class);
				if(annotation != null) {
					name = annotation.value();
				}
				if(name == null || name.trim().isEmpty()) {
					name = clazz.getSimpleName();
				}
			}
			return name;
		}

		public String getClassName() {
			return clazz.getName();
		}
	}

	public static TabbedPaneUIInfo[] getUIThemes() {
		ArrayList<TabbedPaneUIInfo> infoList = new ArrayList<TabbedPaneUIInfo>();
		infoList.add(new TabbedPaneUIInfo(BasicTabbedPaneUI.class));
		try {
			for(Class<?> cls : ClassFinder.getClasses(THEMES_PACKAGE)) {
				if(cls.isMemberClass() || cls.isInterface()
					|| !cls.getSuperclass().equals(BasicTabbedPaneUI.class)) continue;
				infoList.add(new TabbedPaneUIInfo(cls));
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return infoList.toArray(new TabbedPaneUIInfo[0]);
	}

	public static String[] getUIClassNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (TabbedPaneUIInfo info : TabbedPaneUIManager.getUIThemes()) {
			list.add(info.getClassName());
		}
		return list.toArray(new String[list.size()]);
	}

	public static void setUI(JTabbedPane pane, String clazz) {
		if(pane == null || clazz == null || clazz.trim().isEmpty()) {
			return;
		}
		clazz = clazz.replace("com.apkscanner.gui.component.tabbedpane", THEMES_PACKAGE);
		try {
			pane.setUI((TabbedPaneUI) Class.forName(clazz).getConstructor().newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static TabbedPaneUIInfo getUIInfo(JTabbedPane pane) {
		return new TabbedPaneUIInfo(pane.getUI().getClass());
	}
}
