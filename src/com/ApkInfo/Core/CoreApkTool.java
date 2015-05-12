package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ApkInfo.UI.MyProgressBarDemo;

public class CoreApkTool {
	static MyProgressBarDemo progressBarDemo;
	static ArrayList<String> tempResource = new ArrayList<String>();
	static ArrayList<String> tempLib = new ArrayList<String>();
	
	public static String DefaultPath;
	public static Boolean makeFolder(String FilePath) {
		File newDirectory = new File(FilePath);
		if(!newDirectory.exists()) {
			newDirectory.mkdir();
			return true;
		}
		return false;
	}
	
	public static 	ArrayList<String> findfileforResource(File f) {
		File[] list = f.listFiles();
		if(list==null) {
			System.err.println("list null");
			return null;
		}
		for (int i=0; i<list.length; i++) {
			if (list[i].isDirectory()) {
				
	   			if(list[i].getAbsolutePath().indexOf("drawable") > 0) {	   				
	   				findfileforResource(list[i]);
	   			}
			} else {
				if(list[i].getName().endsWith(".png")) {					
					tempResource.add(list[i].getAbsolutePath());
					try {

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}		    	  
			}
		}
		return tempResource;
	}
	
	public static 	ArrayList<String> findfileforLib(File f) {
		File[] list = f.listFiles();
		if(list==null) {
			System.err.println("list null");
			return null;
		}
		for (int i=0; i<list.length; i++) {
			if (list[i].isDirectory()) {				
				findfileforLib(list[i]);   			
			} else {
				if(list[i].getName().endsWith(".so")) {					
					tempLib.add(list[i].getAbsolutePath());
					try {

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}		    	  
			}
		}
		return tempLib;
	}
	
	public static void solveAPK(String APKFilePath, String solvePath) {
		String apkToolPath = CoreApkTool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		DefaultPath = solvePath;
		
		apkToolPath = (new File(apkToolPath)).getParentFile().getPath();
		apkToolPath += File.separator + "apktool.jar";
		try {
			apkToolPath = URLDecoder.decode(apkToolPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("apkToolPath : " + apkToolPath);

		if(!(new File(apkToolPath)).exists()) {
			System.out.println("apktool.jar 파일이 존재 하지 않습니다 :");
			return;
		}

		String[] cmd = {"java","-jar",apkToolPath,"d","-s","-f","-o",solvePath,"-p",solvePath, APKFilePath};
		
		exc(cmd,true);
		
	}
	
	
	static String exc(String[] cmd, boolean showLog) {
		try {
			String s = "";
			String e = "";
			
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			
			String buffer = "";
		    // 외부 프로그램 출력 읽기
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    //BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));
		    
		    
		    if(showLog) {
			    while ((s = stdOut.readLine()) != null) {
			    	if(s.matches("^I:.*"))
			    		progressBarDemo.addProgress(10,s + "\n");
			    	System.out.println(s);
			    	buffer += s;
			    }
		    }
		    // 외부 프로그램 반환값 출력 (이 부분은 필수가 아님)
		    //System.out.println("Exit Code: " + oProcess.exitValue());
		    //System.exit(oProcess.exitValue()); // 외부 프로그램의 반환값을, 이 자바 프로그램 자체의 반환값으로 삼기
		    
		    return buffer;

		    } catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());
		      
		      System.exit(-1);
	    }
		return null;
		
	}

	public static void setProgressBarDlg(MyProgressBarDemo progressBarDlg) {
		// TODO Auto-generated method stub
		progressBarDemo = progressBarDlg;
	}
	
    public static boolean deleteDirectory(File path) {
        if(!path.exists()) {
            return false;
        }         
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }         
        return path.delete();
    }


	public static String getFileLength(long length) {
		double LengthbyUnit = (double) length;
		int Unit = 0;
		while (LengthbyUnit > 1024 && Unit < 5) { // 단위 숫자로 나누고 한번 나눌 때마다 Unit
			LengthbyUnit = LengthbyUnit / 1024;
			Unit++;
		}

		DecimalFormat df = new DecimalFormat("#,##0.00");

		 StringBuilder result = new StringBuilder(df.format(LengthbyUnit).length());

		switch (Unit) {
		case 0:
			result.append(df.format(LengthbyUnit)+" Bytes");
			break;
		case 1:
			result.append(df.format(LengthbyUnit)+" KB");
			break;
		case 2:
			result.append(df.format(LengthbyUnit)+" MB");
			break;
		case 3:
			result.append(df.format(LengthbyUnit)+" GB");
			break;
		case 4:
			result.append(df.format(LengthbyUnit)+" TB");
		}

		return result.toString();
	}
}
