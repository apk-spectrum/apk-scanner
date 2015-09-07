package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.ApkInfo.CoreUtil.ZipUtils;
import com.ApkInfo.Resource.Resource;
import com.apkscanner.core.Log;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class TestAAPT extends JDialog{
	
	TestAAPT () {
		
		JPanel mainpanel = new JPanel();
		
		ArrayList<ImageIcon> arrayImage = null; 
		ArrayList<String> arrayImageFromfile = null;
		
		
		JScrollPane scroller = new JScrollPane(mainpanel);
		
		
		//getContentPane().add(panel, BorderLayout.CENTER);
		
		this.add(scroller);
		
//		Log.d("start memory image");		
//		
//		for(int j=0; j<1; j++) {
//			try {
//				arrayImage = ZipUtils.unimagezip(new File("/home/leejinhyeong/Desktop/PMApplication-release.apk"), new File("/home/leejinhyeong/Desktop/"), "icd_weather_14.png", false);
//				
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}		
//			
//			//Log.d(""+ arrayImage.size());
//			
//			for(int i=0;i<arrayImage.size(); i++ ) {
//				JLabel panel = new JLabel();
//				panel.setIcon(arrayImage.get(i));
//				mainpanel.add(panel);
//			}
//			arrayImage.clear();
//			
//		}
//		
//		Log.d("end memory image");
		
		
		
		
		Log.d("start file image");
		
		for(int j=0; j<1; j++) {
			try {
				arrayImageFromfile = ZipUtils.unimagezipfromfile(new File("/home/leejinhyeong/Desktop/PMApplication-release.apk"), new File("/home/leejinhyeong/Desktop/"), "ic_launcher_hoppin.png", false);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Log.d(""+ arrayImageFromfile.size());
			
			for(int i=0;i<arrayImageFromfile.size(); i++ ) {
				JLabel panel = new JLabel();
				panel.setIcon(new ImageIcon(arrayImageFromfile.get(i)));
				mainpanel.add(panel);
			}
			
			arrayImageFromfile.clear();
		}
		Log.d("end file image");
		
		
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(480,215));
		this.setResizable( true );
		this.setLocationRelativeTo(null);
		this.setModal(true);
		
		//this.pack();
		//dlgDialog.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
	
	

	
	public static void main(final String[] args)
	{
		String apkFile = "/home/leejinhyeong/Desktop/Schedulememo.apk";
		String targetRes = "0x7f0c093d";
		String AllString = "";
//		Log.i("aapt dump --values resource start");
//		String[] result = MyConsolCmd.exc(new String[] {Resource.BIN_AAPT_LNX.getPath(), "dump", "--values", "resources", apkFile});
//		Log.i("aapt dump --values resource completed");
//		Log.i("dump result line : " + result.length);
//		
//		Log.i("make string start");
//		//targetRes = ".*" + targetRes + ".*";
//		StringBuilder sb = new StringBuilder();
//		for(String s: result) {
//			sb.append(s) ;
//		}
//		Log.i("make string end");
//  		Log.i("find res id : " + targetRes);
//		Log.i("find index.. " + sb.indexOf(targetRes));
//		Log.i("find completed.. ");
		
		
		//drawable/icd_weather_27.png
//		try {
//			ZipUtils.unimagezip(new File("/home/leejinhyeong/Desktop/Schedulememo.apk"), new File("/home/leejinhyeong/Desktop/"), "icd_weather_14.png", false);
//			
//			
//			
//			
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//mZipUtils.unzip(new File(list[i].getAbsolutePath()),new File("./test/") , "AndroidManifest.xml", false);
		
		TestAAPT temp = new TestAAPT();
		
	}
	
}
