package com.apkscanner.plugin.sample;

import java.awt.Desktop;
import java.awt.Event;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import com.apkscanner.plugin.AbstractPackageSearcher;
import com.apkscanner.plugin.manifest.Component;

public class SimpleSearcher extends AbstractPackageSearcher {
	public SimpleSearcher(String packageName, Component component) {
		super(packageName, component);
	}

	@Override
	public int getSupportType() {
		return SEARCHER_TYPE_PACKAGE_NAME | SEARCHER_TYPE_APP_NAME;
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
}
