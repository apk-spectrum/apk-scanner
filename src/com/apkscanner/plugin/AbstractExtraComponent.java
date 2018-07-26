package com.apkscanner.plugin;

import java.util.ArrayList;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractExtraComponent extends AbstractPlugIn implements IExtraComponent
{
	protected ArrayList<IRequestListener> listeners = new ArrayList<>();
	protected boolean visible = false;
	protected java.awt.Component component = null;

	public AbstractExtraComponent(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public java.awt.Component getComponent() {
		return component;
	}

	@Override
	public java.awt.Component initailizeComponent() {
		if(component != null) return component; 
		component = new javax.swing.JPanel();
		return component; 
	}

	@Override
	public boolean isVisibleRequested() {
		return visible;
	}

	@Override
	public void addStateChangedListener(IRequestListener listener) {
		if(listener == null) return;
		synchronized(listeners) {
			if(!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	@Override
	public void removeStateChangedListener(IRequestListener listener) {
		if(listener == null) return;
		synchronized(listeners) {
			if(listeners.contains(listener)) {
				listeners.remove(listener);
			}
		}
	}

	protected void sendRequest(boolean visible) {
		this.visible = visible;
		synchronized(listeners) {
			for(IRequestListener l: listeners) {
				l.onRequestVisible(visible);
			}
		}
	}
}
