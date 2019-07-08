package com.apkscanner.gui.tabpanels;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.apkscanner.plugin.ITabbedComponent;
import com.apkscanner.plugin.ITabbedRequest;
import com.apkscanner.util.Log;

public abstract class AbstractTabbedPanel extends JPanel implements ITabbedComponent {
	private static final long serialVersionUID = -5636023017306297012L;
	
	public static final int SEND_REQUEST_CURRENT_ENABLED = 100;
	public static final int SEND_REQUEST_CURRENT_VISIBLE = 101;

	private Icon icon;
	private int dataSize = -1;

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

	@Override
	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	protected void setDataSize(int size) {
		dataSize = size;
	}

	protected void setDataSize(int size, boolean chageEnabled, boolean changeVisible) {
		dataSize = size;
		if(chageEnabled) setEnabled(dataSize > 0);
		if(changeVisible) setVisible(dataSize > 0);
	}

	@Override
	public void clearData() {
		setDataSize(-1, true, false);
	}

	protected void sendRequest(ITabbedRequest handler, int request) {
		switch(request) {
		case ITabbedRequest.REQUEST_VISIBLE:	setVisible(true); break;
		case ITabbedRequest.REQUEST_INVISIBLE:	setVisible(false); break;
		case ITabbedRequest.REQUEST_ENABLED:	setEnabled(true); break;
		case ITabbedRequest.REQUEST_DISABLED:	setEnabled(false); break;
		case ITabbedRequest.REQUEST_SELECTED:	break;
		case SEND_REQUEST_CURRENT_ENABLED:
			request = isEnabled() ? ITabbedRequest.REQUEST_ENABLED : ITabbedRequest.REQUEST_DISABLED;
			break;
		case SEND_REQUEST_CURRENT_VISIBLE:
			request = isVisible() ? ITabbedRequest.REQUEST_VISIBLE : ITabbedRequest.REQUEST_INVISIBLE;
			break;
		default:
			Log.w("unknown request : " + request);
			return;
		}
		if(handler != null) handler.onRequest(request);
	}
}
