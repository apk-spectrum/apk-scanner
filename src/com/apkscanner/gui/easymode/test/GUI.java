package com.apkscanner.gui.easymode.test;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class GUI extends JFrame {
    protected GUI() {
        super("Simple Rearrangeable List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createPanel();
        setBounds(10, 10, 350, 500);
        setVisible(true);
    }

    private void createPanel() {
        final DefaultListModel<String> strings = new DefaultListModel<String>();

        for(int i = 1; i <= 100; i++) {
            strings.addElement("Item " + i);
        }

        final JList<String> dndList = new JList<String>(strings);
        dndList.setDragEnabled(true);
        dndList.setDropMode(DropMode.INSERT);
        dndList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dndList.setTransferHandler(new TransferHandler() {
            private int index;
            private boolean beforeIndex = false; //Start with `false` therefore if it is removed from or added to the list it still works

            @Override
            public int getSourceActions(JComponent comp) {
                return MOVE;
            }

            @Override
            public Transferable createTransferable(JComponent comp) {
                index = dndList.getSelectedIndex(); 
                return new StringSelection(dndList.getSelectedValue());
            }

            @Override
            public void exportDone(JComponent comp, Transferable trans, int action) {
                if (action == MOVE) {
                    if(beforeIndex)
                        strings.remove(index + 1);
                    else
                        strings.remove(index);
                }
            }

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                try {
                    String s = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                    strings.add(dl.getIndex(), s);
                    beforeIndex = dl.getIndex() < index ? true : false;
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dndList);
        getContentPane().add(scrollPane);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GUI();
			}
		});
    }
}