package com.apkscanner.gui.dialog.install;

//-*- mode:java; encoding:utf-8 -*-
//vim:set fileencoding=utf-8:
//http://ateraimemo.com/Swing/StringPaintedCellProgressBar.html
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.ImageIcon;

import com.apkscanner.resource.Resource;

public final class InstallCheckTable extends JPanel {
  private final WorkerModel model = new WorkerModel();
  private final JTable table = new JTable(model);
  private final transient TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
  public InstallCheckTable() {
      super(new BorderLayout());
      table.setRowSorter(sorter);

      JScrollPane scrollPane = new JScrollPane(table);
      scrollPane.getViewport().setBackground(Color.WHITE);
      table.setFillsViewportHeight(true);
      table.setIntercellSpacing(new Dimension());
      table.setShowGrid(false);
      table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            
      //column.setResizable(false);
      //column.setCellRenderer(new ProgressRenderer());

      table.setRowHeight(50);
      
      add(new JButton(new ProgressValueCreateAction("add")), BorderLayout.SOUTH);
      add(scrollPane);
      setPreferredSize(new Dimension(320, 240));
      
      setJTableColumnsWidth(table,320,5,40,20,40);
  }
  	public void addTableModel(String name, String t) {
  		model.addValue(name, t);
  	}
	private void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
			double... percentages) {
			double total = 0;
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
				total += percentages[i];
			}			
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
				TableColumn column = table.getColumnModel().getColumn(i);
				column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
			}
}

  class ProgressValueCreateAction extends AbstractAction {
      public ProgressValueCreateAction(String label) {
          super(label);
      }
      @Override public void actionPerformed(ActionEvent e) {

          model.addValue("aaaa", "bbbb");
          //executor.execute(worker);
          setImageObserver(table);
      }
  }

  public static void main(String... args) {
      EventQueue.invokeLater(new Runnable() {
          @Override public void run() {
              createAndShowGUI();
          }
      });
  }
  public static void createAndShowGUI() {
      try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (ClassNotFoundException | InstantiationException
             | IllegalAccessException | UnsupportedLookAndFeelException ex) {
          ex.printStackTrace();
      }
      JFrame frame = new JFrame("StringPaintedCellProgressBar");
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.getContentPane().add(new InstallCheckTable());
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
  }
  
  private void setImageObserver(JTable table) {
	  TableModel model = table.getModel();
	  int colCount = model.getColumnCount();
	  int rowCount = model.getRowCount();
	  for (int col = 0; col < colCount; col++) {
	      if (ImageIcon.class == model.getColumnClass(col)) {
	      for (int row = 0; row < rowCount; row++) {
	          ImageIcon icon = (ImageIcon) model.getValueAt(row, col);
	          if (icon != null) {
	          icon.setImageObserver(new CellImageObserver(table, row,
	              col));
	          }
	      }
	      }
	  }
	  }

	  class CellImageObserver implements ImageObserver {
	  JTable table;
	  int row;
	  int col;

	  CellImageObserver(JTable table, int row, int col) {
	      this.table = table;
	      this.row = row;
	      this.col = col;
	  }

	  public boolean imageUpdate(Image img, int flags, int x, int y, int w,
	      int h) {
	      if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
	      Rectangle rect = table.getCellRect(row, col, false);
	      table.repaint(rect);
	      }
	      return (flags & (ALLBITS | ABORT)) == 0;
	  }

	  }
}

class WorkerModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("No.",      Integer.class, false),
      new ColumnContext("Name", String.class, false),
      new ColumnContext("Progress",     ImageIcon.class,  false),
      new ColumnContext("Result", String.class, false)
  };
  private final ConcurrentMap<Integer, SwingWorker> swmap = new ConcurrentHashMap<>();
  private int number;
  public void addValue(String name, String t) {
      Object[] obj = {number, name, new ImageIcon(Resource.IMG_INSTALL_TABLE_DONE.getImageIcon().getImage()), t};
      super.addRow(obj);
      number++;
  }

  @Override public boolean isCellEditable(int row, int col) {
      return COLUMN_ARRAY[col].isEditable;
  }
  @Override public Class<?> getColumnClass(int modelIndex) {
      return COLUMN_ARRAY[modelIndex].columnClass;
  }
  @Override public int getColumnCount() {
      return COLUMN_ARRAY.length;
  }
  @Override public String getColumnName(int modelIndex) {
      return COLUMN_ARRAY[modelIndex].columnName;
  }
  private static class ColumnContext {
      public final String  columnName;
      public final Class   columnClass;
      public final boolean isEditable;
      public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
          this.columnName = columnName;
          this.columnClass = columnClass;
          this.isEditable = isEditable;
      }
  }
}