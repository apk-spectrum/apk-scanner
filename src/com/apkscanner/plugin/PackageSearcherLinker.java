package com.apkscanner.plugin;

import java.awt.Desktop;
import java.awt.Event;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import com.apkscanner.plugin.manifest.Component;

public class PackageSearcherLinker extends AbstractPackageSearcher
{

	public PackageSearcherLinker(String packageName, Component component) {
		super(packageName, component);
	}

	@Override
	public int getSupportType() {
		int type = 0;
		for(String s: component.target.split("\\|")) {
			if(s.toLowerCase().equals("package")) {
				type |= IPackageSearcher.SEARCHER_TYPE_PACKAGE_NAME;
			} else if(s.toLowerCase().equals("label")) {
				type |= IPackageSearcher.SEARCHER_TYPE_APP_NAME;
			}
		}
		return type;
	}

	@Override
	public String getPreferLangForAppName() {
		return component.preferLang;
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
		String url = component.url.replaceAll("%[tT][aA][rR][gG][eE][tT]%", filter);
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
