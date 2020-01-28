package com.apkspectrum.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConsolCmd
{
	private boolean multiCmdExit;

	public interface ConsoleOutputObserver
	{
		public boolean ConsolOutput(String output);
	}

	private ConsolCmd()
	{
		multiCmdExit = false;
	}

	static public String[] exec(String[] cmd) {
		return (new ConsolCmd()).exec_i(cmd, false, null);
	}

	static public String[] exec(String[] cmd, boolean showLog) {
		return (new ConsolCmd()).exec_i(cmd, showLog, null);
	}

	static public String[] exec(String[] cmd, boolean showLog, ConsoleOutputObserver observer) {
		return (new ConsolCmd()).exec_i(cmd, showLog, observer);
	}

	private String[] exec_i(String[] cmd, boolean showLog, ConsoleOutputObserver observer)
	{
		String s = "";
		List<String> buffer = new ArrayList<String>();

		if(showLog) {
			Log.i(echoCmd(cmd));
		}
    	if(observer != null) {
    		observer.ConsolOutput(echoCmd(cmd));
    	}

    	Process process = null;
		try {
			process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			try(InputStream inputStream = process.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream/*, encoding*/);
				BufferedReader stdOut = new BufferedReader(inputStreamReader)) {
			    while ((s = stdOut.readLine()) != null) {
			    	if(showLog) Log.i(s);
			    	if(observer != null) {
			    		multiCmdExit = !observer.ConsolOutput(s) || multiCmdExit;
			    	}
			    	buffer.add(s);
			    }
			}
		} catch (IOException e) { // 에러 처리
		      Log.e("error: " + e.getMessage());
		      return null;
	    } finally {
	    	if(process != null) {
	    		process.destroy();
	    	}
	    }

		String[] ret = buffer.toArray(new String[buffer.size()]);
		return ret;
	}

	static public String[][] exec(String[][] cmd)
	{
		return (new ConsolCmd()).exec_i(cmd, false, null);
	}

	static public String[][] exec(String[][] cmd, boolean showLog)
	{
		return (new ConsolCmd()).exec_i(cmd, showLog, null);
	}

	static public String[][] exec(String[][] cmd, boolean showLog, final ConsoleOutputObserver observer)
	{
		return (new ConsolCmd()).exec_i(cmd, showLog, observer);
	}

	private String[][] exec_i(String[][] cmd, boolean showLog, final ConsoleOutputObserver observer) {
		List<String[]> buffer = new ArrayList<String[]>();

		for(int i = 0; i < cmd.length; i++) {
			buffer.add(exec_i(cmd[i], showLog, observer));
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
