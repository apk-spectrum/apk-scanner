package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.android.ddmlib.IDevice;
import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.gui.PlugInDropTargetChooser;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.component.DropTargetChooser;
import com.apkscanner.gui.component.DropTargetChooser.DefaultTargetObject;
import com.apkscanner.gui.easymode.contents.EasyBordPanel;
import com.apkscanner.gui.easymode.contents.EasyContentsPanel;
import com.apkscanner.gui.easymode.contents.EasyGuiToolScaleupPanel;
import com.apkscanner.gui.easymode.contents.EasyPermissionPanel;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiMainPanel extends JPanel implements ComponentListener, DropTargetChooser.Listener  {
	private static final long serialVersionUID = 4664365275666876359L;
	private static Color maincolor = new Color(249, 249, 249);
	static private int PERMISSION_HEIGHT = 46;
	
	private ApkScanner apkScanner;
	private int infoHashCode;
	EasyPermissionPanel permissionPanel;
	
	private EasyBordPanel bordPanel;
	private EasyContentsPanel EasycontentsPanel;
	//private EasyPermissionPanel permissionPanel;
	private JFrame mainframe;
	// private boolean isinit= false;
	private int width, height;
	JLayeredPane layeredPane;
	DropEffectLabel dragdroplabel;
	EasyGuiToolScaleupPanel toolbarpanel;
	public static MessageBoxPool messagePool;
	JPanel iconhoverpanel;
	JPanel contentspanel;
	private DropTargetChooser dropTargetChooser;
	
	public EasyGuiMainPanel(JFrame mainframe) {
		Log.d("start EasyGuiMainPanel------------------------------------------------------------------------------------------------------------------------ ");
		this.mainframe = mainframe;

		ToolEntryManager.mainframe = mainframe;
		messagePool = new MessageBoxPool(this.mainframe);

		EasycontentsPanel = new EasyContentsPanel();
		
		permissionPanel = new EasyPermissionPanel(PERMISSION_HEIGHT);
		
		
//		permissionPanel = new EasyPermissionPanel();
		
		width = EasyContentsPanel.WIDTH;
//		height = contentsPanel.HEIGHT + permissionPanel.HEIGHT;
		height = EasyContentsPanel.HEIGHT;
		
		toolbarpanel = new EasyGuiToolScaleupPanel(100, width);
		
//		FlatPanel spreadflat = new FlatPanel();
//		spreadflat.setPreferredSize(new Dimension(40, 40));
//		spreadflat.setshadowlen(3);
//		spreadflat.setBackground(new Color(217, 217, 217));
//		
//		spreadflat.add(new EasyButton(Resource.IMG_EASY_WINDOW_SPREAD.getImageIcon(35,35)));
		
		iconhoverpanel = new JPanel(new BorderLayout());
		iconhoverpanel.add(toolbarpanel, BorderLayout.NORTH);		
		iconhoverpanel.setBounds(0, 0, width, 100);
		iconhoverpanel.setOpaque(false);
		//iconhoverpanel.setBackground(Color.CYAN);
		
		setLayout(new BorderLayout());
		setBorder(new LineBorder(Color.BLACK, 0));

		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(width, height));

		contentspanel = new JPanel();
		contentspanel.setLayout(new BorderLayout());
		contentspanel.setBackground(maincolor);
		if (EasyMainUI.isdecoframe) {
			bordPanel = new EasyBordPanel(mainframe);
			add(bordPanel, BorderLayout.PAGE_START);
		}
		contentspanel.add(EasycontentsPanel, BorderLayout.CENTER);
		contentspanel.add(permissionPanel, BorderLayout.PAGE_END);
		
		RoundPanel dummy = new RoundPanel(new BorderLayout());
		dummy.setPreferredSize(new Dimension(0, 40));
		dummy.setshadowlen(5);
		dummy.setRoundrectColor(new Color(171,171,171));
		//dummy.add(new EasyButton(Resource.IMG_EASY_WINDOW_SPREAD.getImageIcon(35,35)), BorderLayout.EAST);
		contentspanel.add(dummy, BorderLayout.PAGE_START);
		contentspanel.setBounds(0, 0, width, height);
				
		layeredPane.add(contentspanel, new Integer(1));
		layeredPane.add(iconhoverpanel,new Integer(2));

		dragdroplabel = new DropEffectLabel(Resource.IMG_EASY_WINDOW_DRAGANDDROP.getImageIcon(100, 100));
		// dragdroplabel = new MyJLabel(null);
		// Dimension d3 = new Dimension(width, height);
		// btn1.setLayout(overlay);
		// btn1.setMaximumSize(d3);
		dragdroplabel.setBounds(0, 0, width, height);
		dragdroplabel.setBackground(new Color(213, 134, 145, 223));
		layeredPane.add(dragdroplabel, new Integer(3));
		dragdroplabel.setVisible(false);
		// btn1.setOpaque(true);

		add(layeredPane, BorderLayout.CENTER);
		
		////////////////////// test
		addComponentListener(this);
	
		// Drag & Drop event processing panel
		dropTargetChooser = new PlugInDropTargetChooser(this);
		mainframe.setGlassPane(dropTargetChooser);
		dropTargetChooser.setVisible(true);
		
		//mainframe.setDefaultLookAndFeelDecorated(true);
		//mainframe.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
			
		
		
//		JPanel panel = new JPanel() {
//	        @Override
//	        protected void paintComponent(Graphics g) {
//	            super.paintComponent(g);
//	            g.drawImage(Resource.IMG_APK_LOGO.getImageIcon().getImage(), 0, 0, getWidth(), getHeight(), this);
//	        }
//		};
//		panel.setPreferredSize(new Dimension(100, 100));
//		panel.setOpaque(false);
//		
//		setBackground(new Color(0,0,0,0));
//		setOpaque(false);
//		
//		//add(panel);
//		
//		mainframe.setUndecorated(true);
//		mainframe.setBackground(new Color(0,0,0,0));
//		setBorder(null);
//		
//		//panel.setBackground(Color.RED);
//		
//		add(panel, BorderLayout.SOUTH);
				
		//setOpaque(true);
		//////////////////////	

//		new EasyFileDrop(this, dragdroplabel, new EasyFileDrop.Listener() {
//			public void filesDropped(final java.io.File[] files) {
//				clearApkinfopanel();
//				// EasyGuiMain.corestarttime = System.currentTimeMillis();
//
//				apklightscanner.setApk(files[0].getAbsolutePath());
//
//				// layeredPane.repaint();
//			}
//
//			@Override
//			public void filesEnter() {
//				// layeredPane.add(dragdroplabel, new Integer(2));
//				dragdroplabel.setVisible(true);
//			}
//
//			@Override
//			public void filesOut() {
//				// layeredPane.remove(dragdroplabel);
//				dragdroplabel.setVisible(false);
//			}
//		});

		// showEmptyinfo();
		// isinit=true;
		//if(apkScanner != null) {
			//apkScanner.setReadyListener();
		//}

		Log.d("End EasyGuiMainPanel ------------------------------------------------------------------------------------------------------------------------");
	}

	public void setApkScanner(ApkScanner scanner) {
		if(scanner != null) {
			apkScanner = scanner;
			boolean changed = apkScanner.getApkInfo() != null
					&& apkScanner.getApkInfo().hashCode() != infoHashCode;
			apkScanner.setStatusListener(new GUIApkLightScannerListener(), changed);
		}
	}

	class DropEffectLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		public DropEffectLabel(ImageIcon imageIcon) {
			super(imageIcon);
		}

		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}

    @Override
    protected void paintComponent(Graphics g) 
    {
        if (g instanceof Graphics2D) {
            final int R = 240;
            final int G = 240;
            final int B = 240;

            Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 0), 0.0f, getHeight(), new Color(R, G, B, 255), true);
            Graphics2D g2d = (Graphics2D)g;
            g2d.setPaint(p);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
	
	private void setframetext(String text) {
		if (!EasyMainUI.isdecoframe) {
			mainframe.setTitle(text);
		} else {
			bordPanel.setWindowTitle(text);
		}
	}

	private void showApkinfopanel() {
		Log.d("showapkinfopanel");
		// bordPanel.setWindowTitle(apklightscanner.getApkInfo());
		EasyMainUI.UIstarttime = System.currentTimeMillis();
		setframetext(
				new File(apkScanner.getApkInfo().filePath).getName() + " - " + Resource.STR_APP_NAME.getString());
		
		EasycontentsPanel.setContents(apkScanner.getApkInfo());
		
		new Thread(new Runnable() {
			public void run() {
				permissionPanel.setLoadingpanel();
				permissionPanel.setPermission(apkScanner.getApkInfo());
			}
		}).start();
		
		DateFormat simple = new SimpleDateFormat("HH:mm:ss:SSS"); 
	    Date result = new Date(EasyMainUI.UIstarttime);
		
		Log.d(" UI set 시간 : " + (System.currentTimeMillis() - EasyMainUI.UIstarttime) / 1000.0 + "(start : " + simple.format(result));
	}

	public void showEmptyinfo() {
		// setframetext(Resource.STR_APP_NAME.getString());
		Log.d("showEmptyinfo");
		EasycontentsPanel.setEmptypanel();
		//permissionPanel.setEmptypanel();
	}

	public void showloadinginfo(String msg) {
		Log.d("showLoadinginfo");
		EasycontentsPanel.setLoadingpanel(msg);
	}
	
	private void clearApkinfopanel() {
		// bordPanel.clear();
		EasycontentsPanel.clear();
		permissionPanel.clear();
	}

	class GUIApkLightScannerListener implements ApkScanner.StatusListener {
		private int error = 0;

		@Override
		public void onStart(long estimatedTime) {

		}

		@Override
		public void onSuccess() {
			Log.d("onSuccess()");
			this.error = 0;
		}

		@Override
		public void onError(int error) {
			Log.d("onError()" + error);
			this.error = error;
			// showEmptyinfo();
			messagePool.show(MessageBoxPool.MSG_FAILURE_OPEN_APK);
		}

		@Override
		public void onCompleted() {
			// if(!isinit) return;
			if (this.error == 0) {
				showApkinfopanel();
				dropTargetChooser.setExternalToolsVisible(true);
				infoHashCode = apkScanner.getApkInfo().hashCode();

				DateFormat simple = new SimpleDateFormat("HH:mm:ss:SSS"); 
			    Date result = new Date(EasyMainUI.corestarttime); 
			    Log.d("Core 시간: " + ((System.currentTimeMillis() - EasyMainUI.corestarttime) / 1000.0) + "(core start : " + simple.format(result));

				////////////////////////////////////for test
				//ToolEntryManager.excutePermissionDlg();				
				/////////////////////////////////////for test
			} else {
				showEmptyinfo();
			}
		}

		@Override
		public void onStateChanged(Status status) {
			Log.d("onStateChanged()" + status);
			if (status.equals(Status.STANBY)) {

			}
		}

		@Override
		public void onProgress(int step, final String msg) {
			Log.d("onProgress()" + step + ":" + msg);
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					showloadinginfo(msg);
				}
			});
		}
	}

	public void changeDevice(IDevice[] devices) {
		EasycontentsPanel.changeDeivce(devices);
	}

	private void changesize() {
		int w = getSize().width;
		int h = getSize().height;
				
		toolbarpanel.setPreferredSize(new Dimension(100, w));
		iconhoverpanel.setBounds(0, 0, w, 100);
		layeredPane.setPreferredSize(new Dimension(w, h));
		contentspanel.setBounds(0, 0, w, h);
		dragdroplabel.setBounds(0, 0, w, h);
		
		EasycontentsPanel.changesize(w -5, h - 90); // dummy 40
		updateUI();
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		changesize();
	}

	@Override
	public void componentMoved(ComponentEvent e) {	}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void filesDropped(Object dropedTarget, final File[] files) {
		final String[] filePaths = new String[files.length];
		for(int i = 0; i< files.length; i++) {
			try {
				filePaths[i] = files[i].getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if(dropedTarget instanceof DefaultTargetObject) {
			switch((DefaultTargetObject)dropedTarget) {
			case DROPED_TARGET_APK_OPEN:
				Log.i("filesDropped()");
				dropTargetChooser.setExternalToolsVisible(false);
				Thread thread = new Thread(new Runnable() {
					public void run()
					{
//						apkScanner.clear(false);
//						apkScanner.openApk(filePaths[0]);
						clearApkinfopanel();
						apkScanner.clear(true);
						EasyMainUI.corestarttime = System.currentTimeMillis();
						apkScanner.openApk(files[0].getAbsolutePath());
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
				break;
			case DROPED_TARGET_NEW_WIN:
				Launcher.run(filePaths[0]);
				break;
			}
		} else if(dropedTarget instanceof IExternalTool) {
			String apkPath = apkScanner.getApkInfo().filePath;
			((IExternalTool) dropedTarget).launch(apkPath, filePaths[0]);
		}
	}
}
