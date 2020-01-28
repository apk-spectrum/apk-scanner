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

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.tabpanels.AbstractTabbedPanel;
import com.apkscanner.gui.tabpanels.BasicInfo;
import com.apkscanner.gui.tabpanels.Components;
import com.apkscanner.gui.tabpanels.IProgressListener;
import com.apkscanner.gui.tabpanels.Libraries;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.gui.tabpanels.Signatures;
import com.apkscanner.gui.tabpanels.Widgets;
import com.apkspectrum.plugin.IExtraComponent;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.swing.DefaultTabbedRequest;
import com.apkspectrum.swing.ITabbedComponent;
import com.apkspectrum.swing.ITabbedRequest;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;

public class TabbedPanel extends JTabbedPane implements ActionListener
{
	private static final long serialVersionUID = -5500517956616692675L;

	private List<ITabbedComponent<ApkInfo>> components = new ArrayList<>();

	public TabbedPanel(String themeClazz, ActionListener listener) {
		setOpaque(true);
		TabbedPaneUIManager.setUI(this, themeClazz);

		addTab(new BasicInfo());
		addTab(new Widgets());
		addTab(new Libraries());
		addTab(new Resources(listener));
		addTab(new Components());
		addTab(new Signatures());

		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		KeyStrokeAction.registerKeyStrokeActions(this, JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, false)
			}, this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int idx = getSelectedIndex();
		int count = getTabCount();
		int preIdx = idx;
		switch(evt.getActionCommand()) {
		case "alt pressed RIGHT":
			do {
				idx = ++idx % count;
			} while(!isEnabledAt(idx) && idx != preIdx);
			if(idx != preIdx) {
				setSelectedIndex(idx);
			}
			break;
		case "alt pressed LEFT":
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
		for(ITabbedComponent<?> tabbed: components) {
			Component c = tabbed.getComponent();
			if(c instanceof AbstractTabbedPanel) {
				((AbstractTabbedPanel)c).initialize();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onLoadPlugin(ApkInfo apkInfo, int status) {
		for(final IExtraComponent plugin: PlugInManager.getExtraComponenet()) {
			plugin.initialize();
			addTab(plugin);
			for(int state = 1; (state & ApkScanner.STATUS_ALL_COMPLETED) != 0; state <<= 1) {
				if((state != 0 && (status & state) == state)) {
					plugin.setData(apkInfo, state);
				}
			}
			if(status == ApkScanner.STATUS_ALL_COMPLETED) {
				plugin.setData(apkInfo, ApkScanner.STATUS_ALL_COMPLETED);
			}
		}
	}

	public void addTab(ITabbedComponent<ApkInfo> tabbed) {
		addTab(tabbed, tabbed.getPriority());
	}

	public void addTab(ITabbedComponent<ApkInfo> tabbedComp, int priority) {
		if(tabbedComp == null) return;

		components.add(tabbedComp);

		if(priority == -1) {
			priority = getTabCount();
			tabbedComp.setPriority(priority);
		}

		ITabbedRequest request = new DefaultTabbedRequest(this, tabbedComp);
		request.onRequestVisible(tabbedComp.isTabbedVisible());
	}

	public void setData(ApkInfo apkInfo, int status)
	{
		for(ITabbedComponent<ApkInfo> tabbed: components) {
			tabbed.setData(apkInfo, status);
		}
	}

	public void onProgress(String message)
	{
		for(ITabbedComponent<ApkInfo> tabbed: components) {
			if(tabbed instanceof IProgressListener) {
				((IProgressListener)tabbed).onProgress(message);
			}
		}
	}

	public void setLodingLabel()
	{
		for(ITabbedComponent<ApkInfo> tabbed: components) {
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
}