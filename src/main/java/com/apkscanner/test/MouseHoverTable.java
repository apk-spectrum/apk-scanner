package com.apkscanner.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MouseHoverTable extends JFrame {
    private static final long serialVersionUID = -9221804811287169191L;

    int itsRow = 0;
    int itsColumn = 0;
    JTable itsTable;

    MouseHoverTable(String framename) {
        super(framename);

        itsTable = new JTable(5, 3);
        // make the table transparent
        itsTable.setOpaque(false);
        itsTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer());
        MyMouseAdapter aMouseAda = new MyMouseAdapter();
        itsTable.addMouseMotionListener(aMouseAda);
        @SuppressWarnings("unused")
        JScrollPane jsp = new JScrollPane(itsTable);
        this.getContentPane().add(itsTable);// jsp);
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        JFrame frame = new MouseHoverTable("Table Example");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Window w = e.getWindow();
                w.setVisible(false);
                w.dispose();
                System.exit(0);
            }
        });
        frame.pack();
        frame.show();
    }

    public class MyMouseAdapter extends MouseMotionAdapter // extends
    {
        public void mouseMoved(MouseEvent e) {
            JTable aTable = (JTable) e.getSource();
            itsRow = aTable.rowAtPoint(e.getPoint());
            itsColumn = aTable.columnAtPoint(e.getPoint());
            aTable.repaint();
        }
    }

    public class AttributiveCellRenderer extends JLabel implements TableCellRenderer {

        private static final long serialVersionUID = 4704190022977926777L;

        public AttributiveCellRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (row == itsRow && column == itsColumn) {
                this.setBackground(Color.red);
                this.setForeground(Color.blue);
            } else {
                this.setBackground(Color.cyan);
                this.setForeground(Color.darkGray);
            }
            String aStr = "Row " + row + "Column" + column;
            this.setText(aStr);
            return this;
        }
    }
}
