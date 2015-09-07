package com.apkscanner.gui;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.beans.*;
import javax.swing.*;

import com.apkscanner.core.Log;
import com.apkscanner.resource.Resource;


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

    public ProgressBarDlg(WindowListener windowListener)
    {
    	setTitle("APK Scanner " + Resource.STR_APP_VERSION.getString());
    	setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    	setResizable( false );

        JPanel panel = new JPanel();
        
        JLabel loadingLabel = new JLabel(Resource.IMG_LOADING.getImageIcon());
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
        
        addWindowListener(windowListener);
        starttask();
        
    	KeyStroke vk_f12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(vk_f12, "VK_F12");
		getRootPane().getActionMap().put("VK_F12", new AbstractAction() {
			private static final long serialVersionUID = -5281980076592985530L;
			public void actionPerformed(ActionEvent e) {
				JTextArea taskOutput = new JTextArea();
				taskOutput.setText(Log.getLog());
				taskOutput.setEditable(false);
				taskOutput.setCaretPosition(0);
				
				JScrollPane scrollPane = new JScrollPane(taskOutput);
				scrollPane.setPreferredSize(new Dimension(600, 400));

				JOptionPane.showOptionDialog(null, scrollPane, Resource.STR_LABEL_LOG.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null,
			    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
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
