package com.apkscanner.gui.tabpanels;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class SimpleTableColumnModel extends DefaultTableColumnModel {
    private static final long serialVersionUID = -8456874359714244362L;

    private final int[] columnSize;

    public SimpleTableColumnModel(int... preferredWidth) {
        columnSize = preferredWidth;
    }

    @Override
    public void addColumn(TableColumn aColumn) {
        aColumn.setPreferredWidth(columnSize[aColumn.getModelIndex()]);
        super.addColumn(aColumn);
    }
}
