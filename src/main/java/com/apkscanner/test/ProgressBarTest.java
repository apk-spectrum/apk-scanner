package com.apkscanner.test;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * @see http://stackoverflow.com/a/10491290/230513
 */
public class ProgressBarTest extends JPanel {

    private static final long serialVersionUID = 8727572613016196274L;
    private JTable table = new JTable(new TestModel());

    public ProgressBarTest() {
        table.getColumnModel().getColumn(0).setCellRenderer(new TestCellRenderer());
        table.setPreferredScrollableViewportSize(new Dimension(320, 120));
        this.add(new JScrollPane(table));
    }

    private class TestModel extends AbstractTableModel {

        private static final long serialVersionUID = 6474883623897291715L;

        @Override
        public int getRowCount() {
            return 4;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return String.valueOf(row) + ", " + String.valueOf(col);
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            // update internal model and notify listeners
            fireTableCellUpdated(row, col);
        }
    }

    private class TestCellRenderer implements TableCellRenderer, ActionListener {

        JProgressBar bar = new JProgressBar();
        Timer timer = new Timer(100, this);

        public TestCellRenderer() {
            bar.setIndeterminate(true);
            timer.start();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return bar;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TableModel model = table.getModel();
            for (int row = 0; row < model.getRowCount(); row++) {
                table.getModel().setValueAt(0, row, 0);
            }
        }
    }

    private void display() {
        JFrame f = new JFrame("TestProgressBar");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(this);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new ProgressBarTest().display();
            }
        });
    }
}
