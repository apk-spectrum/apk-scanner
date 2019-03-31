package com.apkscanner.gui.component.table;

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

import com.apkscanner.resource.Resource;


/**
 * @version 1.0 11/26/98
 */
public class MultiSpanCellTableExample extends JFrame {
	private static final long serialVersionUID = -6881051761931159111L;

	MultiSpanCellTableExample() {
		super( "Multi-Span Cell Example" );

		try {
			UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		final AttributiveCellTableModel ml = new AttributiveCellTableModel(0,3);
		final MultiSpanCellTable table = new MultiSpanCellTable( ml );
		JScrollPane scroll = new JScrollPane( table );

		ml.setTable(table);
		final CellSpan cellAtt =(CellSpan)ml.getCellAttribute();

		final JButton b_one   = new JButton("Combine");
		b_one.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] columns = table.getSelectedColumns();
				int[] rows    = table.getSelectedRows();
				cellAtt.combine(rows, columns);
			}
		});
		final JButton b_split = new JButton("Split");
		b_split.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int column = table.getSelectedColumn();
				int row    = table.getSelectedRow();
				cellAtt.split(row, column);
			}
		});
		final JButton b_add = new JButton("Add");
		b_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ml.addRow(new Object[] {"add","to","last"});
			}
		});
		final JButton b_insert = new JButton("Insert");
		b_insert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row    = table.getSelectedRow();
				if(row < 0) return;
				ml.insertRow(row, new Object[] {"insert", "to", Integer.toString(row)});
			}
		});
		final JButton b_remove = new JButton("Remove");
		b_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row    = table.getSelectedRow();
				if(row < 0) return;
				ml.removeRow(row);
			}
		});

		b_one.setEnabled(false);
		b_split.setEnabled(false);
		b_add.setEnabled(true);
		b_insert.setEnabled(false);
		b_remove.setEnabled(false);

		ListSelectionListener selectionListener = new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				int[] columns = table.getSelectedColumns();
				int[] rows    = table.getSelectedRows();
				int count = rows.length * columns.length;
				if(count == 0) {
					b_one.setEnabled(false);
					b_split.setEnabled(false);
					//b_add.setEnabled(true);
					b_insert.setEnabled(false);
					b_remove.setEnabled(false);
				} else if(count == 1) {
					b_one.setEnabled(false);
					b_split.setEnabled(cellAtt.isCombined(rows[0], columns[0]));
					//b_add.setEnabled(true);
					b_insert.setEnabled(true);
					b_remove.setEnabled(true);
				} else {
					b_one.setEnabled(cellAtt.isPossibleCombine(rows, columns));
					b_split.setEnabled(false);
					b_remove.setEnabled(true);
				}
			}
		};
		table.getSelectionModel().addListSelectionListener(selectionListener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

		JPanel p_buttons = new JPanel();
		p_buttons.setLayout(new GridLayout(0,2));
		p_buttons.add(b_one);
		p_buttons.add(b_split);
		p_buttons.add(b_add);
		p_buttons.add(b_insert);
		p_buttons.add(b_remove);

		add(scroll);
		add(p_buttons, BorderLayout.EAST);

		setSize( 400, 200 );
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

