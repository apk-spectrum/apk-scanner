package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyConsolCmd
{
	public interface OutputObserver
	{
		public void ConsolOutput(String output);
	}
	
	private MyConsolCmd() { }
	
	static public String[] exc(String[] cmd, boolean showLog, OutputObserver observer)
	{
		String s = "";
		List<String> buffer = new ArrayList<String>(); 

		try {
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
		    BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream()/*, encoding*/));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) System.out.println(s);
		    	if(observer != null) observer.ConsolOutput(s);
		    	buffer.add(s);
		    }
		} catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }

		return buffer.toArray(new String[0]);
	}
}
