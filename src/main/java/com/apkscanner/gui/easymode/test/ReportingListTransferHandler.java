package com.apkscanner.gui.easymode.test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import com.apkspectrum.util.Log;

@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class ReportingListTransferHandler extends TransferHandler {
    DataFlavor localArrayListFlavor, serialArrayListFlavor;
    String localArrayListType = DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=java.util.ArrayList";
    JList source = null;
    int[] indices = null;
    int addIndex = -1; //Location where items were added
    int addCount = 0;  //Number of items added

    public ReportingListTransferHandler() {
        try {
            localArrayListFlavor = new DataFlavor(localArrayListType);
        } catch (ClassNotFoundException e) {
            sayIt("unable to create data flavor");
        }
        serialArrayListFlavor = new DataFlavor(ArrayList.class,
                                              "ArrayList");
    }

    //@SuppressWarnings("unchecked")
    @Override
	public boolean importData(JComponent c, Transferable t) {
        JList target = null;
        ArrayList alist = null;
        if (!canImport(c, t.getTransferDataFlavors())) {
            sayIt("unable to import data (flavor)");
            return false;
        }
        try {
            target = (JList)c;
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (ArrayList)t.getTransferData(localArrayListFlavor);
            } else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
                alist = (ArrayList)t.getTransferData(serialArrayListFlavor);
            } else {
                return false;
            }
        } catch (UnsupportedFlavorException ufe) {
            sayIt("importData: unsupported data flavor");
            return false;
        } catch (IOException ioe) {
            sayIt("importData: I/O exception");
            return false;
        }

        sayIt("got the target");

        //At this point we use the same code to retrieve the data
        //locally or serially.

        //We'll drop at the current selected index.
        int index = target.getSelectedIndex();


        //Prevent user from dropping data back on itself.
        if (source.equals(target)) {
            if (indices != null && index >= indices[0] - 1 &&
                  index <= indices[indices.length - 1]) {
                indices = null;
                return true;
            }
        }

        DefaultListModel listModel = (DefaultListModel)target.getModel();
        int max = listModel.getSize();
        System.out.printf("index = %d  max = %d%n", index, max);
        if (index < 0) {
            index = max; 
        } else {
            index++;
            if (index > max) {
                index = max;
            }
        }
        addIndex = index;
        addCount = alist.size();
        for (int i=0; i < alist.size(); i++) {
            listModel.add(index++, alist.get(i));
        }
        return true;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if ((action == MOVE) && (indices != null)) {
            DefaultListModel model = (DefaultListModel)source.getModel();

           sayIt("source name: "+source.getName());
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex &&
                        indices[i] + addCount < model.getSize()) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length -1; i >= 0; i--)
                model.remove(indices[i]);
        }
        indices = null;
        addIndex = -1;
        addCount = 0;
    }

    private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
        if (localArrayListFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(localArrayListFlavor)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
        if (serialArrayListFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(serialArrayListFlavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasLocalArrayListFlavor(flavors))  { return true; }
        if (hasSerialArrayListFlavor(flavors)) { return true; }
        sayIt("checking flavor...");
        return false;
    }

    @SuppressWarnings("deprecation")
	@Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JList) {
            source = (JList)c;
            indices = source.getSelectedIndices();
            Object[] values = source.getSelectedValues();
            if (values == null || values.length == 0) {
                sayIt("null values or 0 length");
                return null;
            }
            //String -> Object
            ArrayList<Object> alist = new ArrayList<Object>(values.length);
            for (int i = 0; i < values.length; i++) {
                Object o = values[i];
                //String str = o.toString();
                //if (str == null) str = "";
                alist.add(o);
            }
            return new ReportingListTransferable(alist);
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    private void sayIt(String s){
        Log.d(s);
    }

    public class ReportingListTransferable implements Transferable {
		ArrayList data;

		public ReportingListTransferable(ArrayList alist) {
            data = alist;
        }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { localArrayListFlavor,
                                      serialArrayListFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (localArrayListFlavor.equals(flavor)) {
                sayIt("local flavor supported");
                return true;
            }
            if (serialArrayListFlavor.equals(flavor)) {
                sayIt("serial  flavor supported");
                return true;
            }
            sayIt("flavor not supported - returned false");
            return false;
        }
    }
}