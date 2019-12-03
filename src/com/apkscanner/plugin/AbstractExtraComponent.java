package com.apkscanner.plugin;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.apkscanner.gui.component.ITabbedRequest;
import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.util.Log;

public abstract class AbstractExtraComponent extends AbstractPlugIn implements IExtraComponent
{
	public static final int SEND_REQUEST_NONE = ITabbedRequest.REQUEST_NONE;
	public static final int SEND_REQUEST_VISIBLE = ITabbedRequest.REQUEST_VISIBLE;
	public static final int SEND_REQUEST_INVISIBLE = ITabbedRequest.REQUEST_INVISIBLE;
	public static final int SEND_REQUEST_ENABLED = ITabbedRequest.REQUEST_ENABLED;
	public static final int SEND_REQUEST_DISABLED = ITabbedRequest.REQUEST_DISABLED;
	public static final int SEND_REQUEST_SELECTED = ITabbedRequest.REQUEST_SELECTED;
	public static final int SEND_REQUEST_CHANGE_TITLE = ITabbedRequest.REQUEST_CHANGE_TITLE;

	public static final int SEND_REQUEST_CURRENT_ENABLED = 100;
	public static final int SEND_REQUEST_CURRENT_VISIBLE = 101;

	protected java.awt.Component tabbedComponent = null;

	private ITabbedRequest tabbedRequest;

	private int priority = -1;

	private boolean visible = true;
	private boolean enabled = true;

	public AbstractExtraComponent(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public java.awt.Component getComponent() {
		return tabbedComponent;
	}

	@Override
	public void initialize() {
		if(tabbedComponent != null) return;
		tabbedComponent = new javax.swing.JPanel();
	}

	@Override
	public String getTitle() {
		return getLabel();
	}

	@Override
	public String getToolTip() {
		String tooltip = getDescription();
		if(tooltip == null) tooltip = getLabel();
		return tooltip;
	}

	@Override
	public Icon getIcon() {
		URL url = getIconURL();
		if(url == null) return null;
		return new ImageIcon(url);
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public boolean isTabbedVisible() {
		return visible;
	}

	@Override
	public boolean isTabbedEnabled() {
		return enabled;
	}

	@Override
	public void setTabbedEnabled(boolean enabled) {
		this.enabled = enabled;
		if(tabbedRequest != null)
			tabbedRequest.onRequestEnabled(enabled);
	}

	@Override
	public void setTabbedVisible(boolean visible) {
		this.visible = visible;
		if(tabbedRequest != null)
			tabbedRequest.onRequestVisible(visible);
	}

	@Override
	public void setTabbedRequest(ITabbedRequest request) {
		tabbedRequest = request;
	}

	protected void sendRequest(int request) {
		switch(request) {
		case SEND_REQUEST_VISIBLE:
			setTabbedVisible(true);
			return;
		case SEND_REQUEST_INVISIBLE:
			setTabbedVisible(false);
			return;
		case SEND_REQUEST_ENABLED:
			setTabbedEnabled(true);
			return;
		case SEND_REQUEST_DISABLED:
			setTabbedEnabled(false);
			return;
		case SEND_REQUEST_CURRENT_ENABLED:
			request = isTabbedEnabled() ? ITabbedRequest.REQUEST_ENABLED : ITabbedRequest.REQUEST_DISABLED;
			break;
		case SEND_REQUEST_CURRENT_VISIBLE:
			request = isTabbedVisible() ? ITabbedRequest.REQUEST_VISIBLE : ITabbedRequest.REQUEST_INVISIBLE;
			break;
		case SEND_REQUEST_SELECTED:
		case SEND_REQUEST_CHANGE_TITLE:
			break;
		default:
			Log.w("unknown request : " + request);
		}
		if(tabbedRequest != null) tabbedRequest.onRequest(request);
	}
}
