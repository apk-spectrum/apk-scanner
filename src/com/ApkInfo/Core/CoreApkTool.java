package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ApkInfo.UI.MyProgressBarDemo;

public class CoreApkTool {
	static MyProgressBarDemo progressBarDemo;
	
	
	public static Boolean makeFolder(String FilePath) {
		File newDirectory = new File(FilePath);
		if(!newDirectory.exists()) {
			newDirectory.mkdir();
			return true;
		}
		return false;
	}
	
	public static void solveAPK(String APKFilePath, String solvePath) {
		String apkToolPath = CoreApkTool.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		apkToolPath = (new File(apkToolPath)).getParentFile().getPath();
		apkToolPath += File.separator + "apktool.jar";
		apkToolPath = apkToolPath.replaceAll("%20", " ");
		System.out.println("apkToolPath : " + apkToolPath);

		String[] cmd = {"java","-jar",apkToolPath,"d","-s","-f","-o",solvePath,"-p",solvePath, APKFilePath};
		
		exc(cmd,true);
		
	}
	
	
	static String exc(String[] cmd, boolean showLog) {
		try {
			String s = "";
			
			Process oProcess = new ProcessBuilder(cmd).start();
			
			String buffer = "";
		    // 외부 프로그램 출력 읽기
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));
		    
		    
		    if(showLog) {
			    while ((s =   stdOut.readLine()) != null) {
			  
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
	
}
