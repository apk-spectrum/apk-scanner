package com.apkscanner.gui.util;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.html.ImageView;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.core.AaptWrapper;
import com.apkscanner.gui.tabpanels.ImageResource.ResourceObject;

import com.apkscanner.util.Log;

public class ResouceContentsPanel extends JPanel{
	public static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
	public static final String CONTENT_HTML_VIEWER = "HtmlViewer";
	public static final String CONTENT_TABLE_VIEWER = "TableViewer";
	
	JHtmlEditorPane htmlViewer;
	JTable textTableViewer;
	ImageControlPanel imageViewerPanel;
	private String apkFilePath = null;
	private ResourceObject currentSelectedObj = null;
	private String[] resourcesWithValue = null;
	
	public ResouceContentsPanel() {
		
	}
	public void InitContentsPanel (String apkpath) {
		
		apkFilePath = apkpath;
		JLabel label = new JLabel();
		Font font = label.getFont();
		StringBuilder style = new StringBuilder("#basic-info, #perm-group {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#basic-info a {text-decoration:none; color:black;}");
		style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
		style.append(".danger-perm {text-decoration:none; color:red;}");
		style.append("#about {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");

		htmlViewer = new JHtmlEditorPane();
		htmlViewer.setStyle(style.toString());
		htmlViewer.setBackground(Color.white);
		htmlViewer.setEditable(false);
		htmlViewer.setOpaque(true);
		JScrollPane htmlViewerScroll = new JScrollPane(htmlViewer);
		
		textTableViewer = new JTable();
		textTableViewer.setShowHorizontalLines(false);
		textTableViewer.setTableHeader(null);
		textTableViewer.setCellSelectionEnabled(true);
		textTableViewer.setRowSelectionAllowed(false);
		textTableViewer.setColumnSelectionAllowed(false);
		textTableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane textTableScroll = new JScrollPane(textTableViewer);
		
		setLayout(new CardLayout());
		
		imageViewerPanel = new ImageControlPanel();
		
		this.add(htmlViewerScroll, CONTENT_HTML_VIEWER);
		this.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		this.add(textTableScroll, CONTENT_TABLE_VIEWER);
	}
	
    private void setTextContentPanel(ResourceObject obj) {
    	String content = null;
    	
		switch(obj.attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {obj.path});
			StringBuilder sb = new StringBuilder();
			for(String s: xmlbuffer) sb.append(s+"\n");
			content = sb.toString();
			break;
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
			ZipFile zipFile;
			try {
				zipFile = new ZipFile(apkFilePath);
				ZipEntry entry = zipFile.getEntry(obj.path);
				byte[] buffer = new byte[(int) entry.getSize()];
				zipFile.getInputStream(entry).read(buffer);
				content = new String(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case ResourceObject.ATTR_ETC:
			if("resources.arsc".equals(obj.path)) {
				if(resourcesWithValue != null) {
					textTableViewer.setModel(new StringListTableModel(resourcesWithValue));
					textTableViewer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
					 ((CardLayout)this.getLayout()).show(this, CONTENT_TABLE_VIEWER);
					return;
				} else {
					content = "lodding...";
				}
				break;
			}
		default:
			content = "This type is unsupported by preview.";
			
			
			
			
			
			
			
			break;
		}
		
		if(content != null) {
			htmlViewer.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
			//textViewerPanel.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("[\r]\n", "<br/>") + "</pre>");
			htmlViewer.setCaretPosition(0);
			((CardLayout)this.getLayout()).show(this, CONTENT_HTML_VIEWER);
		}
    }
    
    public void selectContent(JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        if(node == null) return;
		
        ResourceObject resObj = null;
		if(node.getUserObject() instanceof ResourceObject) {
			resObj = (ResourceObject)node.getUserObject();
		}
		
		if(resObj != null && resObj == currentSelectedObj) {
			Log.v("select same object");
			return;
		}
		currentSelectedObj = resObj;

		if(resObj == null || resObj.isFolder) {
			//htmlViewer.setText("");
			//((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_HTML_VIEWER);
		} else {
			switch(resObj.attr) {
			case ResourceObject.ATTR_IMG:
			case ResourceObject.ATTR_QMG:
				drawImageOnPanel(resObj);
				break;
			case ResourceObject.ATTR_AXML:
			case ResourceObject.ATTR_XML:
			case ResourceObject.ATTR_TXT:
			    setTextContentPanel(resObj);
				break;
			default:
			    setTextContentPanel(resObj);
				break;
			}
		}
    }
    
    private void drawImageOnPanel(ResourceObject obj) {
		try {
			imageViewerPanel.setImage(apkFilePath, obj.path);
			imageViewerPanel.repaint();
			 ((CardLayout)this.getLayout()).show(this, CONTENT_IMAGE_VIEWER);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} 
    }
    
	private class StringListTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1473513094076687257L;
		private String[] strings;

		StringListTableModel(String[] strings) {
			this.strings = strings;
		}
		
	    @Override
	    public int getColumnCount() {
	        return 1;
	    }

	    @Override
	    public int getRowCount() {
	        return strings.length;
	    }

	    @Override
	    public String getColumnName(int columnIndex) {
	        switch (columnIndex) {
	        case 0:
	            //return "#";
	        case 1:
	            return "Name";
	        }
	        return "";
	    }

	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        switch (columnIndex) {
	            case 0:                
	                //return String.format("%3d", rowIndex);
	            case 1:
	            	return strings[rowIndex];
	        }            
	        return "";
	    }
	}
}
