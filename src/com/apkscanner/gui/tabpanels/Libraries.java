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
import com.google.common.base.Objects;

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

			String dir = null, preDir = null;
			long dirSize = 0, dirCompressed = 0, totalSize = 0, totalCompressed = 0;
			int preIdx = 0, num = 0;
			for(String lib: apkInfo.libraries) {
				dir = lib.substring(0, lib.lastIndexOf("/"));
				if(!Objects.equal(dir, preDir)) {
					if(preDir != null) {
						data.add(preIdx, new Object[] {
								"Arch", preDir.substring(4) + " (" + num + ")",
								FileUtil.getFileSize(dirSize, FSStyle.FULL),
								String.format("%.2f", ((float)(dirSize - dirCompressed) / (float)dirSize) * 100f) + " %"
						});
						data.add(new Object[] { "", "", "", "" });
						preIdx = data.size();
					}
					totalSize += dirSize;
					totalCompressed += dirCompressed;
					dirSize = dirCompressed = num = 0;
					preDir = dir;
				}
				long size = ZipFileUtil.getFileSize(apkInfo.filePath, lib);
				long compressed = ZipFileUtil.getCompressedSize(apkInfo.filePath, lib);
				dirSize += size;
				dirCompressed += compressed;
				Object[] temp = {
						++num,
						lib.substring(4),
						FileUtil.getFileSize(size, FSStyle.FULL),
						String.format("%.2f", ((float)(size - compressed) / (float)size) * 100f) + " %"
				};
				data.add(temp);
			}
			if(preDir != null) {
				data.add(preIdx, new Object[] {
						"Arch", dir.substring(4) + " (" + num + ")",
						FileUtil.getFileSize(dirSize, FSStyle.FULL),
						String.format("%.2f", ((float)(dirSize - dirCompressed) / (float)dirSize) * 100f) + " %"
				});
				totalSize += dirSize;
				totalCompressed += dirCompressed;
			}
			if(totalSize > 0) {
				data.add(0, new Object[] {
						"Total", "All libraies (" + apkInfo.libraries.length + ")",
						FileUtil.getFileSize(totalSize, FSStyle.FULL),
						String.format("%.2f", ((float)(totalSize - totalCompressed) / (float)totalSize) * 100f) + " %"
				});
				data.add(1, new Object[] { "", "", "", "" });
			}
			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable(int row, int col) { return col > 0; }
	}
}
