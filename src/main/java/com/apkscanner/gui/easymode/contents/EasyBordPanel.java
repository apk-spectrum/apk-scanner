package com.apkscanner.gui.easymode.contents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apkscanner.gui.easymode.util.CustomSlider;
import com.apkscanner.gui.easymode.util.EasyButton;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;
import com.apkspectrum.logback.Log;

public class EasyBordPanel extends JPanel implements ActionListener, ChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = -2246253920944782832L;

    EasyButton btnmini, btnexit;

    int pX, pY;
    JFrame frame;
    JPanel windowpanel;
    JLabel maintitle;
    CustomSlider alphaslider;
    float opacity;

    static private Color bordercolor = new Color(230, 230, 230);
    static private Color bordertitlecolor = new Color(119, 119, 119);

    static private String CMD_WINDOW_EXIT = "window_exit";
    static private String CMD_WINDOW_MINI = "window_mini";

    public EasyBordPanel(JFrame mainframe) {
        Log.d("start EasyBordPanel ");
        setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

        this.frame = mainframe;

        windowpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        maintitle = new JLabel(RStr.APP_NAME.get(), SwingConstants.CENTER);
        maintitle.setOpaque(false);
        maintitle.setFont(new Font(getFont().getName(), Font.BOLD, 15));
        maintitle.setForeground(bordertitlecolor);
        windowpanel.setOpaque(false);

        alphaslider = new CustomSlider();
        alphaslider.addChangeListener(this);
        opacity = 1.0f;
        alphaslider.setValue(100);
        // Turn on labels at major tick marks.

        ((FlowLayout) windowpanel.getLayout()).setHgap(1);
        ImageIcon miniicon = new ImageIcon(RImg.EASY_WINDOW_MINI.getImage(17, 17));
        btnmini = new EasyButton(miniicon);
        btnmini.setActionCommand(CMD_WINDOW_MINI);

        ImageIcon exiticon = new ImageIcon(RImg.EASY_WINDOW_EXIT.getImage(17, 17));
        btnexit = new EasyButton(exiticon);
        btnexit.setActionCommand(CMD_WINDOW_EXIT);
        // stackLabel.setIcon(icon);

        btnmini.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnexit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        // stackLabel.setBorderPainted( false );
        // stackLabel.setContentAreaFilled( false )
        // stackLabel.setFocusPainted(false);
        // stackLabel.addActionListener(this);
        // stackLabel.setSelected(true);
        btnmini.setContentAreaFilled(false);
        btnexit.setContentAreaFilled(false);
        // stackLabel.setRolloverIcon(new
        // ImageIcon(Resource.IMG_APK_FILE_ICON.getImageIcon(15,15).getImage()));
        setBackground(bordercolor);

        windowpanel.add(alphaslider);
        windowpanel.add(btnmini);
        windowpanel.add(btnexit);

        add(windowpanel, BorderLayout.EAST);
        add(maintitle, BorderLayout.CENTER);
        // add(icon, BorderLayout.WEST);

        btnmini.addActionListener(this);
        btnexit.addActionListener(this);

//        addMouseListener(new MouseAdapter() {
//            public void mousePressed(MouseEvent me) {
//                // Get x,y and store them
//            	pX = me.getX();
//                pY = me.getY();
//                
//                Log.d("pX : " + pX);
//            }
//            public void mouseDragged(MouseEvent me) {
//              //  frame.setLocation(frame.getLocation().x + me.getX() - pX,
//              //          frame.getLocation().y + me.getY() - pY);
//            }
//        });
//        addMouseMotionListener(new MouseAdapter() {
//            public void mouseDragged(MouseEvent me) {
//            	
//            	
//            	
//                frame.setLocation(frame.getLocation().x + me.getX() - pX, 
//                        frame.getLocation().y + me.getY() - pY);
//                Log.d("frame.getLocation().x : " + frame.getLocation().x + "    me.getX() - pX : " + (me.getX() - pX) + 
//                		"    me.getX()  : " + (me.getX()) + "    pX : " + pX);
//            }
//        });

        MouseAdapter ma = new MouseAdapter() {
            int lastX, lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getXOnScreen();
                lastY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                // Log.d("x =" + x + " aaaaa : " + (frame.getLocation().x + x - lastX));
                // Move frame by the mouse delta
                frame.setLocation(frame.getLocation().x + x - lastX,
                        frame.getLocation().y + y - lastY);

                // frame.setLocation(1500,500);

                lastX = x;
                lastY = y;
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        Log.d("End EasyBordPanel ");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == CMD_WINDOW_EXIT) {

            // System.exit(0);
            frame.dispose();
        } else if (e.getActionCommand() == CMD_WINDOW_MINI) {
            frame.setState(JFrame.ICONIFIED);
        }
    }

    public void setWindowTitle(String str) {
        this.maintitle.setText(str);
    }

    public void clear() {
        this.maintitle.setText("");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        int fps = (int) source.getValue();
        opacity = (0.1f + ((float) fps / (float) 115));
        try {
            frame.setOpacity(opacity);
        } catch (java.awt.IllegalComponentStateException ea) {
            // ea.printStackTrace();
        }
    }
}
