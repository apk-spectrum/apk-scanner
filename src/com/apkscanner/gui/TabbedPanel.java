package com.apkscanner.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
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
import com.apkscanner.plugin.AbstractTabbedRequest;
import com.apkscanner.plugin.IExtraComponent;
import com.apkscanner.plugin.ITabbedComponent;
import com.apkscanner.plugin.ITabbedRequest;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.LanguageChangeListener;
import com.apkscanner.resource.RStr;

public class TabbedPanel extends JTabbedPane implements LanguageChangeListener, ActionListener
{
	private static final long serialVersionUID = -5500517956616692675L;

	private HashMap<Component, ITabbedComponent> componentMap = new HashMap<>();

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
		for(ITabbedComponent tabbed: componentMap.values()) {
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

		componentMap.put(tabbedComp.getComponent(), tabbedComp);

		if(priority == -1) {
			priority = getTabCount();
			tabbedComp.setPriority(priority);
		}

		ITabbedRequest request = new AbstractTabbedRequest(tabbedComp) {
			@Override
			public boolean onRequestVisible(boolean visible) {
				ITabbedComponent tabbed = getTabbedComponent();
				Component c = tabbed.getComponent();
				int idx = indexOfComponent(c);
				boolean oldVisible = idx > -1;
				if(oldVisible == visible) return false;
				if(!visible) {
					removeTabAt(idx);
				} else {
					int priority = tabbed.getPriority();
					for(idx = 0; idx < getTabCount(); idx++) {
						ITabbedComponent tabbedComp = componentMap.get(getComponentAt(idx));
						if(priority <= tabbedComp.getPriority()) {
							break;
						}
					}
					insertTab(tabbed.getTitle(), tabbed.getIcon(), c, tabbed.getToolTip(), idx);
					setEnabledAt(idx, tabbed.isTabbedEnabled());
				}
				reindexing();
				return true;
			}

			@Override
			public boolean onRequestEnabled(boolean enabled) {
				ITabbedComponent tabbed = getTabbedComponent();
				int idx = indexOfComponent(tabbed.getComponent());
				if(idx == -1) return false;
				setEnabledAt(idx, enabled);
				setTitleAt(idx, tabbed.getTitle());
				setToolTipTextAt(idx, tabbed.getToolTip());
				return true;
			}

			@Override
			public boolean onRequestChangeTitle() {
				ITabbedComponent tabbed = getTabbedComponent();
				int idx = indexOfComponent(tabbed.getComponent());
				if(idx == -1) return false;
				setTitleAt(idx, tabbed.getTitle());
				setToolTipTextAt(idx, tabbed.getToolTip());
				return true;
			}

			@Override
			public boolean onRequestSelected() {
				ITabbedComponent tabbed = getTabbedComponent();
				int idx = indexOfComponent(tabbed.getComponent());
				if(idx == -1) return false;
				setSelectedIndex(idx);
				return false;
			}
		};
		tabbedComp.setTabbedRequest(request);
		request.onRequestVisible(tabbedComp.isTabbedVisible());
	}

	public void reindexing() {
		for(int i = 0; i < getTabCount(); i++) {
			String tooltip = getToolTipTextAt(i);
			if(tooltip != null && !tooltip.isEmpty()) {
				tooltip = tooltip.replaceAll("\\s\\(Alt\\+\\d\\)$", "");
				if(i < 10) {
					tooltip += " (Alt+" + (i<9 ? i+1 : 0) + ")";
					setMnemonicAt(i, (i<9 ? KeyEvent.VK_1 + i : KeyEvent.VK_0));
				}
			}
			setToolTipTextAt(i, tooltip);
		}
	}

	public void reloadResource()
	{
		for(ITabbedComponent tabbed: componentMap.values()) {
			tabbed.reloadResource();
		}
	}

	public void setData(ApkInfo apkInfo, Status status)
	{
		for(ITabbedComponent tabbed: componentMap.values()) {
			tabbed.setData(apkInfo, status);
		}
	}

	public void onProgress(String message)
	{
		for(ITabbedComponent tabbed: componentMap.values()) {
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(message);
			}
		}
	}

	public void setLodingLabel()
	{
		for(ITabbedComponent tabbed: componentMap.values()) {
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