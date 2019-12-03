package com.apkscanner.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.component.DefaultTabbedRequest;
import com.apkscanner.gui.component.ITabbedComponent;
import com.apkscanner.gui.component.ITabbedRequest;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.gui.component.tabbedpane.TabbedPaneUIManager;
import com.apkscanner.gui.tabpanels.AbstractTabbedPanel;
import com.apkscanner.gui.tabpanels.BasicInfo;
import com.apkscanner.gui.tabpanels.Components;
import com.apkscanner.gui.tabpanels.IProgressListener;
import com.apkscanner.gui.tabpanels.Libraries;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.gui.tabpanels.Signatures;
import com.apkscanner.gui.tabpanels.Widgets;
import com.apkscanner.plugin.IExtraComponent;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.LanguageChangeListener;
import com.apkscanner.resource.RStr;

public class TabbedPanel extends JTabbedPane implements LanguageChangeListener, ActionListener
{
	private static final long serialVersionUID = -5500517956616692675L;

	private List<ITabbedComponent> components = new ArrayList<>();

	public TabbedPanel() {
		this(null);
	}

	public TabbedPanel(String themeClazz) {
		setOpaque(true);
		TabbedPaneUIManager.setUI(this, themeClazz);

		addTab(new BasicInfo());
		addTab(new Widgets());
		addTab(new Libraries());
		addTab(new Resources());
		addTab(new Components());
		addTab(new Signatures());

		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		KeyStrokeAction.registerKeyStrokeActions(this, JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, false)
			}, this);

		RStr.addLanguageChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int keycode = Integer.parseInt(evt.getActionCommand());
		int idx = getSelectedIndex();
		int count = getTabCount();
		int preIdx = idx;
		switch(keycode) {
		case KeyEvent.VK_RIGHT:
			do {
				idx = ++idx % count;
			} while(!isEnabledAt(idx) && idx != preIdx);
			if(idx != preIdx) {
				setSelectedIndex(idx);
			}
			break;
		case KeyEvent.VK_LEFT:
			do {
				idx = (--idx + count) % count;
			} while(!isEnabledAt(idx) && idx != preIdx);
			if(idx != preIdx) {
				setSelectedIndex(idx);
			}
			break;
		}
	}

	public void uiLoadBooster() {
		for(ITabbedComponent tabbed: components) {
			Component c = tabbed.getComponent();
			if(c instanceof AbstractTabbedPanel) {
				((AbstractTabbedPanel)c).initialize();
			}
		}
	}

	public void onLoadPlugin() {
		for(final IExtraComponent plugin: PlugInManager.getExtraComponenet()) {
			plugin.initialize();
			addTab(plugin);
		}
	}

	public void addTab(ITabbedComponent tabbed) {
		addTab(tabbed, tabbed.getPriority());
	}

	public void addTab(ITabbedComponent tabbedComp, int priority) {
		if(tabbedComp == null) return;

		components.add(tabbedComp);

		if(priority == -1) {
			priority = getTabCount();
			tabbedComp.setPriority(priority);
		}

		ITabbedRequest request = new DefaultTabbedRequest(this, tabbedComp);
		request.onRequestVisible(tabbedComp.isTabbedVisible());
	}

	public void reloadResource()
	{
		for(ITabbedComponent tabbed: components) {
			tabbed.reloadResource();
		}
	}

	public void setData(ApkInfo apkInfo, Status status)
	{
		for(ITabbedComponent tabbed: components) {
			tabbed.setData(apkInfo, status);
		}
	}

	public void onProgress(String message)
	{
		for(ITabbedComponent tabbed: components) {
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(message);
			}
		}
	}

	public void setLodingLabel()
	{
		for(ITabbedComponent tabbed: components) {
			int idx = indexOfComponent(tabbed.getComponent());
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(null);
				if(idx != -1) setSelectedIndex(idx);
			} else {
				tabbed.clearData();
				if(idx != -1) {
					setEnabledAt(idx, false);
					setTitleAt(idx, tabbed.getTitle());
				}
			}
		}
	}

	@Override
	public void languageChange(String oldLang, String newLang) {
		reloadResource();
	}
}