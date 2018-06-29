package com.apkscanner.plugin;

import java.awt.Desktop;
import java.awt.Event;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class PackageSearcherLinker extends AbstractPackageSearcher
{
	private int searchType;
	private String searchUrl;
	private String preferLanguage;

	public PackageSearcherLinker(String packageName, String pluginName, int searchType, String searchUrl, String preferLanguage) {
		super(packageName, pluginName);
		this.searchType = searchType;
		this.searchUrl = searchUrl;
		this.preferLanguage = preferLanguage;
	}

	@Override
	public int getSupportType() {
		return searchType;
	}

	@Override
	public String getPreferLangForAppName() {
		return preferLanguage;
	}

	@Override
	public boolean trySearch(int type, String name) {
		return (getSupportType() & type) == type && name != null && !name.trim().isEmpty();
	}

	@Override
	public void launch(Event event, int type, String name) {
		if(!trySearch(type, name)) return;

		String filter = null;
		try {
			filter = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			filter = name;
		}
		String url = searchUrl.replaceAll("%[tT][aA][rR][gG][eE][tT]%", filter);
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
