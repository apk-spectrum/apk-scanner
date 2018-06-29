package com.apkscanner.plugin;

import com.apkscanner.util.ConsolCmd;

public class ExternalToolLinker extends AbstractExternalTool
{
	private String path;
	private String param;

	public ExternalToolLinker(String packageName, String pluginName, String path, String param) {
		super(packageName, pluginName);
		this.path = path;
		this.param = param;
	}

	@Override
	public void launch(String apkPath) {
		String tmp = param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%", apkPath);
		ConsolCmd.exc(new String[] {path, tmp}, true, null);		
	}
}
