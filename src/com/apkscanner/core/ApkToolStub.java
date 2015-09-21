package com.apkscanner.core;

import com.apkscanner.data.ApkInfo;

public class ApkToolStub {
	
	private ApkInfo mApkInfo = null;

	public enum Status {
		UNINITIALIZE,
		INITIALIZING,
		INITIALIZEED,
		SOLVE_RESOURCE,
		SOLVE_CODE,
		SOLVE_BOTH,
		STANDBY,
		DELETEING
	}
	
	public enum ProcessCmd {
		SOLVE_RESOURCE,
		SOLVE_CODE,
		SOLVE_BOTH,
		DELETE_TEMP_PATH,
	}
	
	public enum SolveType {
		RESOURCE,
		CODE,
		BOTH
	}
	
	public interface StatusListener
	{
		public void OnStart();
		public void OnSuccess();
		public void OnError();
		public void OnComplete();
		public void OnProgress(int step, String msg);
		public void OnStateChange();
	}
	
	public abstract interface ManagerInterface
	{
		public void solve(SolveType type, StatusListener listener);
		public void clear(boolean sync, StatusListener listener);		
		public void reloadResource();
		public ApkInfo getApkInfo();
	}
	
	
	public ApkInfo getApkInfo() {
		return mApkInfo;
	}
	
}
