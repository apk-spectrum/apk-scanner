package com.apkscanner.gui.dialog.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

import com.apkscanner.gui.dialog.AboutDlg;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.gui.dialog.ApkInstallWizard.InstallDlgFuncListener;
import com.apkscanner.gui.util.ArrowTraversalPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.util.Log;

public class InstallDlg extends JDialog implements ActionListener,WindowListener
{
	private static final long serialVersionUID = -55952374181959464L;

	InstallCheckTable TestTable;
	JScrollPane scrollPane;
	JPanel framelayout;
	JFrame f;
	JTextArea taskOutput;
	private InstallDlgFuncListener CoreInstallLitener; 
	InstallDlg dlg;
	JPanel MessageBox;
	JPanel AboutPanel;
	Boolean isOnlyInstall;
	public enum CHECKLIST_MODE{
		ADD,
		WATING,
		QEUESTION,
		DONE,
		ERROR
	}
	
	public InstallDlgFuncListener getInstallDlgFuncListener() {
		return this.CoreInstallLitener;
	}
	public InstallDlg(Frame owner, Boolean isOnlyInstall) {
		super(owner);
		
		this.isOnlyInstall = isOnlyInstall;
		dlg = this;
		createAndShowGUI();		
		CoreInstallLitener = new InstallDlgFuncListener() {
			Runnable runThread;
			int QuestionResult;
			Object[] tempOption;
			DeviceStatus dev;
			DeviceListPanel panel;
			JOptionPane newOption;
			public int getValue(String str) {
				
				for(int i=0;i< tempOption.length; i++) {
					if(tempOption[i].toString().equals(str)) {
						return tempOption.length - 1 -i;
					}
				}
				return -1;
			}
			
			@Override
			public void AddLog(String str) {
				// TODO Auto-generated method stub
				taskOutput.append(str + "\n");
			}

			@Override
			public void AddCheckList(String name,String t, InstallDlg.CHECKLIST_MODE mode) {
				// TODO Auto-generated method stub
				Log.d("AddCheckList : " + name + " : " + t + " mode : " + mode);
				TestTable.addTableModel(name,t, mode);
			}

			@Override
			public void Complete(String str) {
				// TODO Auto-generated method stub
				Log.d("Thread End");
			}
			

			@Override
			public int ShowDeviceList(Runnable runnable) {
				this.runThread = runnable;
				Log.d("ShowDeviceList");
				MessageBox.removeAll();
				
				panel = new DeviceListPanel(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						if ("Set".equals(e.getActionCommand())) {
							
							dev = panel.getSelectedData();
							Log.i("click set" + dev.getSummary());
							
							SetResult(panel.getSelectedIndex());														
														
							MessageBox.removeAll();
							MessageBox.add(AboutPanel);
							dlg.repaint();
							dlg.pack();
							
						} else if("Refresh".equals(e.getActionCommand())) {
							Log.d("Refresh");
							panel.refreshData();
						}			            
					}
				}); 
				
				MessageBox.add(panel);
				dlg.repaint();
				dlg.pack();
				
				return 0;
			}
			
			@Override
			public int ShowQuestion(Runnable runthread, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
				// TODO Auto-generated method stub
				this.runThread = runthread;
				Log.d("ShowQuestion : " + runthread + " : "+ message + " : " + title + " : " + optionType + " : " + messageType + " : " + options.toString() + ": " + initialValue);
				tempOption = options;
				JButton[] btn = new JButton[options.length];
				
				for( int i=0; options.length > i; i++ ) {
					
					btn[i] = new JButton((String)options[i]);
					btn[i].addActionListener(new AlertButtonListener());

				}

				newOption = ArrowTraversalPane.makeOptionPane(message, title, optionType, messageType, icon, btn, null);
				newOption.setOpaque(false);
				MessageBox.removeAll();
				MessageBox.add(newOption);
				dlg.repaint();
				dlg.pack();
				return 0;
			}

			@Override
			public int getResult() {
				// TODO Auto-generated method stub
				//this.runThread.notify();
				return this.QuestionResult;
			}
			@Override
			public DeviceStatus getSelectDev() {
				return this.dev;
			}
			
			public void SetResult(int result) {
				this.QuestionResult = result;
				
				if(runThread!=null) {				
					synchronized (runThread) {
						this.runThread.notify();
					}
				}
			}			
		};		
		
	}

	class AlertButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
            JButton b = (JButton) e.getSource();
            Log.d("click : " + b.getText());
            
            CoreInstallLitener.SetResult(CoreInstallLitener.getValue(b.getText()));
            
            MessageBox.removeAll();
            MessageBox.add(AboutPanel);
            dlg.pack();
            dlg.repaint();
		}		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		Log.d(e.getActionCommand());
		
		if ("종료".equals(e.getActionCommand())) {
			this.setVisible(false);
			ApkInstallWizard.StopThead();
			
			if(isOnlyInstall) {
				System.exit(0);
			}
		} else if ("재설치".equals(e.getActionCommand())) {
			this.TestTable.clearTable();
			ApkInstallWizard.StopThead();
			ApkInstallWizard.RestartThread();
		}
	}
	
    private void createAndShowGUI() {
        //Create and set up the window.
        
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
    	
        TestTable = new InstallCheckTable();
        
        //TestTable.createAndShowGUI();
        
        this.setTitle(Resource.STR_APP_NAME.getString());
        this.setIconImage(Resource.IMG_TOOLBAR_INSTALL.getImageIcon().getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.setBounds(0, 0, 700, 400);
        this.setPreferredSize(new Dimension(500,450));
        
        this.setMinimumSize(new Dimension(500, 450));
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.addWindowListener(this);
        
        //this.setModal(f);
        
        
        //this.setModalityType(ModalityType.DOCUMENT_MODAL);
        
        //f.getContentPane().setLayout(new BorderLayout());
        //f.setLayout(new BorderLayout());
        JButton btnreInstall = new JButton("재설치");
        btnreInstall.addActionListener(this);
        
        JButton btnExit = new JButton("종료");
        btnExit.addActionListener(this);
        
        JPanel framelayout = new JPanel(new BorderLayout());
        JPanel parent = new JPanel(new BorderLayout());
        JPanel CheckListBox = new JPanel(new BorderLayout());
        JPanel EastPanel =  new JPanel(new BorderLayout());
        
        MessageBox = new JPanel(new BorderLayout());
        AboutPanel = AboutDlg.GetPanel();
        MessageBox.add(AboutPanel, BorderLayout.CENTER);
        
        JPanel ButtonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        
        JPanel LogBox= new JPanel(new BorderLayout());
        
        CheckListBox.setBackground(Color.WHITE);
        
		taskOutput = new JTextArea();
		DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		taskOutput.setWrapStyleWord(true);
		taskOutput.setLineWrap(true);
		taskOutput.setEditable(false);
		
		
		
		scrollPane = new JScrollPane(taskOutput);
		scrollPane.setPreferredSize(new Dimension(100,100));
		LogBox.add(scrollPane);
		
        CheckListBox.add(TestTable);
        //MessageBox.add(deviceListDig);
        
        
        EastPanel.add(LogBox, BorderLayout.SOUTH);
        EastPanel.add(MessageBox, BorderLayout.CENTER);
        
        parent.add(CheckListBox, BorderLayout.WEST);
        parent.add(EastPanel, BorderLayout.CENTER);
        
        
        ButtonBox.add(btnreInstall);
        ButtonBox.add(btnExit);
        //ButtonBox.add(LogBox, BorderLayout.SOUTH);
        
                
        framelayout.add(parent,BorderLayout.CENTER);
        framelayout.add(ButtonBox,BorderLayout.SOUTH);
        
        
        this.add(framelayout);
        this.setVisible(true);
        this.pack();
    }
	@Override
	public void windowOpened(WindowEvent e) {	}
	@Override
	public void windowClosing(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {
		this.setVisible(false);
		ApkInstallWizard.StopThead();
		if(isOnlyInstall) {
			System.exit(0);
		}
			
		}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}

}
