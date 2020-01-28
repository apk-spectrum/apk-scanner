package com.apkspectrum.plugin.manifest;

public class InvalidManifestException extends Exception {
	private static final long serialVersionUID = -1418009045489968369L;

	public InvalidManifestException() {
		super();
	}

	public InvalidManifestException(String message) {
		super(message);
	}

	public InvalidManifestException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidManifestException(Throwable cause) {
		super(cause);
	}
}
