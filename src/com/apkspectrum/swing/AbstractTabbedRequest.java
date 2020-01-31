package com.apkspectrum.swing;

public abstract class AbstractTabbedRequest implements ITabbedRequest
{
	protected ITabbedComponent<?> tabbedComp;

	public AbstractTabbedRequest(ITabbedComponent<?> component) {
		tabbedComp = component;
	}

	protected ITabbedComponent<?> getTabbedComponent() {
		return tabbedComp;
	}

	public boolean onRequestVisible(boolean visible) {
		return false;
	}

	public boolean onRequestEnabled(boolean enable) {
		return false;
	}

	public boolean onRequestSelected() {
		return false;
	}

	public boolean onRequestChangeTitle() {
		return false;
	}

	public boolean onRequest(int request) {
		switch(request) {
		case REQUEST_VISIBLE:
			return onRequestVisible(true);
		case REQUEST_INVISIBLE:
			return onRequestVisible(false);
		case REQUEST_ENABLED:
			return onRequestEnabled(true);
		case REQUEST_DISABLED:
			return onRequestEnabled(false);
		case REQUEST_SELECTED:
			return onRequestSelected();
		case REQUEST_CHANGE_TITLE:
			return onRequestChangeTitle();
		}
		return false;
	}
}