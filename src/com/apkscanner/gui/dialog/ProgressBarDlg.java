package com.apkscanner.gui.dialog;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;


public class ProgressBarDlg extends JFrame
                             implements PropertyChangeListener {
	private static final long serialVersionUID = -4581766331575300150L;

	private JProgressBar progressBar;
    private JTextArea logOutput;
    private Task task;
    private static int progress;
    private static String strAddText;
    
    class Task extends SwingWorker<Void, Void>
    {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {            
            progress = 0;
            //Initialize progress property.
            setProgress(0);
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {}

                setProgress(Math.min(progress, 100));
            }
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();            
            setCursor(null); //turn off the wait cursor
            logOutput.append("Done!\n");
        }
    }

    public ProgressBarDlg(Component component, WindowListener windowListener)
    {
    	setTitle("APK Scanner " + RStr.APP_VERSION.getString());
    	setIconImage(RImg.APP_ICON.getImage());
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

        JPanel panel = new JPanel();
        
        JLabel loadingLabel = new JLabel(RImg.LOADING.getImageIcon());
        panel.add(loadingLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(500,35));
        panel.add(progressBar);

        logOutput = new JTextArea(5, 50);
        logOutput.setEditable(false);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panel, BorderLayout.PAGE_START);
        contentPanel.add(new JScrollPane(logOutput), BorderLayout.CENTER);
        contentPanel.setOpaque(true);
        
        setContentPane(contentPanel);
        pack();
        
        setLocationRelativeTo(component);
    	setResizable( false );
        
        addWindowListener(windowListener);
        starttask();
        
		KeyStrokeAction.registerKeyStrokeActions(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false)
			}, new AbstractAction() {
				private static final long serialVersionUID = -5281980076592985530L;
				public void actionPerformed(ActionEvent e) {
					LogDlg.showLogDialog(ProgressBarDlg.this);
			}
		});
    }

    public void init() {
    	progressBar.setValue(0);
    	logOutput.setText("");
    	starttask();
    }
    
    /**
     * Invoked when the user presses the start button.
     */
    public void starttask() {        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            
            progressBar.setValue(progress);
            if(strAddText != null) {
            	logOutput.append(strAddText);
            	strAddText = null;
            }
        } 
    }

    public void addProgress(int addValue, String addtext) {
    	progress += addValue;
    	
    	strAddText = addtext;
    	if(addValue == 0 && logOutput != null) {
    		logOutput.append(strAddText);
    		logOutput.setCaretPosition(logOutput.getText().length());
    	}
    }

}
