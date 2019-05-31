package com.apkscanner.gui;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JTabbedPane;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.tabpanels.AbstractTabbedPanel;
import com.apkscanner.gui.tabpanels.BasicInfo;
import com.apkscanner.gui.tabpanels.Components;
import com.apkscanner.gui.tabpanels.IProgressListener;
import com.apkscanner.gui.tabpanels.Libraries;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.gui.tabpanels.Signatures;
import com.apkscanner.gui.tabpanels.Widgets;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.plugin.IExtraComponent;
import com.apkscanner.plugin.ITabbedComponent;
import com.apkscanner.plugin.ITabbedRequest;
import com.apkscanner.plugin.PlugInManager;

public class TabbedPanel extends JTabbedPane
{
	private static final long serialVersionUID = -5500517956616692675L;

	private ArrayList<ITabbedRequest> tabbedRequestHandler = new ArrayList<>();
	private HashMap<ITabbedComponent, ITabbedRequest> invisibleTabbeds = new HashMap<>();
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
	}

	public void uiLoadBooster() {
		for(int i=0; i<getTabCount(); i++) {
			Component c = getTabComponentAt(i);
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

	public void addTab(final ITabbedComponent tabbed) {
		if(tabbed == null) return;
		if(!tabbed.getComponent().isVisible()) {
			addInvisibleTab(tabbed);
			return;
		}
		componentMap.put(tabbed.getComponent(), tabbed);

		int idx = getTabCount();
		addTab(tabbed.getTitle(), tabbed.getIcon(), tabbed.getComponent(), tabbed.getToolTip());

		setEnabledAt(idx, tabbed.getComponent().isEnabled());
		reindexing();

		for(int i = tabbedRequestHandler.size(); i < getTabCount(); i++) {
			final int tabIndex = i;
			tabbedRequestHandler.add(new ITabbedRequest() {
				@Override
				public boolean onRequestVisible(boolean visible) {
					if(!visible) {
						addInvisibleTab(componentMap.get(getComponentAt(tabIndex)));
						removeTabAt(tabIndex);
						reindexing();
					}
					return true; 	
				}

				@Override
				public boolean onRequestEnabled(boolean enable) {
					ITabbedComponent tabbed = componentMap.get(getComponentAt(tabIndex));
					setEnabledAt(tabIndex, enable);
					setTitleAt(tabIndex, tabbed.getTitle());
					setToolTipTextAt(tabIndex, tabbed.getToolTip());
					return true;
				}
			});
		}
	}

	public void addInvisibleTab(final ITabbedComponent tabbed) {
		if(tabbed == null) return;
		if(tabbed.getComponent().isVisible()) {
			addTab(tabbed);
			return;
		}
		invisibleTabbeds.put(tabbed, new ITabbedRequest() {
			@Override
			public boolean onRequestVisible(boolean visible) {
				if(visible) {
					addTab(tabbed);
					invisibleTabbeds.remove(tabbed);
				}
				return true; 	
			}
		});
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
		for(int i = 0; i < getTabCount(); i++) {
			ITabbedComponent tabbed = componentMap.get(getComponentAt(i));
			tabbed.reloadResource();
			setTitleAt(i, tabbed.getTitle());
			setToolTipTextAt(i, tabbed.getToolTip());
		}
		for(ITabbedComponent tabbed: invisibleTabbeds.keySet()) {
			tabbed.reloadResource();
		}
	}

	public void setData(ApkInfo apkInfo, Status status)
	{
		for(int i = 0; i < getTabCount(); i++) {
			ITabbedComponent tabbed = componentMap.get(getComponentAt(i));
			tabbed.setData(apkInfo, status, tabbedRequestHandler.get(i));
		}
		for(Entry<ITabbedComponent, ITabbedRequest> entry: invisibleTabbeds.entrySet()) {
			entry.getKey().setData(apkInfo, status, entry.getValue());
		}
	}

	public void onProgress(String message)
	{
		for(int i = 0; i < getTabCount(); i++) {
			ITabbedComponent tabbed = componentMap.get(getComponentAt(i));
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(message);		
			}
		}
	}

	public void setLodingLabel()
	{
		for(int i = 0; i < getTabCount(); i++) {
			ITabbedComponent tabbed = componentMap.get(getComponentAt(i));
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(null);
				setSelectedIndex(i);
			} else {
				tabbed.clearData();
				setEnabledAt(i, false);
				setTitleAt(i, tabbed.getTitle());
			}
		}
	}
}