package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.resource.Resource;

public class EasyBordPanel extends JPanel implements ActionListener{
	EasyButton btnmini, btnexit;
	
    int pX, pY;
    JFrame frame;
    JPanel windowpanel;
    JLabel maintitle;
    
    static private Color bordercolor = new Color(230,230,230);
    static private Color bordertitlecolor = new Color(119,119,119);
    
    static private String CMD_WINDOW_EXIT = "window_exit";
    static private String CMD_WINDOW_MINI = "window_mini";
    
    public EasyBordPanel(JFrame mainframe) {
    	setLayout(new BorderLayout());
    	this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    	
    	this.frame = mainframe;
    	
    	windowpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	maintitle = new JLabel("APKScanner", SwingConstants.CENTER);
    	maintitle.setOpaque(false);
    	maintitle.setFont(new Font(getFont().getName(), Font.BOLD, 15));
    	maintitle.setForeground(bordertitlecolor);
    	windowpanel.setOpaque(false);
    	
    	((FlowLayout)windowpanel.getLayout()).setHgap(1);
        ImageIcon miniicon = new ImageIcon(Resource.IMG_EASY_WINDOW_MINI.getImageIcon(17,17).getImage());
        btnmini = new EasyButton(miniicon);
        btnmini.setActionCommand(CMD_WINDOW_MINI);
        
        ImageIcon exiticon = new ImageIcon(Resource.IMG_EASY_WINDOW_EXIT.getImageIcon(17,17).getImage());
        btnexit = new EasyButton(exiticon);
        btnexit.setActionCommand(CMD_WINDOW_EXIT);
        //stackLabel.setIcon(icon);
        
        btnmini.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnexit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        //stackLabel.setBorderPainted( false );
        //stackLabel.setContentAreaFilled( false )
        //stackLabel.setFocusPainted(false);
        //stackLabel.addActionListener(this);
        //stackLabel.setSelected(true);
        btnmini.setContentAreaFilled(false);
        btnexit.setContentAreaFilled(false);
        //stackLabel.setRolloverIcon(new ImageIcon(Resource.IMG_APK_FILE_ICON.getImageIcon(15,15).getImage()));
        setBackground(bordercolor);

        windowpanel.add(btnmini);
        windowpanel.add(btnexit);
        
        JLabel icon = new JLabel(Resource.IMG_APP_ICON.getImageIcon(17,17));
        
        add(windowpanel, BorderLayout.EAST);
        add(maintitle, BorderLayout.CENTER);
        //add(icon, BorderLayout.WEST);
        
        btnmini.addActionListener(this);
        btnexit.addActionListener(this);
        
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                // Get x,y and store them
                pX = me.getX();
                pY = me.getY();
            }
            public void mouseDragged(MouseEvent me) {
                frame.setLocation(frame.getLocation().x + me.getX() - pX,
                        frame.getLocation().y + me.getY() - pY);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                frame.setLocation(frame.getLocation().x + me.getX() - pX, 
                        frame.getLocation().y + me.getY() - pY);
            }
        });
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand() == CMD_WINDOW_EXIT) {
			System.exit(0);
		} else if(e.getActionCommand() == CMD_WINDOW_MINI) {
			frame.setState(JFrame.ICONIFIED);
		}
	}
	public void setWindowTitle(String str) {
		this.maintitle.setText(str);
	}

	public void clear() {
		// TODO Auto-generated method stub
		this.maintitle.setText("");
	}
}
