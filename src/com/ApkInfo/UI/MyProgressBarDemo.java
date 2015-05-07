package com.ApkInfo.UI;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.beans.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MyProgressBarDemo extends JPanel
                             implements PropertyChangeListener {

    private JProgressBar progressBar;
    private JTextArea taskOutput;
    private Task task;
    private static int progress;
    private static String strAddText;
    private JLabel GifLabel;
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

        taskOutput = new JTextArea(5, 50);
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        ImageIcon icon = new ImageIcon("res/loading.gif");        
        GifLabel = new JLabel(icon);
        
        
        panel.add(GifLabel);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
            taskOutput.append(strAddText);
        } 
    }

    public void addProgress(int addValue, String addtext) {
    	progress +=addValue;
    	
    	strAddText = addtext;
    	
    }
    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    static JFrame createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressBarDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MyProgressBarDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.setResizable( false );
        frame.pack();
        frame.setLocation(200, 200);
        
        frame.setVisible(true);
        return frame;
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
