package com.apkscanner.plugin.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.NetworkException;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
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
		NetworkException e = plugin.getLastNetworkException();
		if(e == null) {
			Log.v("No NetworkException : " + plugin.getName());
			return RESULT_NONE;
		}

		PlugInConfig config = plugin.getPlugInConfig();

		NetworkErrorPanel errPanel = new NetworkErrorPanel();
		int ret = -1;

		StringBuilder errMsg = new StringBuilder("Failed PlugIn: " + plugin.getName() + "\n\n");


		if(e.isNetworkNotFoundException()) {
			if("true".equals(config.getConfiguration(IGNORE_NO_SUCHE_INTERFACE)))
				return RESULT_IGNORED;

			errPanel.setText("Not found network interface\n\nTry checking the network connection\n");
			ret = showOptionDialog(parent, errPanel, "Network Error - Not found network interface");

			config.setConfiguration(IGNORE_NO_SUCHE_INTERFACE, errPanel.isNaverLook() ? "true" : "false");
			PlugInManager.saveProperty();
		} else if(e.isProxyException()) {
			if("true".equals(config.getConfiguration(IGNORE_TIME_OUT)))
				return RESULT_IGNORED;

			errMsg.append("Network connection timed out!\n\n");
			errMsg.append("Try checking the connection or proxy, firewall!");

			errPanel.setText(errMsg.toString());
			errPanel.add(new NetworkProxySettingPanel(plugin));
			ret = showOptionDialog(parent, errPanel, "Network Error - Connection timed out");

			config.setConfiguration(IGNORE_TIME_OUT, errPanel.isNaverLook() ? "true" : "false");
			PlugInManager.saveProperty();
		} else if(e.isSslCertException()) {
			Log.e(e.getCause().toString());
			if("true".equals(config.getConfiguration(IGNORE_SSH_HANDSHAKE)))
				return RESULT_IGNORED;

			errMsg.append("Occurred SSLHandshakeException.\n\n");
			errMsg.append("Try add a SSL certification to the trust store!");

			errPanel.setText(errMsg.toString());
			errPanel.add(new NetworkTruststoreSettingPanel(plugin));
			ret = showOptionDialog(parent, errPanel, "Network Error - SSL Certificate Error");

			config.setConfiguration(IGNORE_SSH_HANDSHAKE, errPanel.isNaverLook() ? "true" : "false");
			PlugInManager.saveProperty();
			//boolean ignoreSSL = "true".equals(updater.getPlugInPackage().getConfiguration(PlugInConfig.CONFIG_IGNORE_SSL_CERT, "false", true));
		} else {
			Log.d("unkown exception : " + e.getMessage());
		}

		return ret == 0 ? RESULT_RETRY : RESULT_NONE;
	}

	private static int showOptionDialog(Component parentComponent, Object context, String title) {
		return MessageBoxPane.showOptionDialog(parentComponent, context, title, JOptionPane.DEFAULT_OPTION,
				MessageBoxPane.ERROR_MESSAGE, null, new String[] { "Retry",  "Close" }, "Close");
	}
}
