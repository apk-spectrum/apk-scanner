package com.apkscanner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConsolCmd
{
	private boolean multiCmdExit;

	public interface OutputObserver
	{
		public boolean ConsolOutput(String output);
	}
	
	private ConsolCmd()
	{
		multiCmdExit = false;
	}

	static public String[] exc(String[] cmd) {
		return (new ConsolCmd()).exc_i(cmd, false, null);
	}

	static public String[] exc(String[] cmd, boolean showLog) {
		return (new ConsolCmd()).exc_i(cmd, showLog, null);
	}

	static public String[] exc(String[] cmd, boolean showLog, OutputObserver observer) {
		return (new ConsolCmd()).exc_i(cmd, showLog, observer);
	}
	
	private String[] exc_i(String[] cmd, boolean showLog, OutputObserver observer)
	{
		String s = "";
		List<String> buffer = new ArrayList<String>(); 
		
		if(showLog) {
			Log.i(echoCmd(cmd));
		}
    	if(observer != null) {
    		observer.ConsolOutput(echoCmd(cmd));
    	}

		try {
			Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			InputStream inputStream = process.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream/*, encoding*/);
		    BufferedReader stdOut = new BufferedReader(inputStreamReader);
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) Log.i(s);
		    	if(observer != null) {
		    		multiCmdExit = !observer.ConsolOutput(s) || multiCmdExit;
		    	}
		    	buffer.add(s);
		    }
		    stdOut.close();
		    inputStreamReader.close();
		    inputStream.close();
		    process.destroy();
		} catch (IOException e) { // 에러 처리
		      Log.e("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }
		
		String[] ret = buffer.toArray(new String[0]);
		return ret;
	}

	static public String[][] exc(String[][] cmd)
	{
		return (new ConsolCmd()).exc_i(cmd, false, null);
	}

	static public String[][] exc(String[][] cmd, boolean showLog)
	{
		return (new ConsolCmd()).exc_i(cmd, showLog, null);
	}

	static public String[][] exc(String[][] cmd, boolean showLog, final OutputObserver observer)
	{
		return (new ConsolCmd()).exc_i(cmd, showLog, observer);
	}
	
	private String[][] exc_i(String[][] cmd, boolean showLog, final OutputObserver observer) {
		List<String[]> buffer = new ArrayList<String[]>();

		for(int i = 0; i < cmd.length; i++) {
			buffer.add(exc_i(cmd[i], showLog, observer));
			if(multiCmdExit) break;
		}

		return buffer.toArray(new String[0][0]);
	}
	
	private String echoCmd(String[] cmd) {
		String echo = "";
		for(int i = 0; i < cmd.length; i++){
			echo += (i==0?"":" ") + cmd[i];
		}
		return echo;
	}
}
