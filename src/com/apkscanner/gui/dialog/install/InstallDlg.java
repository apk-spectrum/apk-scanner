package com.apkscanner.gui.dialog.install;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import com.apkscanner.resource.Resource;

public class InstallDlg extends JDialog implements ActionListener{
	
	
	public InstallDlg() {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

    private static void createAndShowGUI() {
        //Create and set up the window.
        
        JFrame f = new JFrame();

		f.setTitle(Resource.STR_APP_NAME.getString());
		f.setIconImage(Resource.IMG_TOOLBAR_INSTALL.getImageIcon().getImage());
        
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setBounds(0, 0, 700, 500);
		f.setMinimumSize(new Dimension(650, 500));
		f.setLocationRelativeTo(null);
		f.setResizable(false);
        f.pack();
        f.setVisible(true);
     
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
