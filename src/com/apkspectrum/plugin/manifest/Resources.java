package com.apkspectrum.plugin.manifest;

public class Resources
{
	public final String src;
	public final String lang;
	public final StringData[] strings;
	public Resources(String src, String lang, StringData[] strings) {
		this.src = src;
		this.lang = lang;
		this.strings = strings;
	}
}
