package com.apkscanner.gui.easymode;

import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.util.Log;

public class EasyLightApkScanner {

	public interface StatusListener {
		public void onStart();

		public void onSuccess();

		public void onError(int error);

		public void onCompleted();

		public void onStateChanged(Status status);

		public void onProgress(int step, String msg);
	}

	private ApkScanner scanner;
	private StatusListener listener;
	private int latestError = 0;
	private String apkPath = null;
	private Boolean isreadygui = false;	
	private Object lock = new Object();

	public EasyLightApkScanner(String path1) {
		// scanner.setStatusListener(new ApkLightScannerListener());

		if (path1 != null) {
			apkPath = path1;
			scanner.openApk(path1);
		}
	}

	public EasyLightApkScanner(ApkScanner aaptlightscanner) {
		this.scanner = aaptlightscanner;
		this.scanner.setStatusListener(new ApkLightScannerListener());				
	}

	public EasyLightApkScanner() {
		scanner = new AaptLightScanner(null);
		scanner.setStatusListener(new ApkLightScannerListener());
	}

	public ApkInfo getApkInfo() {
		return scanner.getApkInfo();
	}

	public String getApkFilePath() {
		return apkPath;
	}

	public ApkScanner getApkScanner() {
		return scanner;
	}

	public void setApk(final String path) {
		Log.d("setApk :" + path);
		apkPath = path;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					scanner.clear(true);
					EasyMainUI.corestarttime = System.currentTimeMillis();
					scanner.openApk(path);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();

	}

	public void setStatusListener(StatusListener listener) {
		this.listener = listener;
	}

	public void setReadyListener() {
		synchronized (lock) {
			this.isreadygui = true;
			Log.d("setReadyListener notify");
			lock.notify();
			if(((AaptLightScanner)scanner).notcallcomplete) {
				Log.d("call complete");
				listener.onCompleted();
			}
		}
	}
	public int getApkScannerstatus() {
		return scanner.getStatus();
	}

	class ApkLightScannerListener implements ApkScanner.StatusListener {
		public ApkLightScannerListener() {
		}

		@Override
		public void onStart(long estimatedTime) {
			if (listener != null)
				listener.onStart();
		}

		@Override
		public void onSuccess() {
			Log.d("onSuccess()");
			if (listener != null)
				listener.onSuccess();
		}

		@Override
		public void onError(int error) {
			Log.d("onError()" + error);
			latestError = error;
			if (listener != null)
				listener.onError(error);
		}

		@Override
		public void onCompleted() {
			Log.d("completed");
			synchronized (lock) {
				try {
					while (!isreadygui) {
						Log.d("wait for gui end");
						lock.wait();
						Log.d("notify lock");
					}
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}

			if (listener != null && getApkInfo() != null) {
				apkPath = getApkInfo().filePath;
				listener.onCompleted();
			}
		}

		@Override
		public void onProgress(int step, String msg) {
			Log.d("onProgress()" + step + ":" + msg);
			if (listener != null)
				listener.onProgress(step,msg);
		}

		@Override
		public void onStateChanged(Status status) {
			Log.d("onStateChanged()" + status);
			if (listener != null)
				listener.onStateChanged(status);
		}
	}

	public void clear(boolean b) {
		scanner.clear(b);
	}

	public int getlatestError() {
		return latestError;
	}
}
