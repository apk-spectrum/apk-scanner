package com.apkscanner.gui.easymode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.border.LineBorder;

import com.android.ddmlib.IDevice;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.LogDlg;
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
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.gui.tabpanels.Resources;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

class EasyGuiMainPanel extends JPanel implements KeyEventDispatcher {
	private static Color maincolor = new Color(249, 249, 249);

	private EasyLightApkScanner apklightscanner;

	private EasyBordPanel bordPanel;
	private EasyContentsPanel contentsPanel;
	//private EasyPermissionPanel permissionPanel;
	private JFrame mainframe;
	// private boolean isinit= false;
	private int width, height;
	JLayeredPane layeredPane;
	DropEffectLabel dragdroplabel;
	EasyGuiToolScaleupPanel toolbarpanel;
	public static MessageBoxPool messagePool;
		
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

		contentsPanel = new EasyContentsPanel();
		
//		permissionPanel = new EasyPermissionPanel();
		
		width = contentsPanel.WIDTH;
//		height = contentsPanel.HEIGHT + permissionPanel.HEIGHT;
		height = contentsPanel.HEIGHT;
		
		toolbarpanel = new EasyGuiToolScaleupPanel(100, width);
		
//		FlatPanel spreadflat = new FlatPanel();
//		spreadflat.setPreferredSize(new Dimension(40, 40));
//		spreadflat.setshadowlen(3);
//		spreadflat.setBackground(new Color(217, 217, 217));
//		
//		spreadflat.add(new EasyButton(Resource.IMG_EASY_WINDOW_SPREAD.getImageIcon(35,35)));
		
		JPanel iconhoverpanel = new JPanel(new BorderLayout());
		iconhoverpanel.add(toolbarpanel, BorderLayout.NORTH);		
		iconhoverpanel.setBounds(0, 0, width-60, 100);
		iconhoverpanel.setOpaque(false);
		//iconhoverpanel.setBackground(Color.CYAN);
		
		setLayout(new BorderLayout());
		setBorder(new LineBorder(Color.BLACK, 0));

		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(width, height));

		JPanel contentspanel = new JPanel();
		contentspanel.setLayout(new BorderLayout());
		contentspanel.setBackground(maincolor);
		if (EasyGuiMain.isdecoframe) {
			bordPanel = new EasyBordPanel(mainframe);
			add(bordPanel, BorderLayout.PAGE_START);
		}
		contentspanel.add(contentsPanel, BorderLayout.CENTER);
		//contentspanel.add(permissionPanel, BorderLayout.PAGE_END);
		JPanel dummy = new JPanel(new BorderLayout());
		dummy.setPreferredSize(new Dimension(0, 40));
		dummy.add(new EasyButton(Resource.IMG_EASY_WINDOW_SPREAD.getImageIcon(35,35)), BorderLayout.EAST);
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

		new EasyFileDrop(this, dragdroplabel, new EasyFileDrop.Listener() {
			public void filesDropped(final java.io.File[] files) {
				clearApkinfopanel();
				// EasyGuiMain.corestarttime = System.currentTimeMillis();

				apklightscanner.setApk(files[0].getAbsolutePath());

				// layeredPane.repaint();
			}

			@Override
			public void filesEnter() {
				// TODO Auto-generated method stub
				// layeredPane.add(dragdroplabel, new Integer(2));
				dragdroplabel.setVisible(true);
			}

			@Override
			public void filesOut() {
				// TODO Auto-generated method stub
				// layeredPane.remove(dragdroplabel);
				dragdroplabel.setVisible(false);
			}
		});

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

	private void setframetext(String text) {
		if (!EasyGuiMain.isdecoframe) {
			mainframe.setTitle(text);
		} else {
			bordPanel.setWindowTitle(text);
		}
	}

	private void showApkinfopanel() {
		Log.d("showapkinfopanel");
		// bordPanel.setWindowTitle(apklightscanner.getApkInfo());
		EasyGuiMain.UIstarttime = System.currentTimeMillis();
		setframetext(
				Resource.STR_APP_NAME.getString() + " - " + new File(apklightscanner.getApkInfo().filePath).getName());
		Log.d(contentsPanel +"");
		contentsPanel.setContents(apklightscanner.getApkInfo());
		//permissionPanel.setPermission(apklightscanner.getApkInfo());

		Log.d(" UI set 시간 : " + (System.currentTimeMillis() - EasyGuiMain.UIstarttime) / 1000.0);
	}

	void showEmptyinfo() {
		// setframetext(Resource.STR_APP_NAME.getString());
		contentsPanel.setEmptypanel();
		//permissionPanel.setEmptypanel();
	}

	private void clearApkinfopanel() {
		// bordPanel.clear();
		contentsPanel.clear();
		//permissionPanel.clear();
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
				Log.d("Core 시간: " + ((System.currentTimeMillis() - EasyGuiMain.corestarttime) / 1000.0));
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
		contentsPanel.changeDeivce(devices);
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
}
