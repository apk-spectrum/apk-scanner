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
	}

	private AaptLightScanner scanner = new AaptLightScanner();
	private StatusListener listener;

	public EasyLightApkScanner(String path1) {
		scanner.setStatusListener(new ApkLightScannerListener());
		
		if(path1 != null) {
			scanner.openApk(path1);	
		}		
	}

	public EasyLightApkScanner() {
		scanner.setStatusListener(new ApkLightScannerListener());
	}
	
	public ApkInfo getApkInfo() {
		return scanner.getApkInfo();
	}
	
	public ApkScanner getApkScanner() {
		return scanner;
	}
	
	public void setApk(String path) {
		scanner.openApk(path);		
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
			this.error = error;
			if(listener != null) listener.onError(error);
		}

		@Override
		public void onCompleted() {
			Log.d("onCompleted() : " + error);
			if(listener != null) listener.onCompleted();
		}

		@Override
		public void onProgress(int step, String msg) {
			Log.d("onProgress()" + step +":" +  msg);
		}

		@Override
		public void onStateChanged(Status status) {
			Log.d("onProgress()" + status );
		}
    }

	public void clear(boolean b) {
		// TODO Auto-generated method stub
		scanner.clear(b);
	}
}
