package com.apkscanner.gui.dialog.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.resource.Resource;
import com.apkscanner.test.MainPanel;
import com.apkscanner.test.ProgressBarTest;

public class InstallDlg extends JDialog implements ActionListener{
	
	static InstallCheckTable TestTable;
	
	public InstallDlg() {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

    private static void createAndShowGUI() {
        //Create and set up the window.
        
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
    	
        JFrame f = new JFrame();
        
        TestTable = new InstallCheckTable();
        TestTable.createAndShowGUI();
        
        f.setTitle(Resource.STR_APP_NAME.getString());
		f.setIconImage(Resource.IMG_TOOLBAR_INSTALL.getImageIcon().getImage());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setBounds(0, 0, 700, 400);
		f.setMinimumSize(new Dimension(700, 400));
		f.setLocationRelativeTo(null);
		//f.setResizable(false);
        f.pack();
        f.setVisible(true);
        //f.getContentPane().setLayout(new BorderLayout());
        //f.setLayout(new BorderLayout());
        JButton btnExit = new JButton("btnExit");
        JButton btnLogBox = new JButton("LogBox");
        JButton btnshowLogBox = new JButton("showLogBox");
        
        
        JPanel framelayout = new JPanel(new BorderLayout());
        JPanel parent = new JPanel(new GridLayout(1,2));
        JPanel CheckListBox = new JPanel(new BorderLayout());
        JPanel MessageBox = new JPanel(new BorderLayout());
        JPanel ButtonBox = new JPanel(new BorderLayout());
        JPanel LogBox= new JPanel(new BorderLayout());
        
        CheckListBox.setBackground(Color.darkGray);
        MessageBox.setBackground(Color.lightGray);
        ButtonBox.setBackground(Color.PINK);
        
        CheckListBox.add(TestTable);
        
        parent.add(CheckListBox, BorderLayout.WEST);
        parent.add(MessageBox, BorderLayout.EAST);
        
        LogBox.add(btnLogBox);
        
        ButtonBox.add(btnExit,BorderLayout.EAST );
        ButtonBox.add(LogBox, BorderLayout.SOUTH);
        ButtonBox.add(btnshowLogBox, BorderLayout.WEST);
        
        
        framelayout.add(parent,BorderLayout.CENTER);
        framelayout.add(ButtonBox,BorderLayout.SOUTH);
        
        f.add(framelayout);
        
    }
	
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
