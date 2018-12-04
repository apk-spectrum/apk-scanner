package com.apkscanner.gui.easymode;

import com.apkscanner.core.scanner.AaptLightScanner;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.util.Log;

public class EasyLightApkScanner {
    
	public interface StatusListener {
		public void onStart();
		public void onSuccess();
		public void onError(int error);
		public void onCompleted();
		public void onStateChanged(Status status);
	}

	private AaptLightScanner scanner;
	private StatusListener listener;
	private int latestError = 0;
	private String apkPath = null;
	public EasyLightApkScanner(String path1) {
		//scanner.setStatusListener(new ApkLightScannerListener());
		
		if(path1 != null) {
			apkPath = path1;
			scanner.openApk(path1);	
		}
	}
	public EasyLightApkScanner(AaptLightScanner aaptlightscanner) {		
		this.scanner = (AaptLightScanner)aaptlightscanner;		
		this.scanner.setStatusListener(new ApkLightScannerListener());
	}

	public EasyLightApkScanner() {
		scanner = new AaptLightScanner();
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
					EasyGuiMain.corestarttime = System.currentTimeMillis();
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

    class ApkLightScannerListener implements ApkScanner.StatusListener {
    	private int error;
    	
    	public ApkLightScannerListener() {
    		this.error = 0;
		}
    	
		@Override
		public void onStart(long estimatedTime) {
			error = 0;
			if(listener != null) listener.onStart();
		}

		@Override
		public void onSuccess() {
			Log.d("onSuccess()");
			error = 0;
			if(listener != null) listener.onSuccess();
		}

		@Override
		public void onError(int error) {
			Log.d("onError()" + error);
			latestError = this.error = error;
			if(listener != null) listener.onError(error);
		}

		@Override
		public void onCompleted() {
			Log.d("onCompleted() : " + error);
			if(listener != null) {
				listener.onCompleted();
				apkPath = getApkInfo().filePath;
			}
		}

		@Override
		public void onProgress(int step, String msg) {
			Log.d("onProgress()" + step +":" +  msg);
		}

		@Override
		public void onStateChanged(Status status) {
			Log.d("onProgress()" + status );
			if(listener != null) listener.onStateChanged(status);
		}
    }

	public void clear(boolean b) {
		// TODO Auto-generated method stub
		scanner.clear(b);
	}
	
	public int getlatestError() {
		return latestError;
	}
}
