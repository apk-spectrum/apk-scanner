package com.apkspectrum.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

public class NetworkException extends IOException {
	private static final long serialVersionUID = 8466756047136472142L;

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable e) {
		super(e);
	}

	public boolean isNetworkNotFoundException() { // No such network interface
		Throwable t = this.getCause();
		return (t instanceof NetworkNotFoundException);
	}

	public boolean isProxyException() { // maybe proxy issue
		Throwable t = this.getCause();
		return (t instanceof SocketTimeoutException || t instanceof ConnectException);
	}

	public boolean isSslCertException() { // maybe ssl cert issue
		Throwable t = this.getCause();
		while(t instanceof IOException && t.getCause() != null) {
			t = t.getCause();
		}
		boolean result = t instanceof SSLHandshakeException;
		result = result || t instanceof FileNotFoundException;
		result = result || t instanceof UnrecoverableKeyException;
		result = result || t instanceof NoSuchAlgorithmException;
		result = result || t instanceof KeyManagementException;
		result = result || t instanceof KeyStoreException;
		result = result || t instanceof CertificateException;
		result = result || t instanceof CertificateException;
		if(t instanceof IOException) {
			result = result || "Invalid keystore format".equals(t.getMessage());
		}
		return result;
	}
}

