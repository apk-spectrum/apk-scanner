package com.apkspectrum.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JTabbedPane;

public class DefaultTabbedRequest extends AbstractTabbedRequest
{
	private static HashMap<Component, ITabbedComponent<?>> componentMap = new HashMap<>();

	private JTabbedPane tabbedPane;

	public DefaultTabbedRequest(JTabbedPane tabbedPane, ITabbedComponent<?> tabbedComp) {
		super(tabbedComp);
		this.tabbedPane = tabbedPane;
		tabbedComp.setTabbedRequest(this);
		componentMap.put(tabbedComp.getComponent(), tabbedComp);
	}

	@Override
	public boolean onRequestVisible(boolean visible) {
		ITabbedComponent<?> tabbed = getTabbedComponent();
		Component c = tabbed.getComponent();
		int idx = tabbedPane.indexOfComponent(c);
		boolean oldVisible = idx > -1;
		if(oldVisible == visible) return false;
		if(!visible) {
			tabbedPane.removeTabAt(idx);
		} else {
			int priority = tabbed.getPriority();
			for(idx = 0; idx < tabbedPane.getTabCount(); idx++) {
				ITabbedComponent<?> tabbedComp = componentMap.get(tabbedPane.getComponentAt(idx));
				if(priority <= tabbedComp.getPriority()) {
					break;
				}
			}
			tabbedPane.insertTab(tabbed.getTitle(), tabbed.getIcon(), c, tabbed.getToolTip(), idx);
			tabbedPane.setEnabledAt(idx, tabbed.isTabbedEnabled());
		}
		reindexing();
		return true;
	}

	@Override
	public boolean onRequestEnabled(boolean enabled) {
		ITabbedComponent<?> tabbed = getTabbedComponent();
		int idx = tabbedPane.indexOfComponent(tabbed.getComponent());
		if(idx == -1) return false;
		tabbedPane.setEnabledAt(idx, enabled);
		return true;
	}

	@Override
	public boolean onRequestChangeTitle() {
		ITabbedComponent<?> tabbed = getTabbedComponent();
		int idx = tabbedPane.indexOfComponent(tabbed.getComponent());
		if(idx == -1) return false;
		tabbedPane.setTitleAt(idx, tabbed.getTitle());
		tabbedPane.setToolTipTextAt(idx, tabbed.getToolTip());
		return true;
	}

	@Override
	public boolean onRequestSelected() {
		ITabbedComponent<?> tabbed = getTabbedComponent();
		int idx = tabbedPane.indexOfComponent(tabbed.getComponent());
		if(idx == -1) return false;
		tabbedPane.setSelectedIndex(idx);
		return false;
	}

	private void reindexing() {
		for(int i = 0; i < tabbedPane.getTabCount(); i++) {
			String tooltip = tabbedPane.getToolTipTextAt(i);
			if(tooltip != null && !tooltip.isEmpty()) {
				tooltip = tooltip.replaceAll("\\s\\(Alt\\+\\d\\)$", "");
				if(i < 10) {
					tooltip += " (Alt+" + (i<9 ? i+1 : 0) + ")";
					tabbedPane.setMnemonicAt(i, (i<9 ? KeyEvent.VK_1 + i : KeyEvent.VK_0));
				}
			}
			tabbedPane.setToolTipTextAt(i, tooltip);
		}
	}
}
