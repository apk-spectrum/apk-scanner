package com.apkscanner.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.apkspectrum.util.Log;

import javax.swing.JPanel;

import java.awt.Dimension;

@SuppressWarnings("serial")
public class TestAAPT extends JDialog{
	
	TestAAPT () {
		
		JPanel mainpanel = new JPanel();
		
		//ArrayList<ImageIcon> arrayImage = null; 
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
				//Log.d(""+ arrayImageFromfile.size());
				
				for(int i=0;i<arrayImageFromfile.size(); i++ ) {
					JLabel panel = new JLabel();
					panel.setIcon(new ImageIcon(arrayImageFromfile.get(i)));
					mainpanel.add(panel);
				}
				
				arrayImageFromfile.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		String apkFilePath = "/home/leejinhyeong/workspace/APKInfoDlgv2/MediaPlayer.apk";
		String tempFilePath = "/home/leejinhyeong/workspace/APKInfoDlgv2/tmp_image.png";
		
		ArrayList<String> imagePathList = new ArrayList<String>();
		ArrayList<ImageIcon> imageIconList0 = new ArrayList<ImageIcon>();
		ArrayList<ImageIcon> imageIconList1 = new ArrayList<ImageIcon>();
		ArrayList<ImageIcon> imageIconList2 = new ArrayList<ImageIcon>();
		ArrayList<ImageIcon> imageIconList3 = new ArrayList<ImageIcon>();


		Log.i("start");
		

		Log.i("get ImagePath List by ZipFile");
		try {
			ZipFile zipFile = new ZipFile(apkFilePath);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName(); 
				if(path.endsWith(".png"))
					imagePathList.add(path);
				
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i("get ImagePath List by ZipFile completed");
		Log.i("imagePathList size " + imagePathList.size());


		try {
			Log.i("Test #1 - ImageIO start");
			ZipFile zipFile = new ZipFile(apkFilePath);

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName(); 
				if(path.endsWith(".png") && path.startsWith("res")) {
		            InputStream is = zipFile.getInputStream(e) ;
		            imageIconList0.add(new ImageIcon(ImageIO.read(is), path));
				}
			}
			zipFile.close();
			Log.i("Test #1 - ImageIO completed");
			//Log.i("zip completed0.." + imageIconList0.size());
			//Log.i("zip completed0.." + imageIconList0.get .getDescription());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			Log.i("Test #2 - BufferedInputStream start");
			ZipFile zipFile = new ZipFile(apkFilePath);

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName(); 
				if(path.endsWith(".png") && path.startsWith("res")) {
		            byte[] buffer = new byte[(int) e.getSize()];
		            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(e) ); 
		            bis.read(buffer);
		            imageIconList1.add(new ImageIcon(buffer, path));
				}
			}
			zipFile.close();
			Log.i("Test #2 - BufferedInputStream completed");
			//Log.i("zip completed.." + imageIconList1.size());
			//Log.i("zip completed.." + imageIconList1.get .getDescription());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Log.i("Test #3 - jar URL start");
			ZipFile zipFile = new ZipFile(apkFilePath);
			String jarPath = "jar:file:"+apkFilePath+"!/";

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName(); 
				if(path.endsWith(".png") && path.startsWith("res")) {
		            imageIconList2.add(new ImageIcon(new URL(jarPath+path),path));
				}
			}
			zipFile.close();
			Log.i("Test #3 - jar URL completed");
			//Log.i("zip completed2.." + imageIconList2.size());
			//Log.i("zip completed2.." + imageIconList2.get .getDescription());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		try {
			Log.i("Test #4 - unzip start");
			ZipFile zipFile = new ZipFile(apkFilePath);
			
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName(); 
				if(path.endsWith(".png") && path.startsWith("res")) {
			        FileOutputStream fos = null;
			        try {
			            fos = new FileOutputStream(tempFilePath);
	
			            byte[] buffer = new byte[(int) e.getSize()];
			            int len = zipFile.getInputStream(e).read(buffer);
			            fos.write(buffer, 0, len);
			            
			            File iconfile = new File(tempFilePath);
			            imageIconList3.add(new ImageIcon(iconfile.getPath(), path));
			            iconfile.delete();
			        } finally {
			            if (fos != null) {
			                fos.close();
			            }
			        }
				}
			}
			zipFile.close();
			Log.i("Test #4 - unzip completed");
			//Log.i("zip completed3.." + imageIconList3.size());
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
}
