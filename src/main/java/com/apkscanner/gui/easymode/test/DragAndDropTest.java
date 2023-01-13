package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
// import javax.activation.ActivationDataFlavor;
// import javax.activation.DataHandler;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class DragAndDropTest {
    public JComponent makeUI() {
        DefaultListModel<Thumbnail> m = new DefaultListModel<>();
        for (String s : Arrays.asList("error", "information", "question", "warning")) {
            m.addElement(new Thumbnail(s));
        }

        JList<Thumbnail> list = new JList<>(m);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(new ListItemTransferHandler());
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);
        // https://java-swing-tips.blogspot.com/2008/10/rubber-band-selection-drag-and-drop.html
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(0);
        list.setFixedCellWidth(80);
        list.setFixedCellHeight(80);
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        list.setCellRenderer(new ListCellRenderer<Thumbnail>() {
            private final JPanel p = new JPanel(new BorderLayout());
            private final JLabel icon = new JLabel((Icon) null, JLabel.CENTER);
            private final JLabel label = new JLabel("", JLabel.CENTER);

            @Override
            public Component getListCellRendererComponent(JList<? extends Thumbnail> list,
                    Thumbnail value, int index, boolean isSelected, boolean cellHasFocus) {
                icon.setIcon(value.icon);
                label.setText(value.name);
                label.setForeground(
                        isSelected ? list.getSelectionForeground() : list.getForeground());
                p.add(icon);
                p.add(label, BorderLayout.SOUTH);
                p.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                return p;
            }
        });
        return new JScrollPane(list);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.getContentPane().add(new DragAndDropTest().makeUI());
        f.setSize(320, 240);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}


class Thumbnail implements Serializable {
    private static final long serialVersionUID = -6262132799347406683L;

    public final String name;
    public final Icon icon;

    public Thumbnail(String name) {
        this.name = name;
        this.icon = UIManager.getIcon("OptionPane." + name + "Icon");
    }
}


// @camickr already suggested above.
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
class ListItemTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -3639428239766624579L;

    protected final DataFlavor localObjectFlavor;
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
        JList<?> source = (JList<?>) c;
        c.getRootPane().getGlassPane().setVisible(true);

        indices = source.getSelectedIndices();
        final Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
        // return new DataHandler(transferedObjects,
        // localObjectFlavor.getMimeType());
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] {localObjectFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return Objects.equals(localObjectFlavor, flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean importData(TransferSupport info) {
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
            return false;
        }

        JList.DropLocation dl = (JList.DropLocation) tdl;
        JList target = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int max = listModel.getSize();
        int index = dl.getIndex();
        index = index < 0 ? max : index; // If it is out of range, it is
                                         // appended to the end
        index = Math.min(index, max);

        addIndex = index;

        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        c.getRootPane().getGlassPane().setVisible(false);
        cleanup(c, action == MOVE);
    }

    @SuppressWarnings("rawtypes")
    private void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            if (addCount > 0) {
                // https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }

        indices = null;
        addCount = 0;
        addIndex = -1;
    }
}
