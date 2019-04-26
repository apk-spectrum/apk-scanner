package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.OverlayLayout;
import javax.swing.border.LineBorder;

import com.android.ddmlib.IDevice;
import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.DropTargetChooser;
import com.apkscanner.gui.EasyMainUI;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.ToolBar.ButtonSet;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.LogDlg;
import com.apkscanner.gui.DropTargetChooser.DefaultTargetObject;
import com.apkscanner.gui.easymode.contents.EasyBordPanel;
import com.apkscanner.gui.easymode.contents.EasyContentsPanel;
import com.apkscanner.gui.easymode.contents.EasyGuiToolPanel;
import com.apkscanner.gui.easymode.contents.EasyGuiToolScaleupPanel;
import com.apkscanner.gui.easymode.contents.EasyPermissionPanel;
import com.apkscanner.gui.easymode.core.EasyGuiAppFeatureData;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.gui.easymode.util.EasyFileDrop;
import com.apkscanner.gui.easymode.util.FlatPanel;
import com.apkscanner.gui.easymode.util.RoundPanel;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyGuiMainPanel extends JPanel implements KeyEventDispatcher, ComponentListener, DropTargetChooser.Listener  {
	private static Color maincolor = new Color(249, 249, 249);
	static private int PERMISSION_HEIGHT = 46;
	
	private EasyLightApkScanner apklightscanner;
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
	
	public EasyGuiMainPanel(JFrame mainframe, EasyLightApkScanner apkscanner) {
		Log.d("start EasyGuiMainPanel------------------------------------------------------------------------------------------------------------------------ ");
		this.apklightscanner = apkscanner;
		this.mainframe = mainframe;

		ToolEntryManager.Apkscanner = apkscanner;
		ToolEntryManager.mainframe = mainframe;
		messagePool = new MessageBoxPool(this.mainframe);

		KeyboardFocusManager ky = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(this);

		if (apklightscanner != null) {
			apklightscanner.setStatusListener(new GUIApkLightScannerListener());
		}

		EasycontentsPanel = new EasyContentsPanel();
		
		permissionPanel = new EasyPermissionPanel(PERMISSION_HEIGHT);
		
		
//		permissionPanel = new EasyPermissionPanel();
		
		width = EasycontentsPanel.WIDTH;
//		height = contentsPanel.HEIGHT + permissionPanel.HEIGHT;
		height = EasycontentsPanel.HEIGHT;
		
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
		dropTargetChooser = new DropTargetChooser(this);
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
//				// TODO Auto-generated method stub
//				// layeredPane.add(dragdroplabel, new Integer(2));
//				dragdroplabel.setVisible(true);
//			}
//
//			@Override
//			public void filesOut() {
//				// TODO Auto-generated method stub
//				// layeredPane.remove(dragdroplabel);
//				dragdroplabel.setVisible(false);
//			}
//		});

		// showEmptyinfo();
		// isinit=true;		
		apklightscanner.setReadyListener();
		Log.d("End EasyGuiMainPanel ------------------------------------------------------------------------------------------------------------------------");
	}

	class DropEffectLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		public DropEffectLabel(ImageIcon imageIcon) {
			// TODO Auto-generated constructor stub
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
				Resource.STR_APP_NAME.getString() + " - " + new File(apklightscanner.getApkInfo().filePath).getName());
		Log.d(EasycontentsPanel +"");
		EasycontentsPanel.setContents(apklightscanner.getApkInfo());
		permissionPanel.setPermission(apklightscanner.getApkInfo());

		DateFormat simple = new SimpleDateFormat("HH:mm:ss:SSS"); 
	    Date result = new Date(EasyMainUI.UIstarttime);
		
		Log.d(" UI set 시간 : " + (System.currentTimeMillis() - EasyMainUI.UIstarttime) / 1000.0 + "(start : " + simple.format(result));
	}

	public void showEmptyinfo() {
		// setframetext(Resource.STR_APP_NAME.getString());
		EasycontentsPanel.setEmptypanel();
		//permissionPanel.setEmptypanel();
	}

	private void clearApkinfopanel() {
		// bordPanel.clear();
		EasycontentsPanel.clear();
		permissionPanel.clear();
	}

	class GUIApkLightScannerListener implements EasyLightApkScanner.StatusListener {
		private int error = 0;

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
			// showEmptyinfo();
			messagePool.show(MessageBoxPool.MSG_FAILURE_OPEN_APK);
		}

		@Override
		public void onCompleted() {
			// TODO Auto-generated method stub
			// if(!isinit) return;
			
			if (this.error == 0) {
				showApkinfopanel();
				
				DateFormat simple = new SimpleDateFormat("HH:mm:ss:SSS"); 
			    Date result = new Date(EasyMainUI.corestarttime); 
			    Log.d("Core 시간: " + ((System.currentTimeMillis() - EasyMainUI.corestarttime) / 1000.0) + "(core start : " + simple.format(result));
				mainframe.setVisible(true);
				
				
				////////////////////////////////////for test
				//ToolEntryManager.excutePermissionDlg();				
				/////////////////////////////////////for test
				
			} else {
				showEmptyinfo();
				mainframe.setVisible(true);
			}

		}

		@Override
		public void onStateChanged(Status status) {
			// TODO Auto-generated method stub
			if (status.equals(Status.STANBY)) {

			}
		}
	}

	public void changeDevice(IDevice[] devices) {
		// TODO Auto-generated method stub
		EasycontentsPanel.changeDeivce(devices);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getID() == KeyEvent.KEY_RELEASED) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_F12:
				LogDlg.showLogDialog(mainframe);				
				break;
			case KeyEvent.VK_F11:
				apklightscanner.setReadyListener();		
				break;	
				
			default:
				return false;
			}
		}
		return false;
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
		// TODO Auto-generated method stub		
		changesize();
	}

	@Override
	public void componentMoved(ComponentEvent e) {	}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void filesDropped(Object dropedTarget, File[] files) {
		// TODO Auto-generated method stub
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
						apklightscanner.setApk(files[0].getAbsolutePath());
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
			String apkPath = apklightscanner.getApkInfo().filePath;
			((IExternalTool) dropedTarget).launch(apkPath, filePaths[0]);
		}
	}
}
