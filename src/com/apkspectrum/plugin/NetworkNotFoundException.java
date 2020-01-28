package com.apkspectrum.plugin;

import java.net.SocketException;

public class NetworkNotFoundException extends SocketException {
	private static final long serialVersionUID = 5112222247575266101L;

	public NetworkNotFoundException(String messgae) {
		super(messgae);
	}
}
