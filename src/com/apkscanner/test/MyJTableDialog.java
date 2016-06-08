package com.apkscanner.test;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.apkscanner.util.Log;

public class MyJTableDialog extends JDialog {

    JButton btnenable;
    JButton btnApply;
    JButton btnCancel;
    MainPanel mainPanel;

    public MyJTableDialog() {
        this.setTitle("MyJTableDialog");
        this.setModal(true);
        mainPanel = new MainPanel();
        this.add(mainPanel);
        this.setBounds(200,200,635, 340);
    }


     public static class MainPanel extends JPanel implements ActionListener , TableModelListener{
        JButton btnEnable;
        JButton btnApply;
        JButton btnCancel;
        TablePanel tablePanel;
        JLabel title;
        boolean isARowChecked=false;

        public MainPanel(){
            setLayout(null);
            title = new JLabel("Enabled when checked:");
            title.setBounds(50,10,300, 20);
            this.add(title);

            tablePanel = new TablePanel(this); //this
            this.add(tablePanel);
            tablePanel.setBounds(10,30,600, 200);

            btnEnable = new JButton("Enabled when checked");
            btnEnable.setBounds(110,240, 170, 20);
            btnEnable.setEnabled(false);
            add(btnEnable);
            btnEnable.setActionCommand(AC.enableButton);
            btnEnable.addActionListener(this);

            btnApply = new JButton("Apply");
            btnApply.setBounds(300, 240, 80, 20);
            add(btnApply);
            btnApply.setActionCommand(AC.applyButton);
            btnApply.addActionListener(this);

            btnCancel = new JButton("Cancel");
            btnCancel.setBounds(400,240, 80, 20);
            add(btnCancel);
            btnCancel.setActionCommand(AC.cancelButton);
            btnCancel.addActionListener(this);

        }

        private static class AC
        {
            public static final String enableButton = "AC_enableButton";
            public static final String applyButton = "AC_applyButton";
            public static final String cancelButton = "AC_cancelButton";
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String ac = e.getActionCommand();

            if (ac == AC.enableButton){
            	
               //	http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
            	//custom title, no icon
            	JOptionPane.showMessageDialog(this,
            	    "The enabled button was clicked.",
            	    "A plain message",
            	    JOptionPane.PLAIN_MESSAGE);


            }
            else if (ac == AC.applyButton){
            	//custom title, no icon
            	JOptionPane.showMessageDialog(this,
            	    "The apply button was clicked.",
            	    "A plain message",
            	    JOptionPane.PLAIN_MESSAGE);

            }

            else if (ac == AC.cancelButton){

            	btnEnable.setEnabled(false);
            	
            	int rowCount = tablePanel.model.getRowCount();
            	 for (int i = 0; i < rowCount; i++){
            		 tablePanel.model.setValueAt(false, i, 1);
                    
                 }
            	
            	
            	tablePanel.model.setValueAt(false, 0, 1);

            }

        }


        @Override
        public void tableChanged(TableModelEvent e) {


            if (e.getType() == TableModelEvent.UPDATE & e.getColumn() == 1) {

                int rowCount = tablePanel.model.getRowCount();

                for (int i = 0; i < rowCount; i++){
                    Object dataCol1 = tablePanel.model.getValueAt(i, 1);
                    //System.out.println("tttt" + dataCol1);

                    if ((Boolean)dataCol1) {
                            isARowChecked = true;
                                break;
                    }
                    else {
                            isARowChecked = false;
                    }
                }

                btnEnable.setEnabled(isARowChecked);
            }

        } // end changed listener

     } // end main panel class

     public static class TablePanel extends JPanel  implements TableModelListener{
            private JTable table;
            JLabel  busyLabel;
            DefaultTableModel model;

            int rowSelected=-1;

            ArrayList rowsSelectedList = new ArrayList();
            boolean somethingChkd = false;

            Object[] columnNames = {"Col 0", "Col 1", "Col 2"};
            Object[][] data = {};

            public TablePanel(TableModelListener t) {//

                model = new DefaultTableModel(data, columnNames);
                table = new JTable(model) {

                    @Override
                    public Class getColumnClass(int column) {
                        switch (column) {
                            case 0:
                                return  String.class;
                            case 1:
                                return Boolean.class;
                            default:
                                return String.class;
                        }
                    }


                    @Override
                    public boolean isCellEditable(int row,int cols){

                        if(cols==0 | cols == 2 ){return false;}

                        return true;

                    }
                };

                table.getModel().addTableModelListener(t);// t
                table.setPreferredScrollableViewportSize(new Dimension(500, 170));
                table.setFillsViewportHeight(true);
                table.setRowSelectionAllowed(true);
                table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                table.getTableHeader().setReorderingAllowed(false);

                //Create the scroll pane and add the table to it.
                JScrollPane scrollPane = new JScrollPane(table);

                //Add the scroll pane to this panel.
                add(scrollPane);

            }//end constructor


            public void setData(Object[][] objArray){

                int rc = model.getRowCount();

                for(int i = rc-1; i>=0 ; i--){
                   this.model.removeRow(i);
                }

                data = objArray.clone();

                for(int i = 0; i < objArray.length;i++){
                    model.addRow(new Object[]{objArray[i][0], objArray[i][1], objArray[i][2]});

                }

            }


            @Override
            public void tableChanged(TableModelEvent e)
            {

            	

            }// end table changed event

        } // end TablePanelClass

	public static void main(String[] args){

		MyJTableDialog myJTD = new MyJTableDialog();

		Object[][] objArr = {{"Row 0 Col 0",false,"Row 0 Col 2"},{"Row 1 Col 0",false,"Row 1 Col 2"},{"Row 2 Col 0",false,"Row 2 Col 2"}};
		myJTD.mainPanel.tablePanel.setData(objArr);

		myJTD.setVisible(true);
	}

}
