package com.apkscanner.plugin;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

public class NetworkException extends IOException {
	private static final long serialVersionUID = 8466756047136472142L;

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable e) {
		super(e);
	}

	public boolean isProxyException() { // maybe proxy issue
		Throwable t = this.getCause();
		return (t instanceof SocketTimeoutException || t instanceof ConnectException);
	}

	public boolean isSslCertException() { // maybe ssl cert issue
		Throwable t = this.getCause();
		return (t instanceof SSLHandshakeException);
	}
}
