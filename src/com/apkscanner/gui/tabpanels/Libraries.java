package com.apkscanner.gui.tabpanels;


import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RStr;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.FileUtil.FSStyle;
import com.apkscanner.util.ZipFileUtil;

public class Libraries extends AbstractTabbedPanel
{
	private static final long serialVersionUID = -8985157400085276691L;

	private SimpleTableModel tableModel;

	public Libraries()
	{
		setLayout(new GridLayout(1, 0));
		setTitle(RComp.TABBED_LIBRARIES);
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		JTable table = new JTable(tableModel = new LibTableModel(),
				new SimpleTableColumnModel(5, 295, 150, 50));
		table.setAutoCreateColumnsFromModel(true);

		add(new JScrollPane(table));
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status)
	{
		if(!Status.LIB_COMPLETED.equals(status)) {
			return;
		}

		if(tableModel == null)
			initialize();
		tableModel.setData(apkInfo);

		setDataSize(apkInfo.libraries.length, true, false);
		setTabbedVisible(apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX);
	}

	class LibTableModel extends SimpleTableModel {
		private static final long serialVersionUID = 8403482180817388452L;

		LibTableModel() {
			super(RStr.LIB_COLUMN_INDEX, RStr.LIB_COLUMN_PATH,
					RStr.LIB_COLUMN_SIZE, RStr.LIB_COLUMN_COMPRESS);
		}

		@Override
		public void setData(ApkInfo apkInfo) {
			data.clear();
			if(apkInfo.libraries == null) return;

			for(int i=0; i< apkInfo.libraries.length; i++) {
				long size = ZipFileUtil.getFileSize(apkInfo.filePath, apkInfo.libraries[i]);
				long compressed = ZipFileUtil.getCompressedSize(apkInfo.filePath, apkInfo.libraries[i]);
				Object[] temp = {
						i+1,
						apkInfo.libraries[i],
						FileUtil.getFileSize(size, FSStyle.FULL),
						String.format("%.2f", ((float)(size - compressed) / (float)size) * 100f) + " %"
				};
				data.add(temp);
			}
			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int row, int col) { return col > 0; }
	}
}
