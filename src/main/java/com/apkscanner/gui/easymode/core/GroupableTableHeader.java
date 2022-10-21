package com.apkscanner.gui.easymode.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class GroupableTableHeader extends JTableHeader {
    private static final long serialVersionUID = -3411849042833954584L;

	@SuppressWarnings("unused")
    private static final String uiClassID = "GroupableTableHeaderUI";

    protected List<ColumnGroup> columnGroups = new ArrayList<ColumnGroup>();

    public GroupableTableHeader(TableColumnModel model) {
        super(model);
        setUI(new GroupableTableHeaderUI());
        setReorderingAllowed(false);
        // setDefaultRenderer(new MultiLineHeaderRenderer());
    }

    @Override
    public void updateUI() {
        setUI(new GroupableTableHeaderUI());
    }

    @Override
    public void setReorderingAllowed(boolean b) {
        super.setReorderingAllowed(false);
    }

    public void addColumnGroup(ColumnGroup g) {
        columnGroups.add(g);
    }

    public List<ColumnGroup> getColumnGroups(TableColumn col) {
        for (ColumnGroup group : columnGroups) {
            List<ColumnGroup> groups = group.getColumnGroups(col);
            if (!groups.isEmpty()) {
                return groups;
            }
        }
        return Collections.emptyList();
    }

    public void setColumnMargin() {
        int columnMargin = getColumnModel().getColumnMargin();
        for (ColumnGroup group : columnGroups) {
            group.setColumnMargin(columnMargin);
        }
    }

}