package com.apkscanner.gui.tabpanels;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.apkscanner.gui.component.ITabbedComponent;
import com.apkscanner.gui.component.ITabbedRequest;
import com.apkscanner.util.Log;

public abstract class AbstractTabbedPanel extends JPanel implements ITabbedComponent {
	private static final long serialVersionUID = -5636023017306297012L;

	public static final int SEND_REQUEST_NONE = ITabbedRequest.REQUEST_NONE;
	public static final int SEND_REQUEST_VISIBLE = ITabbedRequest.REQUEST_VISIBLE;
	public static final int SEND_REQUEST_INVISIBLE = ITabbedRequest.REQUEST_INVISIBLE;
	public static final int SEND_REQUEST_ENABLED = ITabbedRequest.REQUEST_ENABLED;
	public static final int SEND_REQUEST_DISABLED = ITabbedRequest.REQUEST_DISABLED;
	public static final int SEND_REQUEST_SELECTED = ITabbedRequest.REQUEST_SELECTED;
	public static final int SEND_REQUEST_CHANGE_TITLE = ITabbedRequest.REQUEST_CHANGE_TITLE;

	public static final int SEND_REQUEST_CURRENT_ENABLED = 100;
	public static final int SEND_REQUEST_CURRENT_VISIBLE = 101;

	private Icon icon;
	private int dataSize = -1;
	private int priority = -1;

	private boolean visible = true;
	private boolean enabled = true;

	private ITabbedRequest tabbedRequest;

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getTitle() {
		return super.getName() + (dataSize >= 0 ? "(" + dataSize + ")" : "");
	}

	@Override
	public String getToolTip() {
		return super.getToolTipText();
	}

	public void setTitle(String title, String toolTip) {
		setName(title);
		setToolTipText(toolTip);
		if(tabbedRequest != null)
			tabbedRequest.onRequestChangeTitle();
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	protected void setDataSize(int size) {
		dataSize = size;
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

	public void setSeletected() {
		if(tabbedRequest != null)
			tabbedRequest.onRequestSelected();
	}

	protected void setDataSize(int size, boolean chageEnabled, boolean changeVisible) {
		if(dataSize == size) return;
		dataSize = size;
		sendRequest(SEND_REQUEST_CHANGE_TITLE);
		if(chageEnabled) setTabbedEnabled(dataSize > 0);
		if(changeVisible) setTabbedVisible(dataSize > 0);
	}

	@Override
	public void clearData() {
		setDataSize(-1, true, false);
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
			request = isTabbedEnabled() ? SEND_REQUEST_ENABLED : SEND_REQUEST_DISABLED;
			break;
		case SEND_REQUEST_CURRENT_VISIBLE:
			request = isTabbedVisible() ? SEND_REQUEST_VISIBLE : SEND_REQUEST_INVISIBLE;
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
