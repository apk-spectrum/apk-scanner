package com.apkscanner.gui.easymode.dlg;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
@SuppressWarnings("serial")
public class ListItemTransferHandler extends TransferHandler {
	protected final DataFlavor localObjectFlavor;
	JList<?> source = null;
	JList<Object> target = null;
	protected int[] indices;
	protected int addIndex = -1; // Location where items were added
	protected int addCount; // Number of items added.

	public ListItemTransferHandler() {
		super();
		// localObjectFlavor = new ActivationDataFlavor(
		// Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of
		// items");
		localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		//Log.d("createTransferable");
		
		
		source = (JList<?>) c;
		c.getRootPane().getGlassPane().setVisible(true);
		
		indices = source.getSelectedIndices();
		final Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
		// return new DataHandler(transferedObjects,
		// localObjectFlavor.getMimeType());
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { localObjectFlavor };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return Objects.equals(localObjectFlavor, flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {
					return transferedObjects;
				} else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
	}

	@Override
	public boolean canImport(TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
	}

	@Override
	public int getSourceActions(JComponent c) {
		Component glassPane = c.getRootPane().getGlassPane();
		
		glassPane.setCursor(DragSource.DefaultMoveDrop);
		
		return MOVE; // COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		//Log.d("importData :");
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
			return false;
		}

		JList.DropLocation dl = (JList.DropLocation) tdl;
		target = (JList<Object>) info.getComponent();
		DefaultListModel<Object> listModel = (DefaultListModel<Object>) target.getModel();
		int max = listModel.getSize();
		int index = dl.getIndex();
		
		index = index < 0 ? max : index; // If it is out of range, it is
											// appended to the end
		index = Math.min(index, max);

		addIndex = index;

		
		
		try {
			Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
			//Log.d("importData = max : " + max + "addIndex :" + addIndex + "values : " + values[0]);	
			
			for (int i = 0; i < values.length; i++) {
				int idx = index++;
				//Log.d("idx = " + idx);
				listModel.add(idx, values[i]);
				target.addSelectionInterval(idx, idx);
			}
			addCount = values.length;
			//Log.d("addCount = " + addCount);
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
		}

		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		//Log.d("exportDone :");
		c.getRootPane().getGlassPane().setVisible(false);
		cleanup(c, action == MOVE);
	}

	private void cleanup(JComponent c, boolean remove) {
		
		
		if (remove && indices != null) {
			//Log.d("indices[0]: "+indices[0]);
			DefaultListModel<?> model = (DefaultListModel<?>) source.getModel();
			if (addCount > 0 && target.equals(source)) {
				// https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
				for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex &&
                            indices[i] + addCount < model.getSize()) {
                            indices[i] += addCount;
                        }
				}
			}
			
			for (int i = indices.length - 1; i >= 0; i--) {
				//Log.d("indices[0]: "+indices[0]);
				model.remove(indices[i]);
			}
		}

		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}