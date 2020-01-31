package com.apkspectrum.plugin.manifest;

public class Linker {
	public final String url;
	public final String target;
	public final String preferLang;
	
	Linker(String url, String target, String preferLang) {
		this.url = url;
		this.target = target;
		this.preferLang = preferLang;
	}
}
