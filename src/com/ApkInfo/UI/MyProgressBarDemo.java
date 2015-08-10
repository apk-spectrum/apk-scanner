package com.ApkInfo.UI;


import java.awt.*;
import java.beans.*;
import javax.swing.*;

import com.ApkInfo.Resource.Resource;


public class MyProgressBarDemo extends JPanel
                             implements PropertyChangeListener {
	private static final long serialVersionUID = -4581766331575300150L;

	private JProgressBar progressBar;
    private JTextArea taskOutput;
    private Task task;
    private static int progress;
    private static String strAddText;
    private JLabel GifLabel;
    static JFrame frame;
    class Task extends SwingWorker<Void, Void> {
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
                //Make random progress.
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
            taskOutput.append("Done!\n");
        }
    }

    public MyProgressBarDemo() {
        super(new BorderLayout());

        //Create the demo's UI.
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        progressBar.setPreferredSize(new Dimension(500,35));
        
        taskOutput = new JTextArea(5, 50);
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        
        
        ImageIcon icon = Resource.IMG_LOADING.getImageIcon();
        //ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        
        //System.out.println("loding icon : " + icon);
        
        
        GifLabel = new JLabel(icon);
        
        
        panel.add(GifLabel);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        starttask();
    }

    public void init() {
    	this.setVisible(true);
    	progressBar.setValue(0);
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
            	taskOutput.append(strAddText);
            	strAddText = null;
            }
        } 
    }

    public void addProgress(int addValue, String addtext) {
    	progress += addValue;
    	
    	strAddText = addtext;
    	if(addValue == 0 && taskOutput != null) {
    		taskOutput.append(strAddText);
    		taskOutput.setCaretPosition(taskOutput.getText().length());
    	}
    }
    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    static JFrame createAndShowGUI(MyProgressBarDemo temp) {
        //Create and set up the window.
        frame = new JFrame("APK Scanner " + Resource.STR_APP_VERSION.getString());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = temp;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        
		ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
        frame.setIconImage(Appicon.getImage());
        
        //Display the window.
        frame.setResizable( false );
        
        
        frame.pack();
        //frame.setLocation(200, 200);        
        //frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        
        return frame;
    }
}
