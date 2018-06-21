package com.apkscanner.plugin;

import java.awt.Desktop;
import java.awt.Event;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

public class SimpleSearcher implements IStoreSearcher {

	@Override
	public int getSupportType() {
		return SEARCHER_TYPE_PACKAGE_NAME | SEARCHER_TYPE_APP_NAME;
	}

	@Override
	public URL getIconURL() {
		return null;
	}

	@Override
	public String getPreferLangForAppName() {
		return null;
	}

	@Override
	public boolean trySearch(int type, String name) {
		return (getSupportType() & type) == type && Integer.bitCount(type) == 1;
	}

	@Override
	public void launch(Event event, int type, String name) {
		if(!trySearch(type, name)) return;
		String url = "https://play.google.com/store";

		String filter = null;
		try {
			filter = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(type == SEARCHER_TYPE_PACKAGE_NAME) {
			url += "/apps/details?id=" + filter;
		} else if(type == SEARCHER_TYPE_APP_NAME) {
			url += "/search?q=" + filter + "&c=apps";
		}

		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(url));
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }
	    }
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int getType() {
		return IPlugIn.PLUGIN_TPYE_STORE_SEARCHER;
	}
}
