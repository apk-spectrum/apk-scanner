package com.apkscanner.plugin;

import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.util.ConsolCmd;

public class ExternalToolLinker extends AbstractExternalTool
{
	public ExternalToolLinker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public void launch(String apkPath) {
		String tmp = component.param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%", apkPath);
		ConsolCmd.exc(new String[] {component.path, tmp}, true, null);		
	}
}
