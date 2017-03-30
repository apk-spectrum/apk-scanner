package com.apkscanner.gui.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.apkscanner.Launcher;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.ApkInstaller.ApkInstallerListener;
import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScannerStub;
import com.apkscanner.core.scanner.ApkScannerStub.Status;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ManifestInfo;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.gui.util.AbstractTabRenderer;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.ImagePanel;
import com.apkscanner.gui.util.SimpleCheckTableModel;
import com.apkscanner.gui.util.SimpleCheckTableModel.TableRowObject;
import com.apkscanner.gui.util.JXTabbedPane;

import com.apkscanner.resource.Resource;

import com.apkscanner.tool.adb.AdbDeviceManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.tool.adb.AdbPackageManager;
import com.apkscanner.tool.adb.AdbPackageManager.PackageInfo;
import com.apkscanner.tool.adb.AdbWrapper;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class ApkInstallWizard
{
	public static final int STATUS_INIT = 0;
	public static final int STATUS_DEVICE_SCANNING = 1;
	public static final int STATUS_WAIT_FOR_DEVICE = 2;
	public static final int STATUS_DEVICE_REFRESH = 3;
	public static final int STATUS_SELECT_DEVICE = 4;
	public static final int STATUS_PACKAGE_SCANNING = 5;
	public static final int STATUS_CHECK_PACKAGES = 6;
	public static final int STATUS_SET_INSTALL_OPTION = 7;
	public static final int STATUS_INSTALLING = 8;
	public static final int STATUS_COMPLETED = 9;
	
	public static final int FLAG_OPT_INSTALL	 	= 0x0100;
	public static final int FLAG_OPT_PUSH			= 0x0200;
	public static final int FLAG_OPT_PUSH_OVERWRITE = 0x0400;
	
	public static final int FLAG_OPT_INSTALL_INTERNAL = 0x0001;
	public static final int FLAG_OPT_INSTALL_EXTERNAL = 0x0002;
	
	public static final int FLAG_OPT_PUSH_SYSTEM	= 0x0001;
	public static final int FLAG_OPT_PUSH_PRIVAPP	= 0x0002;
	public static final int FLAG_OPT_PUSH_DATA		= 0x0004;
	
	public static final int FLAG_OPT_EXTRA_RUN		= 0x0010;
	public static final int FLAG_OPT_EXTRA_REBOOT	= 0x0020;
	public static final int FLAG_OPT_EXTRA_WITH_LIB	= 0x0040;
	public static final int FLAG_OPT_EXTRA_DELETE_EXISTING_APK = 0x0080;

	// UI components
	private Window wizard;
	private ProgressPanel progressPanel;
	private ContentPanel contentPanel;
	private ControlPanel controlPanel;
	private UIEventHandler uiEventHandler = new UIEventHandler();
	
	private int status;
	private int flag;
	private ArrayList<InstalledReport> installReports;

	private DeviceStatus[] targetDevices;
	private PackageInfo[] installedPackage;
	private ApkInfo apkInfo;
	
	private ApkScannerStub apkScanner;

	public class ApkInstallWizardDialog  extends JDialog
	{
		private static final long serialVersionUID = 2018466680871932348L;

		public ApkInstallWizardDialog() {
			dialog_init(null);
		}
		
		public ApkInstallWizardDialog(Frame owner) {
			super(owner);
			dialog_init(owner);
		}
		
		public ApkInstallWizardDialog(JDialog owner) {
			super(owner);
			dialog_init(owner);
		}
		
		private void dialog_init(Component owner) {
			setTitle(Resource.STR_TITLE_INSTALL_WIZARD.getString());
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setResizable(true);
			setModal(false);

			initialize(this);
			setLocationRelativeTo(owner);
		}
	}
	
	public class ApkInstallWizardFrame extends JFrame
	{
		private static final long serialVersionUID = -5642057585041759436L;
		
		public ApkInstallWizardFrame() {
			frame_init();
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}

		public ApkInstallWizardFrame(Frame owner) {
			frame_init();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}

		public ApkInstallWizardFrame(JDialog owner) {
			frame_init();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
		
		private void frame_init()
		{
			try {
				if(Resource.PROP_CURRENT_THEME.getData()==null) {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} else {
					UIManager.setLookAndFeel(Resource.PROP_CURRENT_THEME.getData().toString());
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
			
			setTitle(Resource.STR_TITLE_INSTALL_WIZARD.getString());
			setResizable(true);

			initialize(this);
			setLocationRelativeTo(null);
			
			// Closing event of window be delete tempFile
			addWindowListener(uiEventHandler);
		}
	}
    private void setmargin(JPanel c, int size) {
    	c.setBorder(BorderFactory.createEmptyBorder(size, size, size, size));
    }
	
	
    private GridBagConstraints addGrid(GridBagConstraints gbc, 
            int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
      gbc.gridx = gridx;
      gbc.gridy = gridy;
      gbc.gridwidth = gridwidth;
      gbc.gridheight = gridheight;
      gbc.weightx = weightx;
      gbc.weighty = weighty;
      return gbc;
    }
    
	private class ProgressPanel extends JPanel
	{
		private static final long serialVersionUID = 6145481552592676895L;
		
		JPanel ProgressStepPanel;
		JPanel TextStepPanel;
		private final int STEPMAX = 5;
		
		private final int COLOR_STEP_NOTFINISH = 0; 
		private final int COLOR_STEP_PROCESSING = 1; 		
		private final int COLOR_STEP_FINISHED = 2;
		
		int CurrentProgress=0;
		private final String [] outtexts= {"  SELECT DEVICE", "   FIND PACKAGE", "OPTIONS", "INSTALLING", "FINISH     "};
		
		private final Color []Colorset = {new Color(222,228,228), new Color(52,152,220),new Color(46,204,114)};
				
        private EllipseLayout[] ellipselabel = new EllipseLayout[STEPMAX];			
        private Linelayout[] linelabel = new Linelayout[STEPMAX-1];
        private AnimationLabel[] animatlabel = new AnimationLabel[STEPMAX];
        
		public class ColorBase {
			int state;
	        public Timer timer = null;
	        public Color currentColor;
	        public Boolean isAnimation= false;
	        private static final int DELAY = 30;
	        private static final int INC = 4;
	        public Container childContainer;
	        
			// disable 223,227,228
			// ing 52,152,219
			// finish 46,204,113
	        public int addColorINC(int base, int current) {
	        	
	        	if(Math.abs(base-current) < 10) {
	        		return base;
	        	}
	        	
	        	if(base >= current) {
	        		return current+INC;
	        	} else {
	        		return current-INC;
	        	}
	        }
	        
	        public ColorBase(Container child) {
	        	state = 0;
	        	childContainer = child;
	        	currentColor = new Color(222,228,228);
	            timer = new Timer(DELAY, new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                	int r=0,g=0,b=0;	                	
	                		r = addColorINC(Colorset[state].getRed() , currentColor.getRed());
	                		g = addColorINC(Colorset[state].getGreen() , currentColor.getGreen());
	                		b = addColorINC(Colorset[state].getBlue() , currentColor.getBlue());
	                	currentColor = new Color(r,g,b);
	                	//if(Math.abs(r-Colorset[state].getRed()) < 10 && Math.abs(g-Colorset[state].getGreen()) < 10 && 
	                			//Math.abs(b-Colorset[state].getBlue()) < 10) {
	                	if(currentColor.equals(Colorset[state]))	{	                		
	                		currentColor = Colorset[state];
	                		timer.stop();
	                		isAnimation = false;
	                	}
	                	//ColorBase.this.getParent().repaint();
	                	if(childContainer!=null)childContainer.getParent().repaint();
	                }
	            });
	        }
		    public void setAnimation() {
		    	isAnimation = true;
		    	currentColor = Colorset[state];
		    	timer.start();
		    }
		}
		
		
		public class EllipseLayout extends JPanel {
			private static final long serialVersionUID = 5831964884908650735L;
			String /*outtext,*/ intext;
			ColorBase colorbase;
			public EllipseLayout() {
				super();
				//outtext = new String("");
				intext = new String("");
				colorbase = new ColorBase(this);				
				colorbase.state = 0;
			}
			
		  public void drawCenteredString(String s, int w, int h, Graphics g) {
			    FontMetrics fm = g.getFontMetrics();
			    int x = (w - fm.stringWidth(s)) / 2;
			    int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
			    g.drawString(s, x, y);
			  }
			
		    public void paintComponent(Graphics g)
		    {	
		        Graphics2D g2 = (Graphics2D)g;
		        		    	
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        
		    	Dimension size = getSize();
		    	if(colorbase.isAnimation) {
		    		g.setColor(colorbase.currentColor);
		    	} else {
		    		g.setColor(Colorset[colorbase.state]);
		    	}
		    	
		    	if(size.getWidth() <= size.getHeight()) {
		    		//g.fillOval(0,(int)(size.getHeight()/2 - size.getWidth()/2), (int)size.getWidth(), (int)size.getWidth());
		    		Shape theCircle = new Ellipse2D.Double(0,(size.getHeight()/2 - size.getWidth()/2), size.getWidth(), size.getWidth());
		    		g2.fill(theCircle);
		    	} else {
		    		
		    	}
		    	//g.setFont(g.getFont().deriveFont(15f));
		    	//g.drawString(outtext, 0, (int)size.getHeight()-10);
		    	g.setFont(g.getFont().deriveFont(30f));
		    	g.setColor(Color.WHITE);
		    	//g.drawString(intext, (int)(size.getWidth()/2-15), (int)(size.getHeight()/2+ 15));
		    	drawCenteredString(intext, (int)size.getWidth(), (int)size.getHeight(), g);
		    	
		    }
		    public void setEllipseText(String str) {
		    	intext = str;
		    }
		    
		    public void setDescriptionText(String str) {
		    	//outtext = str;
		    }
		    
		    public void setState(int state) {
		    	if(colorbase.state == state) return;
		    	colorbase.setAnimation();		    	
		    	colorbase.state = state;
		    }
		}
		
		public class Linelayout extends JLabel {
			private static final long serialVersionUID = 4192134315491972328L;
			ColorBase colorbase;
			
			public Linelayout() {
				super();
				colorbase = new ColorBase(this);
				colorbase.state = 0;
			}
			public void paintComponent(Graphics g)
		    {
				Dimension size = getSize();
				Graphics2D g2 = (Graphics2D) g;
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        
		    	if(colorbase.isAnimation) {
		    		g.setColor(colorbase.currentColor);
		    	} else {
		    		g.setColor(Colorset[colorbase.state]);
		    	}
				g2.setStroke(new BasicStroke(8) );				
				//g.drawLine(0, (int)(size.getHeight()/2), (int)size.getWidth(), (int)(size.getHeight()/2));
				
				Shape Line = new Line2D.Double(0, size.getHeight()/2, size.getWidth(), size.getHeight()/2);
	    		g2.draw(Line);
				
		    }
		    
		    public void setState(int state) {
		    	
		    	if(colorbase.state == state) return;		    	
		    	colorbase.setAnimation();		    	
		    	colorbase.state = state;
		    }
		}
		
		public class AnimationLabel extends JLabel {
			private static final long serialVersionUID = 4192134315491972328L;
			ColorBase colorbase;
			
			public AnimationLabel(String string, int center) {
				super(string, center);
				colorbase = new ColorBase(this);
				colorbase.state = 0;				
			}
			public void paintComponent(Graphics g)
		    {
				super.paintComponent(g);				
				Graphics2D g2 = (Graphics2D) g;
		        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        
		    	if(colorbase.isAnimation) {
		    		this.setForeground(colorbase.currentColor);
		    	} else {
		    		this.setForeground(Colorset[colorbase.state]);
		    	}		    	
		    }
		    public void setState(int state) {
		    	
		    	if(colorbase.state == state) return;		    	
		    	colorbase.setAnimation();		    	
		    	colorbase.state = state;
		    }
		}
		
		

		public ProgressPanel() {
			super(new BorderLayout());
			
			ProgressStepPanel = new JPanel();
			ProgressStepPanel.setLayout(new GridBagLayout());
			ProgressStepPanel.setBackground(Color.WHITE);
			
			
			TextStepPanel = new JPanel();
			TextStepPanel.setLayout(new GridLayout(1,STEPMAX));
			TextStepPanel.setBackground(Color.WHITE);
			
			
            GridBagConstraints gbc = new GridBagConstraints();            
            gbc.fill = GridBagConstraints.BOTH;
			
            for(int i=0; i< STEPMAX; i++) {
            	animatlabel[i] = new AnimationLabel(outtexts[i], SwingConstants.CENTER);
            	animatlabel[i].setFont(new Font(animatlabel[i].getFont().getName(), Font.BOLD, 14));
            	animatlabel[i].setForeground(Colorset[COLOR_STEP_NOTFINISH]);
            	//ellipselabel[i].setOpaque(true);
            	//ellipselabel[i].setBackground(new Color(i*50,100,100));
            	TextStepPanel.add(animatlabel[i]);
            }
            
            JPanel marginlabel = new JPanel();
            marginlabel.setBackground(Color.WHITE);			
			ProgressStepPanel.add(marginlabel, addGrid(gbc, 0, 0, 1, 1, 1, 1));            
			for(int i=0;i < STEPMAX-1; i++) {
				ellipselabel[i] = new EllipseLayout();
				ellipselabel[i].setOpaque(true);
				ellipselabel[i].setDescriptionText(outtexts[i]);
				ellipselabel[i].setEllipseText(""+i);
				ProgressStepPanel.add(ellipselabel[i], addGrid(gbc,  i*2+1, 0, 1, 1, 1, 1));
				
				linelabel[i] = new Linelayout();
				linelabel[i].setOpaque(true);
				ProgressStepPanel.add(linelabel[i], addGrid(gbc,  (i*2+2), 0, 1, 1, 2, 1));
			}

			ellipselabel[STEPMAX-1] = new EllipseLayout();
			ellipselabel[STEPMAX-1].setOpaque(true);
			ellipselabel[STEPMAX-1].setDescriptionText(outtexts[STEPMAX-1]);
			ellipselabel[STEPMAX-1].setEllipseText(""+(STEPMAX-1));
			ProgressStepPanel.add(ellipselabel[STEPMAX-1], addGrid(gbc, STEPMAX*2-1, 0, 1, 1, 1, 1));
			
            JPanel marginlabel2 = new JPanel();
            marginlabel2.setBackground(Color.WHITE);
			ProgressStepPanel.add(marginlabel2, addGrid(gbc, STEPMAX*2, 0, 1, 1, 1, 1));			
			
			ProgressStepPanel.setPreferredSize(new Dimension(0, 60));			
			
			add(ProgressStepPanel, BorderLayout.CENTER);
			add(TextStepPanel, BorderLayout.SOUTH);
			
			// set status
			setStatus(STATUS_INIT);
			
		}
		
		private void setProgressColor(int state) {
			Log.d("state : " + state);
			
			if(state==0) {
				for(int i=0; i< STEPMAX; i++) {
					ellipselabel[i].setState(COLOR_STEP_NOTFINISH);
					animatlabel[i].setState(COLOR_STEP_NOTFINISH);
				}
				for(int i=0; i< STEPMAX-1; i++) {
					linelabel[i].setState(COLOR_STEP_NOTFINISH);
				}
				return ;
			}
			
			switch(state) {
			case 2:
				String lable = targetDevices[0].model;
				if(targetDevices.length > 1) {
					lable = String.format("%1$s 외 %2$d", targetDevices[0].model, targetDevices.length-1);
					//lable = String.format("%2$d beside %1$s", targetDevices[0].model, targetDevices.length);
				}
				animatlabel[0].setText(lable);
				break;
			case 3:
				int packCount = 0;
				if(installedPackage != null) {
					for(int i = 0; i < installedPackage.length; i++) {
						if(installedPackage[i] != null) packCount++;
					}
				}
				animatlabel[1].setText(packCount > 0 ? packCount + " DEVICE" : "NOTHING");
				break;
			case 4:
				animatlabel[2].setText((flag & FLAG_OPT_INSTALL) != 0 ? "INSTALL" : "PUSH");
				break;
			default:
				break;
			}
			
			for(int i=1; i <= state; i++) {
				ellipselabel[i-1].setState(COLOR_STEP_FINISHED);
				animatlabel[i-1].setState(COLOR_STEP_FINISHED);
				if(i!=state)linelabel[i-1].setState(COLOR_STEP_FINISHED);
			}
			for(int i=state; i< STEPMAX; i++) {
				ellipselabel[i].setState(COLOR_STEP_NOTFINISH);
				animatlabel[i].setState(COLOR_STEP_NOTFINISH);
				animatlabel[i-1].setText(outtexts[i-1]);
			}			
			linelabel[state-1].setState(COLOR_STEP_NOTFINISH);
			ellipselabel[state-1].setState(COLOR_STEP_PROCESSING);
			animatlabel[state-1].setState(COLOR_STEP_PROCESSING);			
		}
		
		public void setStatus(int status) {
			int newStatus = CurrentProgress;
			switch(status) {
			case STATUS_INIT:
			case STATUS_DEVICE_SCANNING:
			case STATUS_WAIT_FOR_DEVICE:
			case STATUS_SELECT_DEVICE:
				newStatus = 1;
				break;
			case STATUS_PACKAGE_SCANNING:
			case STATUS_CHECK_PACKAGES:
				newStatus = 2;
				break;
			case STATUS_SET_INSTALL_OPTION:
				newStatus = 3;
				break;
			case STATUS_INSTALLING:
				newStatus = 4;
				break;
			case STATUS_COMPLETED:
				newStatus = 5;
				break;
			default:
				break;
			}
			if(CurrentProgress != newStatus) {
				CurrentProgress = newStatus;
				setProgressColor(CurrentProgress);
			}
		}
	    
	}
	
	private class ContentPanel extends JPanel
	{
		private static final long serialVersionUID = -680173960208954055L;

		public static final String CONTENT_INIT = "CONTENT_INIT";
		public static final String CONTENT_LOADING = "CONTENT_LOADING";
		public static final String CONTENT_DEVICE_SCANNING = "CONTENT_DEVICE_SCANNING";
		public static final String CONTENT_SELECT_DEVICE = "CONTENT_SELECT_DEVICE";
		public static final String CONTENT_PACKAGE_SCANNING = "CONTENT_PACKAGE_SCANNING";
		public static final String CONTENT_CHECK_PACKAGES = "CONTENT_CHECK_PACKAGES";
		public static final String CONTENT_SET_INSTALL_OPTION = "CONTENT_SET_INSTALL_OPTION";
		public static final String CONTENT_INSTALLING = "CONTENT_INSTALLING";
		public static final String CONTENT_COMPLETED = "CONTENT_COMPLETED";
		

		public static final String CTT_ACT_CMD_REFRESH = "CTT_ACT_CMD_REFRESH";
		public static final String CTT_ACT_CMD_SELECT_ALL = "CTR_ACT_CMD_SELECT_ALL";
		
		
		private JButton dev_refreshButton;
		private JButton dev_selectAllButton;
		
		private JList<String> pack_deviceList;
		private JTextArea pack_textPakcInfo;
		private JComboBox<String> pack_comboStartActivity;
		private JButton pack_btnSave;
		private JButton pack_btnOpen;
		private JButton pack_btnLaunch;
		private JButton pack_btnRemove;
		
		public class DeviceTableObject implements TableRowObject {
			public Boolean buse;
			public DeviceStatus devInfo;
			
			public DeviceTableObject(Boolean buse, DeviceStatus devInfo) {
				this.buse = buse;
				this.devInfo = devInfo;
			}
			
			@Override
			public Object get(int columnIndex) {
		    	switch(columnIndex) {
		    	case 0:
		    		return buse;        		
		    	case 1:
		    		return devInfo.name;
		    	case 2:
		    		return devInfo.model;
		    	case 3:
		    		return devInfo.device;
		    	case 4:
		    		return devInfo.status;
		    	}
		    	return null;
			}

			@Override
			public void set(int columnIndex, Object obj) {
		    	switch(columnIndex) {
		    	case 0:
		    		buse = (Boolean) obj;
		    		break;        		
		    	default:
		    		break;
		    	}
			}
		}

		String[] columnNames = {"", "SERIAL NUMBER", "MODEL", "PROJECT", "STATUS"};
		private JTable targetDeviceTable;
		private ArrayList<TableRowObject> tableDatas;
		
		private JPanel panel_select_device;
		private JPanel panel_check_package;
		private JPanel panel_set_install_option;
		
		private JLabel loadingMessageLable;
		
		private void updateSelectedAllButtonUI(SimpleCheckTableModel tableModel) {
			int rowCount = tableModel.getRowCount();
			int selectedRowCount = tableModel.getSelectedRowCount();
			if(rowCount > 0) {
				if(rowCount == selectedRowCount) {
					dev_selectAllButton.setText("Unselect All");
				} else {
					dev_selectAllButton.setText("Select All");
				}
				dev_selectAllButton.setEnabled(true);
			} else {
				dev_selectAllButton.setText("Select All");
				dev_selectAllButton.setEnabled(false);
			}
			if(controlPanel != null) {
				controlPanel.btnNext.setEnabled(selectedRowCount != 0);
			}
		}
		
		public void refreshDeviceList() {
			if(dev_refreshButton == null || !dev_refreshButton.isEnabled()) return;
			dev_refreshButton.setEnabled(false);
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						targetDevices = AdbDeviceManager.scanDevices();
						setStatus(STATUS_DEVICE_REFRESH);
						dev_refreshButton.setEnabled(true);
					}
				}
			}).start();
		}
		
		public void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
				double... percentages) {
				double total = 0;
				for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
				total += percentages[i];
				}
				
				for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
				TableColumn column = table.getColumnModel().getColumn(i);		        
				column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
				}
			}
		
		private JPanel createSelectDevicePanel(final ActionListener listener) {
			final JPanel newSelctDevicePanel = new JPanel(new GridBagLayout());
					
			JLabel textSelectDevice = new JLabel("Select target device!");
			textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
			
			targetDeviceTable = new JTable();
			tableDatas = new ArrayList<TableRowObject>();

	        final SimpleCheckTableModel tableModel = new SimpleCheckTableModel(columnNames, tableDatas);
	        tableModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent arg0) {
					if(arg0.getColumn() != 0) return;
					SimpleCheckTableModel tableModel = (SimpleCheckTableModel)arg0.getSource();
					if(tableModel.getRowCount() > 0) {
						for(int row = arg0.getFirstRow(); row <= arg0.getLastRow(); row++) {
							boolean checked = (boolean)tableModel.getValueAt(row, 0);
							if(checked) {
								String status = (String)tableModel.getValueAt(row, 4);
								if(!"device".equals(status)) {
									tableModel.setValueAt(false, row, 0);
								}
							}
						}
					}
					updateSelectedAllButtonUI(tableModel);
				}
			});
	        targetDeviceTable.setModel(tableModel);
			
			targetDeviceTable.setPreferredScrollableViewportSize(targetDeviceTable.getPreferredSize());
			targetDeviceTable.setFillsViewportHeight(true);
			setJTableColumnsWidth(targetDeviceTable,530,1,125,200,100,100);
			//targetDeviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        JScrollPane targetDevicesPane = new JScrollPane(targetDeviceTable);
			
			dev_refreshButton = new JButton("Refresh(F5)");
			dev_refreshButton.setActionCommand(CTT_ACT_CMD_REFRESH);
			dev_refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					refreshDeviceList();
					listener.actionPerformed(arg0);
				}
			});

			dev_selectAllButton = new JButton("Select All");
			dev_selectAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int rowCount = tableModel.getRowCount();
					int selectedRowCount = tableModel.getSelectedRowCount();
					boolean check = rowCount != selectedRowCount;

					for(TableRowObject rowObj : tableDatas) {
						rowObj.set(0, check && "device".equals(rowObj.get(4)));
					}
	    			
					((AbstractTableModel) targetDeviceTable.getModel()).fireTableDataChanged();
					targetDeviceTable.updateUI();
					newSelctDevicePanel.repaint();
					
					updateSelectedAllButtonUI(tableModel);
					listener.actionPerformed(arg0);
				}
			});
			updateSelectedAllButtonUI(tableModel);
			
			JPanel buttonsetPanel = new JPanel(new BorderLayout());
			buttonsetPanel.add(dev_refreshButton, BorderLayout.WEST);
			buttonsetPanel.add(dev_selectAllButton, BorderLayout.EAST);	
			
			GridBagConstraints gbc = new GridBagConstraints();            
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
			gbc.anchor = GridBagConstraints.NORTH;
			newSelctDevicePanel.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 1));
			gbc.fill = GridBagConstraints.BOTH;
			newSelctDevicePanel.add(targetDevicesPane,addGrid(gbc, 0, 1, 1, 1, 1, 7));
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTH;			
			newSelctDevicePanel.add(buttonsetPanel,addGrid(gbc, 0, 2, 1, 1, 1, 4));
			
			setmargin(newSelctDevicePanel, 5);
			
			return newSelctDevicePanel;
		}
		
		JPanel createFindPackagePanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());	
			JPanel mainpanel = new JPanel(new BorderLayout());
			JPanel Listpanel = new JPanel(new BorderLayout());
			JPanel packagepanel = new JPanel(new BorderLayout());
			JPanel appstartpanel = new JPanel(new BorderLayout());
			JPanel buttonpanel = new JPanel();
			
			JLabel textSelectDevice = new JLabel("installed same package!");
			textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
			
			pack_deviceList = new JList<String>(new DefaultListModel<String>());
			pack_deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			pack_deviceList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					if(pack_deviceList.getSelectedIndex() <= -1) return;
					String selDev = pack_deviceList.getSelectedValue();
					for(int i=0; i < targetDevices.length; i++) {
						if(targetDevices[i].toString().equals(selDev)) {
							pack_textPakcInfo.setText(installedPackage[i].toString());
							break;
						}
					}
				}
			});
			JScrollPane listscrollPane = new JScrollPane(pack_deviceList);
			
			pack_textPakcInfo = new JTextArea();
			pack_textPakcInfo.setEditable(false);
			JScrollPane textViewscrollPane = new JScrollPane(pack_textPakcInfo);
			
			pack_comboStartActivity = new JComboBox<String>(new DefaultComboBoxModel<String>());
		    
		    pack_btnLaunch = new JButton("Launch");
		    pack_btnLaunch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String selDev = pack_deviceList.getSelectedValue();
					for(DeviceStatus dev: targetDevices) {
						if(dev.toString().equals(selDev)) {
							launchApp(dev.name);
							break;
						}
					}
				}
		    });
		    pack_btnOpen = new JButton("Open");
		    pack_btnOpen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(pack_deviceList.getSelectedIndex() <= -1) return;
					String selDev = pack_deviceList.getSelectedValue();
					for(int i=0; i < targetDevices.length; i++) {
						if(targetDevices[i].toString().equals(selDev)) {
							openApk(targetDevices[i], installedPackage[i]);
							break;
						}
					}
				}
		    });
		    pack_btnSave = new JButton("Save");
		    pack_btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(pack_deviceList.getSelectedIndex() <= -1) return;
					String selDev = pack_deviceList.getSelectedValue();
					for(int i=0; i < targetDevices.length; i++) {
						if(targetDevices[i].toString().equals(selDev)) {
							saveApk(targetDevices[i], installedPackage[i]);
							break;
						}
					}
				}
		    });
		    pack_btnRemove = new JButton("Remove");
		    pack_btnRemove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(pack_deviceList.getSelectedIndex() <= -1) return;
					String selDev = pack_deviceList.getSelectedValue();
					for(int i=0; i < targetDevices.length; i++) {
						if(targetDevices[i].toString().equals(selDev)) {
							uninstallApk(targetDevices[i], installedPackage[i]);
							installedPackage[i] = null;
							pack_deviceList.remove(pack_deviceList.getSelectedIndex());
							if(pack_deviceList.getComponentCount() > 0) {
								pack_deviceList.setSelectedIndex(0);
							} else {
								pack_textPakcInfo.setText("");
								installedPackage = null;
								next();
							}
							break;
						}
					}
				}
		    });
		    
		    setmargin(mainpanel,5);
		    setmargin(packagepanel,5);
		    setmargin(Listpanel,5);
		    
		    
		    buttonpanel.add(pack_btnOpen, BorderLayout.WEST);
		    buttonpanel.add(pack_btnSave, BorderLayout.CENTER);
		    buttonpanel.add(pack_btnRemove, BorderLayout.EAST);
		    
		    appstartpanel.add(pack_comboStartActivity, BorderLayout.CENTER);
		    appstartpanel.add(pack_btnLaunch, BorderLayout.EAST);
		    
		    packagepanel.add(textViewscrollPane, BorderLayout.CENTER);
		    packagepanel.add(appstartpanel, BorderLayout.SOUTH);		    
		    
		    Listpanel.add(packagepanel, BorderLayout.CENTER);
		    Listpanel.add(buttonpanel, BorderLayout.SOUTH);
		    
		    //mainpanel.add(textSelectDevice,BorderLayout.NORTH);
		    mainpanel.add(Listpanel,BorderLayout.CENTER);
		    mainpanel.add(listscrollPane, BorderLayout.WEST);
		    
		    GridBagConstraints gbc = new GridBagConstraints();            
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            panel.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 1));
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(mainpanel,addGrid(gbc, 0, 1, 1, 1, 1, 7));
            panel.add(new JPanel(),addGrid(gbc, 0, 2, 1, 1, 1, 3));
            
            
			return panel;
		}
		
		void init_Panel_set_install_option() {
			panel_set_install_option.setLayout(new GridBagLayout());	
			GridBagConstraints gbc = new GridBagConstraints();      
			
			JPanel optionPanel = new JPanel(new GridLayout(1, 2));
			JPanel togglePanel = new JPanel(new GridBagLayout());
						
			Border Installborder = BorderFactory.createTitledBorder("Install");
			Border Pushborder = BorderFactory.createTitledBorder("Push");
			
			JPanel installPanel = new JPanel(new GridLayout(0,1));
			JPanel pushPanel = new JPanel(new GridLayout(0,1));
			
			
			JXTabbedPane tabbedPane = new JXTabbedPane(JTabbedPane.LEFT);
	        AbstractTabRenderer renderer = (AbstractTabRenderer)tabbedPane.getTabRenderer();
	        renderer.setPrototypeText("This text is a prototype");
	        renderer.setHorizontalTextAlignment(SwingConstants.LEADING);

	        tabbedPane.addTab("Install", null, installPanel, "Install");
	        tabbedPane.addTab("Push", null, pushPanel, "Push");
	        			
			installPanel.setBorder(Installborder);
			pushPanel.setBorder(Pushborder);
			
			JPanel CertPanel = new JPanel(new BorderLayout());
			
			JLabel textSelectDevice = new JLabel("set install option");
			textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
			
			JLabel textCertInfo = new JLabel("Cert Info");
			
			JTextArea CertInfo = new JTextArea();
			JScrollPane textViewscrollPane = new JScrollPane(CertInfo);
			
			JButton buttonchangeCert = new JButton("change Cert");
			
			
			JRadioButton Radiointernal = new JRadioButton("internal");
			JRadioButton Radioexternal = new JRadioButton("external");
			JCheckBox Checkrunafterinstall = new JCheckBox("run after installed");
			
			 JToggleButton InstalltoggleButton = new JToggleButton("Install");
		     
			
		    installPanel.add(Radiointernal);
			installPanel.add(Radioexternal);
			installPanel.add(Checkrunafterinstall);
				
			
			JRadioButton RadiosystemPush = new JRadioButton("system");
			JRadioButton RadioprivPush = new JRadioButton("priv-app");
			JRadioButton RadiodataPush = new JRadioButton("data");
			JCheckBox CheckOverwrite = new JCheckBox("overwrite lib");
			JCheckBox CheckWithLib = new JCheckBox("with Lib");
			JCheckBox CheckReboot = new JCheckBox("reboot after push");
			
			JToggleButton pushtoggleButton = new JToggleButton("Push");
			
			pushPanel.add(RadiosystemPush);
			pushPanel.add(RadioprivPush);
			pushPanel.add(RadiodataPush);
			pushPanel.add(CheckOverwrite);
			pushPanel.add(CheckWithLib);
			pushPanel.add(CheckReboot);
			
			//optionPanel.add(installPanel);
			//optionPanel.add(pushPanel);
			
			optionPanel.add(tabbedPane);
			
			InstalltoggleButton.setPreferredSize(new Dimension(100, 25));
			pushtoggleButton.setPreferredSize(new Dimension(100, 25));
			
            gbc.anchor = GridBagConstraints.EAST;            
            togglePanel.add(InstalltoggleButton,addGrid(gbc, 0, 0, 1, 1, 1, 1));
            gbc.anchor = GridBagConstraints.WEST;
            togglePanel.add(pushtoggleButton,addGrid(gbc, 1, 0, 1, 1, 1, 1));
            
			
			//togglePanel.add(InstalltoggleButton);
			//togglePanel.add(pushtoggleButton);
			
			JPanel certibuttonpanel = new JPanel(new BorderLayout());
			
			certibuttonpanel.add(buttonchangeCert, BorderLayout.EAST);
			
			CertPanel.add(textCertInfo, BorderLayout.NORTH);
			CertPanel.add(textViewscrollPane, BorderLayout.CENTER);
			CertPanel.add(certibuttonpanel, BorderLayout.SOUTH);
			
			      
			
			//panel_set_install_option.add(textSelectDevice, BorderLayout.NORTH);
			//panel_set_install_option.add(optionPanel, BorderLayout.CENTER);
			//panel_set_install_option.add(CertPanel, BorderLayout.SOUTH);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            
			panel_set_install_option.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 2));
						
			//panel_set_install_option.add(togglePanel, addGrid(gbc, 0, 1, 1, 1, 1, 1));
			
			gbc.fill = GridBagConstraints.BOTH;
			panel_set_install_option.add(optionPanel,addGrid(gbc, 0, 2, 1, 1, 1, 3));
			gbc.fill = GridBagConstraints.BOTH;
			panel_set_install_option.add(CertPanel,addGrid(gbc, 0, 3, 1, 1, 1, 5));
			
			panel_set_install_option.add(new JPanel(),addGrid(gbc, 0, 4, 1, 1, 1, 3));
		}
		
		public ContentPanel(ActionListener listener) {
			super(new CardLayout());
			
			JPanel lodingPanel = new JPanel();
			loadingMessageLable = new JLabel("");
			lodingPanel.setLayout(new BoxLayout(lodingPanel, BoxLayout.Y_AXIS));
			lodingPanel.add(new ImagePanel(Resource.IMG_APK_LOGO.getImageIcon()));
			lodingPanel.add(loadingMessageLable);
			lodingPanel.add(new ImagePanel(Resource.IMG_WAIT_BAR.getImageIcon()));
			
			panel_select_device = createSelectDevicePanel(listener);
			panel_check_package = createFindPackagePanel();
			panel_set_install_option = new JPanel();
			
			add(new JPanel(), CONTENT_INIT);
			add(lodingPanel, CONTENT_LOADING);
			add(new JPanel(), CONTENT_DEVICE_SCANNING);
			add(panel_select_device, CONTENT_SELECT_DEVICE);
			add(new JPanel(), CONTENT_PACKAGE_SCANNING);
			add(panel_check_package, CONTENT_CHECK_PACKAGES);
			add(panel_set_install_option, CONTENT_SET_INSTALL_OPTION);
			add(new JPanel(), CONTENT_INSTALLING);
			add(new JPanel(), CONTENT_COMPLETED);
			

			init_Panel_set_install_option();
			// set status
			setStatus(STATUS_INIT);
		}
		
		public void setStatus(int status) {
			switch(status) {
			case STATUS_INIT:
				loadingMessageLable.setText("INIT");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			case STATUS_WAIT_FOR_DEVICE:
				loadingMessageLable.setText("WAIT-FOR-DEVICE");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			case STATUS_DEVICE_SCANNING:
				loadingMessageLable.setText("SCANNING DEVICES");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			case STATUS_DEVICE_REFRESH:
			case STATUS_SELECT_DEVICE:
				if(!tableDatas.isEmpty()) {
					ArrayList<TableRowObject> removeList = new ArrayList<TableRowObject>(tableDatas);
					for(DeviceStatus dev: targetDevices) {
						boolean existed = false;
						for(TableRowObject row: tableDatas) {
							if(dev.name.equals(row.get(1))) {
								removeList.remove(row);
								existed = true;
								break;
							}
						}
						if(!existed) {
							tableDatas.add(new DeviceTableObject("device".equals(dev.status), dev));
						}
					}
					tableDatas.removeAll(removeList);
					removeList.clear();
				} else {
					for(DeviceStatus dev: targetDevices) {
						tableDatas.add(new DeviceTableObject("device".equals(dev.status), dev));
					}
				}

    			((AbstractTableModel) targetDeviceTable.getModel()).fireTableDataChanged();
				targetDeviceTable.updateUI();
				panel_select_device.repaint();
				
				updateSelectedAllButtonUI((SimpleCheckTableModel) targetDeviceTable.getModel());
				
				if(status == STATUS_DEVICE_REFRESH) {
					break;
				}

				((CardLayout)getLayout()).show(this, CONTENT_SELECT_DEVICE);
				break;
			case STATUS_PACKAGE_SCANNING:
				loadingMessageLable.setText("FIND PACKAGES");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			case STATUS_CHECK_PACKAGES:
				pack_textPakcInfo.setText("");
				DefaultListModel<String> listModel = (DefaultListModel<String>) pack_deviceList.getModel();
				listModel.clear();
				for(int i = 0; i < targetDevices.length; i++) {
					if(installedPackage != null && installedPackage[i] != null) {
						listModel.addElement(targetDevices[i].toString());
					}
				}
				if(listModel.size() > 0) {
					pack_deviceList.setSelectedIndex(0);
				}
				pack_deviceList.updateUI();
				
				DefaultComboBoxModel<String> comboModel = (DefaultComboBoxModel<String>) pack_comboStartActivity.getModel();
				comboModel.removeAllElements();
				if(apkInfo != null && (apkInfo.manifest.featureFlags & ManifestInfo.MANIFEST_FEATURE_LAUNCHUR) != 0) {
					for(String act: getLauncherActivityList()) {
						comboModel.addElement(act);
					}
					pack_comboStartActivity.setEnabled(comboModel.getSize() > 0);
				} else {
					pack_comboStartActivity.setEnabled(false);
				}

				((CardLayout)getLayout()).show(this, CONTENT_CHECK_PACKAGES);
				break;
			case STATUS_SET_INSTALL_OPTION:
				// set state of component
				
				if(apkInfo != null) {
					if((apkInfo.featureFlags & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
						// enable run app button
					} else {
						
					}
					if((apkInfo.manifest.installLocation.indexOf("internalOnly")) > -1) {
						// disable external
					} else {
						
					}
	
					if(apkInfo.certificates == null || apkInfo.certificates.length == 0) {
						// disable next
					} else {
						
					}
				}
				
				boolean isAllRootDevice = true;
				for(DeviceStatus dev: targetDevices) {
					// such dev.name in listview
					if(!AdbWrapper.root(dev.name, null)) {
						isAllRootDevice = false;
						break;
					}
				}
				if(isAllRootDevice) {
					// enable push group
					boolean haveSystemApp = false;
					if(installedPackage != null) {
						for(PackageInfo pack: installedPackage) {
							if(pack == null) continue;
							if(pack.isSystemApp) {
								haveSystemApp = true;
								break;
							}
						}
					}
					if(haveSystemApp) {
						// enable overwrite
					} else {
						// disable overwrite
					}
					
					if(apkInfo == null || apkInfo.libraries == null || apkInfo.libraries.length == 0) {
						// disable with libs
					} else {
						
					}
				} else {
					// disable push group
				}
				
				((CardLayout)getLayout()).show(this, CONTENT_SET_INSTALL_OPTION);
				break;
			case STATUS_INSTALLING:
				loadingMessageLable.setText("INSTALLING");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			case STATUS_COMPLETED:
				int successCount = 0;
				StringBuilder sb = new StringBuilder("-- Installation Reports ----------------\n");
				for(InstalledReport report: installReports) {
					sb.append(report);
					if(report.successed) {
						successCount++;
					}
				}
				sb.append("----------------------------------------");
				
				if(installReports.size() == successCount) {
					printLog("Installation succeeded.");
				} else if(installReports.size() == 1) {
					printLog("Installation failed.");
				} else {
					printLog(String.format("Installation succeeded %1$d of %2$d.", successCount, installReports.size()));
				}
				printLog(sb.toString());
				
				((CardLayout)getLayout()).show(this, CONTENT_COMPLETED);
				break;
			default:
				loadingMessageLable.setText("UNKNOWN STEP");
				((CardLayout)getLayout()).show(this, CONTENT_LOADING);
				break;
			}
		}
		
		private String getSelectedLauncherActivity() {
			return (String)pack_comboStartActivity.getSelectedItem();
		}
		
		private void appendLog(String msg) {
			// append to log viewer
			
		}

		public DeviceStatus[] getSelectedDevices() {
			DeviceStatus[] selectedDevices = null;
			//
			SimpleCheckTableModel model = (SimpleCheckTableModel)targetDeviceTable.getModel();
			if(model.getSelectedRowCount() > 0) {
				selectedDevices = new DeviceStatus[model.getSelectedRowCount()];
				int i = 0;
				for(TableRowObject rowObj : tableDatas) {
					if((boolean)rowObj.get(0)) {
						selectedDevices[i++] = ((DeviceTableObject)rowObj).devInfo;
					}
				}
			}
			
			return selectedDevices;
		}
	}
	
	private class ControlPanel extends JPanel
	{
		private static final long serialVersionUID = 5959656550868421305L;
		
		public static final String CTR_ACT_CMD_NEXT = "CTR_ACT_CMD_NEXT";
		public static final String CTR_ACT_CMD_PREVIOUS = "CTR_ACT_CMD_PREVIOUS";
		public static final String CTR_ACT_CMD_OK = "CTR_ACT_CMD_OK";
		public static final String CTR_ACT_CMD_CANCEL = "CTR_ACT_CMD_CANCEL";
		public static final String CTR_ACT_CMD_SHOW_LOG = "CTR_ACT_CMD_SHOW_LOG";
		public static final String CTR_ACT_CMD_RESTART = "CTR_ACT_CMD_RESTART";
		
		private JButton btnNext;
		private JButton btnPre;
		private JButton btnOk;
		private JButton btnCancel;
		private JButton btnShowLog;
		private JButton btnRestart;

		public ControlPanel(ActionListener listener) {
			super(new BorderLayout());
			
			btnNext = getButton("Next", CTR_ACT_CMD_NEXT, listener);
			btnPre = getButton("Previous", CTR_ACT_CMD_PREVIOUS, listener);
			btnOk = getButton("OK", CTR_ACT_CMD_OK, listener);
			btnCancel = getButton("Cancel", CTR_ACT_CMD_CANCEL, listener);
			btnShowLog = getButton("Show Log", CTR_ACT_CMD_SHOW_LOG, listener);
			btnRestart = getButton("Restart", CTR_ACT_CMD_SHOW_LOG, listener);
			
			JPanel stepPanel = new JPanel();
			stepPanel.add(btnCancel);
			stepPanel.add(btnPre);
			stepPanel.add(btnNext);
			stepPanel.add(btnRestart);
			stepPanel.add(btnOk);

			add(btnShowLog, BorderLayout.WEST);
			add(stepPanel, BorderLayout.EAST);
			
			JSeparator separator = new JSeparator();
			separator.setForeground(Color.LIGHT_GRAY);
			add(separator, BorderLayout.NORTH);
			
			// set status
			setStatus(STATUS_INIT);
		}
		
		private JButton getButton(String text, String actCmd, ActionListener listener) {
			JButton btn = new JButton(text);
			btn.setActionCommand(actCmd);
			btn.addActionListener(listener);
			return btn;
		}
		
		private void setVisibleButtons(boolean next, boolean pre, boolean ok, boolean cancel, boolean showlog, boolean restart) {
			btnNext.setVisible(next);
			btnPre.setVisible(pre);
			btnOk.setVisible(ok);
			btnCancel.setVisible(cancel);
			btnShowLog.setVisible(showlog);
			btnRestart.setVisible(restart);
		}
		
		public void setStatus(int status) {
			switch(status) {
			case STATUS_INIT:
				setVisibleButtons(true, false, false, false, false, false); break;
			case STATUS_DEVICE_SCANNING:
			case STATUS_WAIT_FOR_DEVICE:
				setVisibleButtons(false, false, false, true, false, false); break;
			case STATUS_DEVICE_REFRESH:
				setVisibleButtons(false, false, false, false, false, false); break;
			case STATUS_SELECT_DEVICE:
				setVisibleButtons(true, false, false, true, false, false); break;
			case STATUS_PACKAGE_SCANNING:
				setVisibleButtons(false, false, false, false, false, false); break;
			case STATUS_CHECK_PACKAGES:
				setVisibleButtons(true, true, false, true, false, false); break;
			case STATUS_SET_INSTALL_OPTION:
				setVisibleButtons(true, true, false, true, false, false); break;
			case STATUS_INSTALLING:
				setVisibleButtons(false, false, false, false, true, false); break;
			case STATUS_COMPLETED:
				setVisibleButtons(false, false, true, false, true, true); break;
			default:
				break;
			}
		}
	}

	public ApkInstallWizard() {
		wizard = new ApkInstallWizardFrame();
	}

	public ApkInstallWizard(JFrame owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}
	
	public ApkInstallWizard(JDialog owner) {
		if(owner != null)
			wizard = new ApkInstallWizardDialog(owner);
		else 
			wizard = new ApkInstallWizardFrame(owner);
	}
	
	private void setVisible(boolean visible) {
		if(wizard != null) wizard.setVisible(visible);
	}

	private void initialize(Window window)
	{
		if(window == null) return;

		window.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		window.setSize(new Dimension(700,550));
		
		progressPanel = new ProgressPanel();
		contentPanel = new ContentPanel(uiEventHandler);
		controlPanel = new ControlPanel(uiEventHandler);
		
		window.add(progressPanel, BorderLayout.NORTH);
		window.add(contentPanel, BorderLayout.CENTER);
		window.add(controlPanel, BorderLayout.SOUTH);
		
		//Log.i("initialize() register event handler");
		//window.addWindowListener(new UIEventHandler());
		
		// Shortcut key event processing
		KeyboardFocusManager ky=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ky.addKeyEventDispatcher(uiEventHandler);
	}
	
	private void changeState(int status) {
		Log.e(">>>>>>>>>>>>> changeState() " + status);
		if(this.status == status) return;
		this.status = status;
		progressPanel.setStatus(status);
		contentPanel.setStatus(status);
		controlPanel.setStatus(status);
		
		execute(status);
	}
	
	private void execute(int status) {
		switch(status) {
		case STATUS_DEVICE_SCANNING:
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						targetDevices = AdbDeviceManager.scanDevices();
						next();
					}
				}
			}).start();
			break;
		case STATUS_WAIT_FOR_DEVICE:
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						AdbWrapper.waitForDevice();
						next();
					}
				}
			}).start();
			break;
		case STATUS_PACKAGE_SCANNING:
			new Thread(new Runnable() {
				public void run()
				{
					synchronized(ApkInstallWizard.this) {
						if(apkInfo != null && targetDevices != null && targetDevices.length > 0) {
							boolean existed = false;
							installedPackage = new PackageInfo[targetDevices.length];
							for(int i = 0; i < targetDevices.length; i++) {
								installedPackage[i] = AdbPackageManager.getPackageInfo(targetDevices[i].name, apkInfo.manifest.packageName);
								if(installedPackage[i] != null) existed = true;
							}
							if(!existed) {
								installedPackage = null;
							}
						} else {
							installedPackage = null;
						}
						next();
					}
				}
			}).start();
			break;
		case STATUS_INSTALLING:
			installReports = new ArrayList<InstalledReport>(targetDevices.length);
			for(int i = 0; i < targetDevices.length; i++) {
				installApk(targetDevices[i], installedPackage != null ? installedPackage[i] : null);
			}
			break;
		default:
			break;
		}
	}
	
	public void start() {
		if(status != STATUS_INIT) {
			Log.w("No init state : " + status);
			return;
		}
		if(apkInfo == null || apkInfo.filePath == null || 
				!(new File(apkInfo.filePath).isFile())) {
			Log.e("No such apk file...");
		    JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
			return;
		}
		setVisible(true);
		changeState(STATUS_DEVICE_SCANNING);
	}
	
	private void next() {
		synchronized(this) {
			switch(status) {
			case STATUS_INIT:
			case STATUS_WAIT_FOR_DEVICE:
				changeState(STATUS_DEVICE_SCANNING);
				break;
			case STATUS_DEVICE_SCANNING:
				if(targetDevices == null || targetDevices.length == 0) {
					changeState(STATUS_WAIT_FOR_DEVICE);
					break;
				} else if(targetDevices == null || targetDevices.length > 1) {
					changeState(STATUS_SELECT_DEVICE);
					break;
				}
			case STATUS_SELECT_DEVICE:
				if(status == STATUS_SELECT_DEVICE) {
					targetDevices = contentPanel.getSelectedDevices();
				}
				if(targetDevices != null) {
					boolean isAllOnline = true;
					for(DeviceStatus dev: targetDevices) {
						// such dev.name in listview
						if(!"device".equals(dev.status)) {
							isAllOnline = false;
							break;
						}
					}
					if(targetDevices.length > 0 && isAllOnline) {
						changeState(STATUS_PACKAGE_SCANNING);
					} else if(status == STATUS_DEVICE_SCANNING) {
						changeState(STATUS_SELECT_DEVICE);
					} else {
						// show warring message, offline device selected...
					}
				}
				break;
			case STATUS_PACKAGE_SCANNING:
				if(installedPackage != null) {
					changeState(STATUS_CHECK_PACKAGES);
					break;
				}
			case STATUS_CHECK_PACKAGES:
				changeState(STATUS_SET_INSTALL_OPTION);
				break;
			case STATUS_SET_INSTALL_OPTION:
				//if(flag == 0) break;
				changeState(STATUS_INSTALLING);
				break;
			case STATUS_INSTALLING:
				changeState(STATUS_COMPLETED);
				break;
			default:
				break;
			}
		}
	}
	
	private void previous() {
		synchronized(this) {
			switch(status) {
			case STATUS_SET_INSTALL_OPTION:
				changeState(STATUS_CHECK_PACKAGES);
				if(installedPackage != null) {
					break;
				}
			case STATUS_CHECK_PACKAGES:
				changeState(STATUS_SELECT_DEVICE);
				break;
			default:
				break;
			}
		}
		
	}
	
	public void stop() {
		
	}
	
	private void restart() {
		if(status != STATUS_COMPLETED) return;
		status = STATUS_INIT;
		start();
	}
	
	public void setApk(ApkInfo apkInfo) {
		this.apkInfo = apkInfo;
		if(apkScanner != null) {
			apkScanner.clear(false);
			apkScanner = null;
		}
	}

	public void setApk(String apkFilePath) {
		if(apkFilePath == null || !(new File(apkFilePath).isFile())) {
			Log.e("No such apk file... : " + apkFilePath);
		    JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
			return;
		}

		if(apkScanner != null) {
			apkScanner.clear(false);
			apkScanner = null;
		}
		apkScanner = new AaptScanner(new ApkScannerStub.StatusListener() {
			@Override
			public void onStateChanged(Status status) {
				Log.i("OnStateChanged() "+ status);
				switch(status) {
				case BASIC_INFO_COMPLETED:
				case LIB_COMPLETED:
				case CERT_COMPLETED:
				case ACTIVITY_COMPLETED:
					break;
				default:
					break;
				}
			}

			@Override public void onSuccess() { }
			@Override public void onStart(long estimatedTime) { }
			@Override public void onProgress(int step, String msg) { }
			@Override public void onError(int error) { }
			@Override public void onCompleted() { }
		});
		apkScanner.openApk(apkFilePath);
		apkInfo = apkScanner.getApkInfo();
	}
	
	private String[] getLauncherActivityList() {
		ArrayList<String> launcherList = new ArrayList<String>();
		ArrayList<String> mainList = new ArrayList<String>(); 
		if(apkInfo != null &&
				apkInfo.manifest != null &&
				apkInfo.manifest.application != null) {
			if(apkInfo.manifest.application.activity != null) {
				for(ActivityInfo info: apkInfo.manifest.application.activity) {
					if((info.enabled == null || info.enabled) &&
							   (info.exported == null || info.exported) &&
							   (info.permission == null || info.permission.isEmpty()) &&
							   (info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info.name);
						else
							mainList.add(info.name);
					}
				}
			}
			if(apkInfo.manifest.application.activityAlias != null) {
				for(ActivityAliasInfo info: apkInfo.manifest.application.activityAlias) {
					if((info.enabled == null || info.enabled) &&
							   (info.exported == null || info.exported) &&
							   (info.permission == null || info.permission.isEmpty()) &&
							   (info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
						if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0)
							launcherList.add(info.name);
						else
							mainList.add(info.name);
					}
				}
			}
			launcherList.addAll(mainList);
		}
		return launcherList.toArray(new String[0]);
	}
	
	private void launchApp(final String device) {
		final String selectedActivity = contentPanel.getSelectedLauncherActivity();
		if(selectedActivity == null) {
			Log.w("No such launch activity");
			ArrowTraversalPane.showOptionDialog(null,
					Resource.STR_MSG_NO_SUCH_LAUNCHER.getString(),
					Resource.STR_LABEL_WARNING.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
			return;
		}

		new Thread(new Runnable() {
			public void run()
			{
				String launcherActivity = apkInfo.manifest.packageName + "/" + selectedActivity;
				String[] cmdResult = AdbWrapper.shell(device, new String[] {"am", "start", "-n", launcherActivity}, null);
				if(cmdResult == null || (cmdResult.length > 2 && cmdResult[1].startsWith("Error")) ||
						(cmdResult.length > 1 && cmdResult[0].startsWith("error"))) {
					Log.e("activity start faile : " + launcherActivity);
					//Log.e(String.join("\n", cmdResult));
					ArrowTraversalPane.showOptionDialog(null,
							Resource.STR_MSG_FAILURE_LAUNCH_APP.getString(),
							Resource.STR_LABEL_WARNING.getString(),
							JOptionPane.OK_OPTION, 
							JOptionPane.INFORMATION_MESSAGE,
							null,
							new String[] {Resource.STR_BTN_OK.getString()},
							Resource.STR_BTN_OK.getString());
				}
			}
		}).start();
	}
	
	public void openApk(DeviceStatus dev, PackageInfo pkgInfo) {
		Launcher.run(dev.name, pkgInfo.apkPath, null);
	}
	
	private void saveApk(DeviceStatus dev, PackageInfo pkgInfo) {
		String saveFileName;
		if(pkgInfo.apkPath.endsWith("base.apk")) {
			saveFileName = pkgInfo.apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
		} else {
			saveFileName = pkgInfo.apkPath.replaceAll(".*/", "");
		}

		final File destFile = ApkFileChooser.saveApkFile(wizard, saveFileName);
		if(destFile == null) return;

		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			@Override
			public void OnError(int cmdType, String device) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_FAILURE_PULL_APK.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				int n = ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_SUCCESS_PULL_APK.getString() + "\n" + destFile.getAbsolutePath(),
						Resource.STR_LABEL_QUESTION.getString(),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				switch(n) {
				case 0: // explorer
					SystemUtil.openFileExplorer(destFile);
					break;
				case 1: // open
					Launcher.run(destFile.getAbsolutePath());
					break;
				default:
					break;
				}
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { printLog(msg); }
		});		
		apkInstaller.pullApk(pkgInfo.apkPath, destFile.getAbsolutePath());
	}
	
	private void uninstallApk(final DeviceStatus dev, final PackageInfo pkgInfo) {
		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			@Override
			public void OnError(int cmdType, String device) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_FAILURE_UNINSTALLED.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				Log.i("Success APK uninstall or remove " + cmdType);
				if(cmdType == CMD_REMOVE) {
					final Object[] yesNoOptions = {Resource.STR_BTN_YES.getString(), Resource.STR_BTN_NO.getString()};
					int reboot = ArrowTraversalPane.showOptionDialog(null,
							Resource.STR_MSG_SUCCESS_REMOVED.getString() + "\n" + Resource.STR_QUESTION_REBOOT_DEVICE.getString(),
							Resource.STR_LABEL_INFO.getString(),
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE,
							null,
							yesNoOptions, yesNoOptions[1]);
					if(reboot == 0){							
						AdbWrapper.reboot(dev.name, null);
					}
				} else {
					ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_SUCCESS_REMOVED.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				}
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) { printLog(msg); }
		});

		if(pkgInfo.isSystemApp) {
			if(!AdbWrapper.root(dev.name, null)) {
				ArrowTraversalPane.showOptionDialog(null,
						Resource.STR_MSG_DEVICE_HAS_NOT_ROOT.getString(),
						Resource.STR_LABEL_ERROR.getString(),
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE,
						null,
						new String[] {Resource.STR_BTN_OK.getString()},
						Resource.STR_BTN_OK.getString());
				return;
			}
			apkInstaller.removeApk(pkgInfo.codePath);
		} else {
			apkInstaller.uninstallApk(pkgInfo.pkgName);
		}
	}
	
	private void installReport(final DeviceStatus dev, boolean sucess, String errorMsg) {
		installReports.add(new InstalledReport(dev, sucess, errorMsg));
		if(installReports.size() >= targetDevices.length) {
			Log.e("Installation completed");
			changeState(STATUS_COMPLETED);
		}
	}
	
	private void installApk(final DeviceStatus dev, final PackageInfo pkgInfo) {
		// install
		ApkInstaller apkInstaller = new ApkInstaller(dev.name, new ApkInstallerListener() {
			String ErrorMsg = "";
			@Override
			public void OnError(int cmdType, String device) {
				if(cmdType == CMD_REMOVE) {
					 return;
				}
				printLog("Error installed " + dev.name + "-" + dev.device + ", " + ErrorMsg);
				if(ErrorMsg.indexOf("INSTALL_FAILED_INSUFFICIENT_STORAGE") > -1) {
					// 
				} else {
					
				}
				installReport(dev, false, ErrorMsg);
			}

			@Override
			public void OnSuccess(int cmdType, String device) {
				if(cmdType == CMD_REMOVE) {
					return;
				}
				printLog("Succeed install into device(" + device + ")");
				if(cmdType == CMD_INSTALL) {
					if((flag & FLAG_OPT_EXTRA_RUN) == FLAG_OPT_EXTRA_RUN) {
						printLog("Launch app in device(" + device + ")");
						launchApp(dev.name);
					}
				} else if(cmdType == CMD_PUSH) {
					if((flag & FLAG_OPT_EXTRA_REBOOT) == FLAG_OPT_EXTRA_REBOOT) {
						printLog("reboot device(" + device + ")");
						AdbWrapper.reboot(dev.name, null);
					}
				}
				installReport(dev, true, null);
			}

			@Override public void OnCompleted(int cmdType, String device) { }
			@Override public void OnMessage(String msg) {
				String errmsg = msg.toUpperCase();
				if(errmsg.indexOf("ERROR") > -1 || 
						errmsg.indexOf("FAILURE") > -1 ||
						errmsg.indexOf("FAILED") > -1) {
					ErrorMsg = msg;
				}
				printLog(msg); 
			}
		});

		if((flag & FLAG_OPT_INSTALL) == FLAG_OPT_INSTALL) {
			printLog("Install APK ...");
			apkInstaller.installApk(apkInfo.filePath, (flag & FLAG_OPT_INSTALL_EXTERNAL) != 0);
		} else if((flag & FLAG_OPT_PUSH) == FLAG_OPT_PUSH) {
			printLog("Install APK by push ...");
			String destPath = null;
			if((flag & FLAG_OPT_PUSH_OVERWRITE) == FLAG_OPT_PUSH_OVERWRITE &&
					pkgInfo != null) {
				printLog("Overwrite APK ...");
				printLog("Existing path : " + pkgInfo.apkPath);
				destPath = pkgInfo.apkPath;
			} else {
				if(pkgInfo != null && pkgInfo.codePath != null) {
					printLog("Delete existing APK ...");
					printLog("code path : " + pkgInfo.codePath);
					apkInstaller.removeApk(pkgInfo.codePath);
				}
				if((flag & FLAG_OPT_PUSH_SYSTEM) == FLAG_OPT_PUSH_SYSTEM) {
					destPath = "/system/app/";
				} else if((flag & FLAG_OPT_PUSH_PRIVAPP) == FLAG_OPT_PUSH_PRIVAPP) {
					destPath = "/system/priv-app/";
				} else if((flag & FLAG_OPT_PUSH_DATA) == FLAG_OPT_PUSH_DATA) {
					destPath = "/data/app/";
				} else {
					destPath = "/system/app/";
				}
				destPath += apkInfo.manifest.packageName + "-1/base.apk";
				printLog("New APK Path : " + destPath);
			}
			
			String libPath = null;
			if((flag & FLAG_OPT_EXTRA_WITH_LIB) == FLAG_OPT_EXTRA_WITH_LIB &&
					(flag & FLAG_OPT_PUSH_DATA) != FLAG_OPT_PUSH_DATA &&
					apkInfo.libraries != null && apkInfo.libraries.length > 0) {
				printLog("With libraries ...");
				// unzip libs..
				if(ZipFileUtil.unZip(apkInfo.filePath, "lib/", apkInfo.tempWorkPath+File.separator+"lib")) {
					libPath = apkInfo.tempWorkPath + File.separator + "lib" + File.separator;
					printLog("Success unzip lib ... " + libPath);
				} else {
					printLog("Fail unzip lib ...");	
				}
			}
			apkInstaller.pushApk(apkInfo.filePath, destPath, libPath);
		}

	}
	
	private void printLog(String msg) {
		Log.v(msg);	
		// append to log viewer
		contentPanel.appendLog(msg);
	}
	
	private class InstalledReport {
		public DeviceStatus dev;
		public boolean successed;
		public String errMessage;
		
		public InstalledReport(DeviceStatus dev, boolean successed, String errMessage) {
			this.dev = dev;
			this.successed = successed;
			this.errMessage = errMessage;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(successed) {
				sb.append("SUCCESS: " + dev.device + "(" + dev.name + ")\n");
			} else {
				sb.append("FAILURE: " + dev.device + "(" + dev.name + ")\n");
				sb.append("\tERROR : " + errMessage + "\n");
			}
			return sb.toString();
		}
	}
	
	private class UIEventHandler implements ActionListener, KeyEventDispatcher, WindowListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(ControlPanel.CTR_ACT_CMD_NEXT.equals(arg0.getActionCommand())) {
				next();
			} else if(ControlPanel.CTR_ACT_CMD_PREVIOUS.equals(arg0.getActionCommand())) {
				previous();
			} else if(ControlPanel.CTR_ACT_CMD_CANCEL.equals(arg0.getActionCommand()) ||
					ControlPanel.CTR_ACT_CMD_OK.equals(arg0.getActionCommand())) {
				if(wizard instanceof JFrame &&
						((JFrame)wizard).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
					System.exit(0);
				} else {
					wizard.dispose();
				}
			} else if(ControlPanel.CTR_ACT_CMD_RESTART.equals(arg0.getActionCommand())) {
				restart();
			} else if(ContentPanel.CTT_ACT_CMD_REFRESH.equals(arg0.getActionCommand())) {

			} else if(ContentPanel.CTT_ACT_CMD_SELECT_ALL.equals(arg0.getActionCommand())) {

			} else if("SELECT_ALL".equals(arg0.getActionCommand())) {
				
			} else if("RUN".equals(arg0.getActionCommand())) {
				
			} else if("OPEN".equals(arg0.getActionCommand())) {
				
			} else if("SAVE".equals(arg0.getActionCommand())) {

			} else if("UNINSTALL".equals(arg0.getActionCommand())) {
				
			} else if("CHANG_SIGN".equals(arg0.getActionCommand())) {
				
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if(!wizard.isFocused()) return false;
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(e.getModifiers() == KeyEvent.ALT_MASK) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_N: 
						next(); 
						break;
					case KeyEvent.VK_P:	
						previous();
						break;
					default: 
						return false;
					}
					return true;
				} else if(e.getModifiers() == 0) {
					switch(e.getKeyCode()) {
					case KeyEvent.VK_F5 :
						contentPanel.refreshDeviceList();
						break;
					default:
						return false;
					}
					return true;
				}
			}
			return false;
		}
		
		// Closing event of window be delete tempFile
		@Override
		public void windowClosing(WindowEvent e)
		{
			if(apkScanner != null) {
				apkScanner.clear(false);
				apkScanner = null;
			}
		}
		
		@Override public void windowOpened(WindowEvent e) { }
		@Override public void windowClosed(WindowEvent e) { }
		@Override public void windowIconified(WindowEvent e) { }
		@Override public void windowDeiconified(WindowEvent e) { }
		@Override public void windowActivated(WindowEvent e) { }
		@Override public void windowDeactivated(WindowEvent e) { }
	};
	
    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
				ApkInstallWizard wizard = new ApkInstallWizard();
				if(SystemUtil.isWindows()) {
					wizard.setApk("C:\\Melon.apk");
				} else {  //for linux
					wizard.setApk("/home/leejinhyeong/Desktop/SecSettings2.apk");
				}
				wizard.start();
				//wizard.setVisible(true);
            }
        });
    }
}


