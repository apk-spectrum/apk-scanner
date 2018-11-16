package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.border.LineBorder;

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.contents.EasyBordPanel;
import com.apkscanner.gui.easymode.contents.EasyContentsPanel;

import com.apkscanner.gui.easymode.contents.EasyPermissionPanel;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
import com.apkscanner.gui.easymode.util.EasyFileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

class EasyGuiMainPanel extends JPanel {
	private static Color maincolor = new Color(249,249,249);
	
	private EasyLightApkScanner apklightscanner;
	
	//private EasyBordPanel bordPanel;
	private EasyContentsPanel contentsPanel;
	private EasyPermissionPanel permissionPanel;
	private JFrame mainframe;
	//private boolean isinit= false;
	private int width,height;
	JLayeredPane layeredPane;
	MyJLabel dragdroplabel;
	
		
	public EasyGuiMainPanel(JFrame mainframe, EasyLightApkScanner apkscanner) {
		this.apklightscanner = apkscanner;
		this.mainframe = mainframe;
		
		if(apklightscanner != null) {
			apklightscanner.setStatusListener(new ApkLightScannerListener());
		}
		
		contentsPanel = new EasyContentsPanel();
        permissionPanel = new EasyPermissionPanel();		
		
		width = contentsPanel.WIDTH;
		height = contentsPanel.HEIGHT +permissionPanel.HEIGHT;
		
		setLayout(new BorderLayout());
		setBorder(new LineBorder(Color.BLACK, 0));
		
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(width, height));
		
		JPanel contentspanel = new JPanel();
		
		contentspanel.setLayout(new BorderLayout());
		contentspanel.setBackground(maincolor);
		//bordPanel = new EasyBordPanel(mainframe);

		contentspanel.add(contentsPanel, BorderLayout.CENTER);			
		contentspanel.add(permissionPanel, BorderLayout.PAGE_END);		
	
		
		contentspanel.setBounds(0, 0, width, height);
		layeredPane.add(contentspanel, new Integer(1));
			 
		dragdroplabel = new MyJLabel(Resource.IMG_EASY_WINDOW_DRAGANDDROP.getImageIcon(100, 100));
		//dragdroplabel = new MyJLabel(null);
	    //Dimension d3 = new Dimension(width, height);
	    //btn1.setLayout(overlay);
	    //btn1.setMaximumSize(d3);    
		dragdroplabel.setBounds(0, 0, width, height);
		dragdroplabel.setBackground(new Color(213,134,145,223));
		layeredPane.add(dragdroplabel, new Integer(2));
		dragdroplabel.setVisible(false);
	    //btn1.setOpaque(true);
	    
	    add(layeredPane, BorderLayout.CENTER);
	    
		new EasyFileDrop(this, dragdroplabel, new EasyFileDrop.Listener() {
			public void filesDropped(final java.io.File[] files) {
				clearApkinfopanel();
				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						try {
							apklightscanner.clear(true);
							EasyGuiMain.corestarttime = System.currentTimeMillis();
							apklightscanner.setApk(files[0].getAbsolutePath());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
				
				//layeredPane.repaint();
			}
			
			@Override
			public void filesEnter() {
				// TODO Auto-generated method stub
				//layeredPane.add(dragdroplabel, new Integer(2));
				dragdroplabel.setVisible(true);	
			}
			@Override
			public void filesOut() {
				// TODO Auto-generated method stub
				//layeredPane.remove(dragdroplabel);
				dragdroplabel.setVisible(false);				
			}
		});
	    
		//showEmptyinfo();
		//isinit=true;
	}

	class MyJLabel extends JLabel {
	    public MyJLabel(ImageIcon imageIcon) {
			// TODO Auto-generated constructor stub
	    	super(imageIcon);
		}

		protected void paintComponent(Graphics g)
	    {			
	        g.setColor( getBackground() );
	        g.fillRect(0, 0, getWidth(), getHeight());
	        super.paintComponent(g);
	    }
	}
	
	
	private void showApkinfopanel() {
		Log.d("showapkinfopanel");
		//bordPanel.setWindowTitle(apklightscanner.getApkInfo());
		EasyGuiMain.UIstarttime =System.currentTimeMillis(); 
		
		mainframe.setTitle(Resource.STR_APP_NAME.getString() + " - "  + new File(apklightscanner.getApkInfo().filePath).getName());
		contentsPanel.setContents(apklightscanner.getApkInfo());
		permissionPanel.setPermission(apklightscanner.getApkInfo());
		
		Log.d( " UI set 시간 : " + ( System.currentTimeMillis() - EasyGuiMain.UIstarttime )/1000.0 );
	}
	
	private void showEmptyinfo() {
		mainframe.setTitle(Resource.STR_APP_NAME.getString());		
		contentsPanel.setEmptypanel();
	}
	
	private void clearApkinfopanel() {		
		//bordPanel.clear();
		contentsPanel.clear();
		permissionPanel.clear();
	}
    class ApkLightScannerListener implements EasyLightApkScanner.StatusListener {
    	private int error=0;
    	
		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess() {
			// TODO Auto-generated method stub
			this.error = 0;
		}

		@Override
		public void onError(int error) {
			// TODO Auto-generated method stub
			this.error = error;
			//showEmptyinfo();
		}

		@Override
		public void onCompleted() {
			// TODO Auto-generated method stub
			//if(!isinit) return;
			
			if(this.error == 0) {
				showApkinfopanel();
				long end = System.currentTimeMillis();
				Log.d( "Core 시간: " + ( (System.currentTimeMillis() - EasyGuiMain.corestarttime)/1000.0 ));
				mainframe.setVisible(true);
			} else {
				showEmptyinfo();
				mainframe.setVisible(true);
			}
		}
		@Override
		public void onStateChanged(Status status) {
			// TODO Auto-generated method stub
			if(status.equals(Status.STANBY)) {
				
			}
		}
    }
}


