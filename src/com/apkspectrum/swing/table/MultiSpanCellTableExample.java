package com.apkspectrum.swing.table;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkspectrum.resource._RProp;


/**
 * @version 1.0 11/26/98
 */
public class MultiSpanCellTableExample extends JFrame {
	private static final long serialVersionUID = -6881051761931159111L;

	MultiSpanCellTableExample() {
		super( "Multi-Span Cell Example" );

		try {
			UIManager.setLookAndFeel(_RProp.S.CURRENT_THEME.get());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		final AttributiveCellTableModel ml = new AttributiveCellTableModel(5,5);
		final MultiSpanCellTable table = new MultiSpanCellTable( ml );
		JScrollPane scroll = new JScrollPane( table );

		final CellSpan cellAtt = ml.getCellSpan();

		final JButton b_one   = new JButton("Combine");
		b_one.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] columns = table.getSelectedColumns();
				int[] rows    = table.getSelectedRows();
				cellAtt.combine(rows, columns);

				table.setRowSelectionInterval(rows[0], rows[0]);
				table.setColumnSelectionInterval(columns[0], columns[0]);
			}
		});
		final JButton b_split = new JButton("Split");
		b_split.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] anchor = cellAtt.getAnchorPoint(table.getSelectedRow(), table.getSelectedColumn());
				int row = anchor[CellSpan.ROW];
				int column = anchor[CellSpan.COLUMN];
				int[] span = cellAtt.getSpan(row, column);

				cellAtt.split(row, column);

				table.setRowSelectionInterval(row, row + span[CellSpan.ROW] - 1);
				table.setColumnSelectionInterval(column, column + span[CellSpan.COLUMN] - 1);
			}
		});
		final JButton b_add_row = new JButton("Add Row");
		b_add_row.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.addRow(new Object[] {"add", "to", ml.getRowCount()});
			}
		});
		final JButton b_add_column = new JButton("Add Column");
		b_add_column.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.addColumn(null);
			}
		});
		final JButton b_insert_row = new JButton("Insert Row");
		b_insert_row.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row    = table.getSelectedRow();
				if(row < 0) return;
				ml.insertRow(row, new Object[] {"insert", "to", Integer.toString(row)});

				int[] rows = table.getSelectedRows();
				int[] cols = table.getSelectedColumns();
				if(rows.length > 1 && cols.length > 0
						&& rows[0] <= row && row <= rows[rows.length-1]) {
					table.setRowSelectionInterval(rows[1], rows[rows.length-1]);
					table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
				}
			}
		});
		final JButton b_remove_row = new JButton("Remove Row");
		b_remove_row.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table != null ? table.getSelectedRows() : null;
				int[] cols = table != null ? table.getSelectedColumns() : null;

				if(rows.length <= 0) return;
				ml.removeRow(rows[0]);

				int size = ml.getRowCount();
		        if(size > 0) {
					if(rows[0] >= size) {
						rows = new int[] { size - 1 };
					} else if(rows[rows.length-1] >= size) {
						rows[rows.length-1] = size - 1;
					}
					table.setRowSelectionInterval(rows[0], rows[0] >= rows[rows.length-1] ? rows[0] : rows[rows.length-1] - 1);
					table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
				}
			}
		});
		final JButton b_increase_row_5 = new JButton("Increase Rows +5");
		b_increase_row_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.setRowCount(ml.getRowCount() + 5);
			}
		});
		final JButton b_decrease_row_5 = new JButton("Decrease Rows -5");
		b_decrease_row_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.setRowCount(ml.getRowCount() - 5);
			}
		});
		final JButton b_increase_col_5 = new JButton("Increase Columns +5");
		b_increase_col_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.setColumnCount(ml.getColumnCount() + 5);
			}
		});
		final JButton b_decrease_col_5 = new JButton("Decrease Columns -5");
		b_decrease_col_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.setColumnCount(ml.getColumnCount() - 5);
			}
		});
		final JButton b_move_to_up = new JButton("Move rows to up");
		b_move_to_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length <= 0 && rows[0] <= 0) return;
				ml.moveRow(rows[0], rows[rows.length-1], rows[0] - 1);

				if(table != null) {
					int[] cols = table.getSelectedColumns();
					if(rows.length > 0 && cols.length > 0) {
						table.setRowSelectionInterval(rows[0] - 1, (rows[0] - 1) + (rows.length-1));
						table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
					}
					table.repaint();
				}
			}
		});
		final JButton b_move_to_down = new JButton("Move rows to down");
		b_move_to_down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length <= 0 && rows[rows.length-1] >= rows.length) return;
				ml.moveRow(rows[0], rows[rows.length-1], rows[0] + 1);

				if(table != null) {
					int[] cols = table.getSelectedColumns();
					if(rows.length > 0 && cols.length > 0) {
						table.setRowSelectionInterval(rows[0] + 1, (rows[0] + 1) + (rows.length-1));
						table.setColumnSelectionInterval(cols[0], cols[cols.length-1]);
					}
					table.repaint();
				}
			}
		});
		final JButton b_move_to_first = new JButton("Move rows to first");
		b_move_to_first.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length <= 0 && rows[0] <= 0) return;
				ml.moveRow(rows[0], rows[rows.length-1], 0);
			}
		});
		final JButton b_move_to_last = new JButton("Move rows to last");
		b_move_to_last.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = table.getSelectedRows();
				if(rows.length <= 0 && rows[0] <= 0) return;
				ml.moveRow(rows[0], rows[rows.length-1], ml.getRowCount() - rows.length);
			}
		});

		b_one.setEnabled(false);
		b_split.setEnabled(false);
		b_insert_row.setEnabled(false);
		b_remove_row.setEnabled(false);
		b_move_to_up.setEnabled(false);
		b_move_to_down.setEnabled(false);
		b_move_to_first.setEnabled(false);
		b_move_to_last.setEnabled(false);

		ListSelectionListener selectionListener = new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				int[] columns = table.getSelectedColumns();
				int[] rows    = table.getSelectedRows();
				int count = rows.length * columns.length;
				if(count == 0) {
					b_one.setEnabled(false);
					b_split.setEnabled(false);
					b_insert_row.setEnabled(false);
					b_remove_row.setEnabled(false);
				} else if(count == 1) {
					b_one.setEnabled(false);
					b_split.setEnabled(cellAtt.isCombined(rows[0], columns[0]));
					b_insert_row.setEnabled(true);
					b_remove_row.setEnabled(true);
				} else {
					b_one.setEnabled(cellAtt.isPossibleCombine(rows, columns));
					b_split.setEnabled(false);
					b_remove_row.setEnabled(true);
				}
				b_move_to_up.setEnabled(count > 0 &&
						cellAtt.isPossibleMove(rows[0], rows[rows.length-1], rows[0] - 1));
				b_move_to_down.setEnabled(count > 0 &&
						cellAtt.isPossibleMove(rows[0], rows[rows.length-1], rows[0] + 1));
				b_move_to_first.setEnabled(count > 0 &&
						cellAtt.isPossibleMove(rows[0], rows[rows.length-1], 0));
				b_move_to_last.setEnabled(count > 0 &&
						cellAtt.isPossibleMove(rows[0], rows[rows.length-1], ml.getRowCount() - rows.length));
			}
		};
		table.getSelectionModel().addListSelectionListener(selectionListener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

		JPanel p_buttons = new JPanel();
		p_buttons.setLayout(new GridLayout(0,2));
		p_buttons.add(b_one);
		p_buttons.add(b_split);
		p_buttons.add(b_add_row);
		p_buttons.add(b_add_column);
		p_buttons.add(b_insert_row);
		p_buttons.add(b_remove_row);
		p_buttons.add(b_increase_row_5);
		p_buttons.add(b_decrease_row_5);
		p_buttons.add(b_increase_col_5);
		p_buttons.add(b_decrease_col_5);
		p_buttons.add(b_move_to_up);
		p_buttons.add(b_move_to_down);
		p_buttons.add(b_move_to_first);
		p_buttons.add(b_move_to_last);

		add(scroll);
		add(p_buttons, BorderLayout.EAST);

		setSize( 800, 400 );
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {
		MultiSpanCellTableExample frame = new MultiSpanCellTableExample();
		frame.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				System.exit(0);
			}
		});
	}
}

