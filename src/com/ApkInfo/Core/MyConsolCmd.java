package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyConsolCmd
{
	private boolean multiCmdExit;

	public interface OutputObserver
	{
		public boolean ConsolOutput(String output);
	}
	
	private MyConsolCmd()
	{
		multiCmdExit = false;
	}

	static public String[] exc(String[] cmd) {
		return (new MyConsolCmd()).exc_i(cmd, false, null);
	}

	static public String[] exc(String[] cmd, boolean showLog) {
		return (new MyConsolCmd()).exc_i(cmd, showLog, null);
	}

	static public String[] exc(String[] cmd, boolean showLog, OutputObserver observer) {
		return (new MyConsolCmd()).exc_i(cmd, showLog, observer);
	}
	
	private String[] exc_i(String[] cmd, boolean showLog, OutputObserver observer)
	{
		String s = "";
		List<String> buffer = new ArrayList<String>(); 
		
		//if(showLog) {
			//buffer.add(echoCmd(cmd));
		//}
    	if(observer != null) {
    		observer.ConsolOutput(echoCmd(cmd));
    	}

		try {
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
		    BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream()/*, encoding*/));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) System.out.println(s);
		    	if(observer != null) {
		    		multiCmdExit = !observer.ConsolOutput(s) || multiCmdExit;
		    	}
		    	buffer.add(s);
		    }
		} catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }

		return buffer.toArray(new String[0]);
	}

	static public String[][] exc(String[][] cmd)
	{
		return (new MyConsolCmd()).exc_i(cmd, false, null);
	}

	static public String[][] exc(String[][] cmd, boolean showLog)
	{
		return (new MyConsolCmd()).exc_i(cmd, showLog, null);
	}

	static public String[][] exc(String[][] cmd, boolean showLog, final OutputObserver observer)
	{
		return (new MyConsolCmd()).exc_i(cmd, showLog, observer);
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
