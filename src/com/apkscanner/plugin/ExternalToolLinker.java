package com.apkscanner.plugin;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.plugin.manifest.Component;
import com.apkscanner.util.ConsolCmd;

public class ExternalToolLinker extends AbstractExternalTool
{
	public ExternalToolLinker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	@Override
	public void launch() {
		ApkInfo info = PlugInManager.getApkInfo();
		if(info == null) return;
		String tmp = component.param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%", info.filePath);
		ConsolCmd.exc(new String[] {component.path, tmp}, true, null);		
	}
}
