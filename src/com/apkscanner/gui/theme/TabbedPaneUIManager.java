package com.apkscanner.gui.theme;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.apkscanner.util.ClassFinder;

public class TabbedPaneUIManager {
	private static final String THEMES_PACKAGE = "com.apkscanner.gui.theme.tabbedpane"; 

	public static class TabbedPaneUIInfo {
		Class<?> clazz;

		TabbedPaneUIInfo(Class<?> clazz) {
			this.clazz = clazz;
		}

		public String getName() {
			UIThemeName annotation = clazz.getAnnotation(UIThemeName.class);
			String name = null;
			if(annotation != null) {
				name = annotation.value();
			}
			if(name == null || name.trim().isEmpty()) {
				name = clazz.getSimpleName();
			}
			return name;
		}

		public String getClassName() {
			return clazz.getName();
		}
	}

	public static TabbedPaneUIInfo[] getUIThemes() {
		ArrayList<TabbedPaneUIInfo> infoList = new ArrayList<TabbedPaneUIInfo>();
		try {
			for(Class<?> cls : ClassFinder.getClasses(THEMES_PACKAGE)) {
				if(cls.isMemberClass() || cls.isInterface()) continue;
				try {
					if(cls.newInstance() instanceof BasicTabbedPaneUI) {
						infoList.add(new TabbedPaneUIInfo(cls));
					}
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return infoList.toArray(new TabbedPaneUIInfo[0]);
	}

	public static void setUI(JTabbedPane pane, String clazz) {
		if(pane == null || clazz == null || clazz.trim().isEmpty()) {
			return;
		}
		try {
			pane.setUI((TabbedPaneUI) Class.forName(clazz).newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
