package com.apkscanner.plugin.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.NetworkException;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class NetworkErrorDialog
{
	public static final int RESULT_NONE = 0;
	public static final int RESULT_RETRY = 1;
	public static final int RESULT_CHANGED = 2;
	public static final int RESULT_IGNORED = 3;

	private static final String IGNORE_NO_SUCHE_INTERFACE = PlugInConfig.CONFIG_IGNORE_NETWORK_ERR_NO_SUCHE_INTERFACE;
	private static final String IGNORE_TIME_OUT = PlugInConfig.CONFIG_IGNORE_NETWORK_ERR_CONNECTION_TIMEOUT;
	private static final String IGNORE_SSH_HANDSHAKE = PlugInConfig.CONFIG_IGNORE_NETWORK_ERR_SSL_HANDSHAKE;

	public static int show(Component parent, IUpdateChecker plugin) {
		return show(parent, plugin, false);
	}

	public static int show(Component parent, IUpdateChecker plugin, boolean force) {
		NetworkException e = plugin.getLastNetworkException();
		if(e == null) {
			Log.v("No NetworkException : " + plugin.getName());
			return RESULT_NONE;
		}

		PlugInConfig config = plugin.getPlugInConfig();

		NetworkErrorPanel errPanel = new NetworkErrorPanel();
		errPanel.setVisibleNeverLook(!force);
		int ret = -1;

		String pluginName = plugin.getName();
		if(pluginName == null) pluginName = "IUpdateChecker@0x" + Integer.toHexString(plugin.hashCode());

		if(e.isNetworkNotFoundException()) {
			if(!force && "true".equals(config.getConfiguration(IGNORE_NO_SUCHE_INTERFACE)))
				return RESULT_IGNORED;

			errPanel.setText(Resource.STR_MSG_NO_SUCH_NETWORK.getString());
			ret = showOptionDialog(parent, errPanel, Resource.STR_TITLE_NO_SUCH_NETWORK.getString());

			if(!force) config.setConfiguration(IGNORE_NO_SUCHE_INTERFACE, errPanel.isNeverLook() ? "true" : "false");
			PlugInManager.saveProperty();
		} else if(e.isProxyException()) {
			if(!force && "true".equals(config.getConfiguration(IGNORE_TIME_OUT)))
				return RESULT_IGNORED;

			errPanel.setText(String.format(Resource.STR_MSG_FAILURE_PROXY_ERROR.getString(), pluginName));
			errPanel.add(new NetworkProxySettingPanel(plugin.getPlugInPackage()));
			ret = showOptionDialog(parent, errPanel, Resource.STR_TITLE_NETWORK_TIMEOUT.getString());

			if(!force) config.setConfiguration(IGNORE_TIME_OUT, errPanel.isNeverLook() ? "true" : "false");
			PlugInManager.saveProperty();
		} else if(e.isSslCertException()) {
			Log.e(e.getCause().toString());
			if(!force && "true".equals(config.getConfiguration(IGNORE_SSH_HANDSHAKE)))
				return RESULT_IGNORED;

			errPanel.setText(String.format(Resource.STR_MSG_FAILURE_SSL_ERROR.getString(), pluginName));
			errPanel.add(new NetworkTruststoreSettingPanel(plugin.getPlugInPackage()));
			ret = showOptionDialog(parent, errPanel, Resource.STR_TITLE_SSL_EXCEPTION.getString());

			if(!force) config.setConfiguration(IGNORE_SSH_HANDSHAKE, errPanel.isNeverLook() ? "true" : "false");
			PlugInManager.saveProperty();
			//boolean ignoreSSL = "true".equals(updater.getPlugInPackage().getConfiguration(PlugInConfig.CONFIG_IGNORE_SSL_CERT, "false", true));
		} else {
			Log.d("unkown exception : " + e.getMessage());
		}

		return ret == 0 ? RESULT_RETRY : RESULT_NONE;
	}

	private static int showOptionDialog(Component parentComponent, Object context, String title) {
		return MessageBoxPane.showOptionDialog(parentComponent, context, title, JOptionPane.DEFAULT_OPTION,
				MessageBoxPane.ERROR_MESSAGE, null,
				new String[] { Resource.STR_BTN_RETRY.getString(),  Resource.STR_BTN_CLOSE.getString() },
				Resource.STR_BTN_CLOSE.getString());
	}
}
