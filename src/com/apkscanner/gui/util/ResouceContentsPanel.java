package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.naming.directory.SearchResult;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextAreaHighlighter.HighlightInfo;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.apkscanner.DexLuncher;
import com.apkscanner.Launcher;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.core.AaptWrapper;
import com.apkscanner.gui.tabpanels.ImageResource.ResourceObject;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class ResouceContentsPanel extends JPanel{
	private static final long serialVersionUID = -934921813626224616L;
	
	public static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
	public static final String CONTENT_HTML_VIEWER = "HtmlViewer";
	public static final String CONTENT_TABLE_VIEWER = "TableViewer";
	public static final String CONTENT_SELECT_VIEWER = "SelectViewer";
	
	//JHtmlEditorPane htmlViewer;
	JTable textTableViewer;
	ImageControlPanel imageViewerPanel;
	private ResourceObject currentSelectedObj = null;
	private String[] resourcesWithValue = null;
	JPanel ContentsviewPanel;
	JTextField FilePathtextField;	
	SelectViewPanel selectPanel;
	Color defaultColor;
	ResourceObject CurrentresObj = null;
	ApkInfo apkinfo;
	RSyntaxTextArea xmltextArea;
	
	public ResouceContentsPanel() {
		
	}
	public void InitContentsPanel (ApkInfo apkinfo) {
		
		this.apkinfo = apkinfo;
		this.resourcesWithValue = apkinfo.resourcesWithValue;
		JLabel label = new JLabel();

//		Font font = label.getFont();
//		StringBuilder style = new StringBuilder("#basic-info, #perm-group {");
//		style.append("font-family:" + font.getFamily() + ";");
//		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
//		style.append("font-size:" + font.getSize() + "pt;}");
//		style.append("#basic-info a {text-decoration:none; color:black;}");
//		style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
//		style.append(".danger-perm {text-decoration:none; color:red;}");
//		style.append("#about {");
//		style.append("font-family:" + font.getFamily() + ";");
//		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
//		style.append("font-size:" + font.getSize() + "pt;}");
//		style.append("#about a {text-decoration:none;}");
//
//		htmlViewer = new JHtmlEditorPane();
//		htmlViewer.setStyle(style.toString());
//		htmlViewer.setBackground(Color.white);
//		htmlViewer.setEditable(false);
//		htmlViewer.setOpaque(true);
//		JScrollPane htmlViewerScroll = new JScrollPane(htmlViewer);
		
		xmltextArea = new RSyntaxTextArea(20, 60);
		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);				
		xmltextArea.setEditable(false);		
		RTextScrollPane sp = new RTextScrollPane(xmltextArea);
	      
		
		

		defaultColor = this.getBackground();
		
		textTableViewer = new JTable();
		textTableViewer.setShowHorizontalLines(false);
		textTableViewer.setTableHeader(null);
		textTableViewer.setCellSelectionEnabled(true);
		textTableViewer.setRowSelectionAllowed(false);
		textTableViewer.setColumnSelectionAllowed(false);
		textTableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane textTableScroll = new JScrollPane(textTableViewer);
		
		setLayout(new BorderLayout());
		
		imageViewerPanel = new ImageControlPanel();
		
		selectPanel = new SelectViewPanel();
		selectPanel.InitPanel();
		
		ContentsviewPanel = new JPanel(new CardLayout());
		
		ContentsviewPanel.add(sp, CONTENT_HTML_VIEWER);
		ContentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		ContentsviewPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
		ContentsviewPanel.add(selectPanel, CONTENT_SELECT_VIEWER);
		
		
		FilePathtextField = new JTextField("FilePath");
		FilePathtextField.setEditable(false);
		
		this.add(FilePathtextField, BorderLayout.NORTH);
		this.add(ContentsviewPanel, BorderLayout.CENTER);
		
	}
	
	public class SelectViewPanel extends JPanel {
		ImageIcon warring = Resource.IMG_WARNING.getImageIcon(50,50);
		
		HashMap<Integer, JLabel> IconHashMap = new HashMap<Integer, JLabel>();  

		public final static int SELECT_VIEW_ICON_OPEN = 1;
		public final static int SELECT_VIEW_ICON_JD_OPEN = 2;
		public final static int SELECT_VIEW_ICON_SCANNER_OPEN = 4;
		public final static int SELECT_VIEW_ICON_CHOOSE_APPLICATION = 8;
		public final static int SELECT_VIEW_ICON_EXPLORER = 16;
		
		
		String message = new String("Sorry, this file type is unsupported by preview.\nSo, open file with an external application by below button.");
		
		public void InitPanel() {
			JTextArea textArea = new JTextArea(message);
			JLabel warringLabel = new JLabel(warring);
			textArea.setBackground(defaultColor);
			
			JPanel MessagePanel = new JPanel(new FlowLayout());
	        MessagePanel.add(warringLabel);
	        MessagePanel.add(textArea);
	        MessagePanel.setBorder(new EmptyBorder(50, 0, 0, 0));
	        
	        this.setLayout(new BorderLayout());
	        
			this.add(MessagePanel, BorderLayout.NORTH);
			
			JPanel IconPanel = new JPanel(new GridBagLayout());
			
			IconHashMap.put(SELECT_VIEW_ICON_JD_OPEN, new JLabel("Open", Resource.IMG_RESOURCE_TREE_JD_ICON.getImageIcon(100,100), JLabel.CENTER));
			IconHashMap.put(SELECT_VIEW_ICON_SCANNER_OPEN, new JLabel("Open", Resource.IMG_APP_ICON.getImageIcon(100,100), JLabel.CENTER));
			IconHashMap.put(SELECT_VIEW_ICON_CHOOSE_APPLICATION, new JLabel("Choose Application", Resource.IMG_RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON.getImageIcon(100,100), JLabel.CENTER));
			IconHashMap.put(SELECT_VIEW_ICON_EXPLORER, new JLabel("Explorer", Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(100,100), JLabel.CENTER));
			IconHashMap.put(SELECT_VIEW_ICON_OPEN, new JLabel("Open", Resource.IMG_RESOURCE_TREE_OPEN_ICON.getImageIcon(100,100), JLabel.CENTER));
			
			for(int i=0; i< IconHashMap.size(); i++) {				
				final JLabel temp = IconHashMap.get(1<<i);
				temp.setHorizontalTextPosition(JLabel.CENTER);
		        temp.setVerticalTextPosition(JLabel.BOTTOM);
		        temp.setOpaque(true);
		        temp.setIconTextGap(5);
		        IconPanel.add(temp);
		        
		        temp.addMouseListener(new MouseListener() {		        	
		        	@Override
					public void mouseReleased(MouseEvent arg0) {	if(temp.isEnabled()==false) return;temp.setBackground(defaultColor);}					
					@Override
					public void mousePressed(MouseEvent arg0) { if(temp.isEnabled()==false) return;Color color = new Color(0, 155 ,200, 100); temp.setBackground(color);}					
					@Override
					public void mouseExited(MouseEvent arg0) { 	temp.setBackground(defaultColor);}					
					@Override
					public void mouseEntered(MouseEvent arg0) { if(temp.isEnabled()==false) return; temp.setDisabledIcon(Resource.IMG_RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon()); Color color = new Color(0, 155 ,100, 100); temp.setBackground(color);}					
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(temp.isEnabled()==false) return;
						//int ClickedObject = IconHashMap.get((JLabel)(temp));
						
						//HashMap<JLabel, Integer> reversedHashMap = MapUtils.invertMap(IconHashMap);
						
						HashMap<JLabel, Integer> reversedHashMap = new HashMap<JLabel, Integer>();
						for (Integer i : IconHashMap.keySet()) {
						    reversedHashMap.put(IconHashMap.get(i), i);
						}
						int ClickedObject = reversedHashMap.get(temp);
						
						Log.d("Click Label : "+ClickedObject);
						String resPath = apkinfo.tempWorkPath + File.separator + CurrentresObj.path.replace("/", File.separator);
						ZipFileUtil.unZip(apkinfo.filePath, currentSelectedObj.path, resPath);
						
						switch(ClickedObject) {
						case SELECT_VIEW_ICON_JD_OPEN:
							if(CurrentresObj!=null) {
								temp.setIcon(Resource.IMG_RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon());
								temp.setDisabledIcon(Resource.IMG_RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon());
								temp.setEnabled(false);
								DexLuncher.openDex(resPath, new DexLuncher.DexWrapperListener() {
									@Override
									public void OnError() {}
									@Override
									public void OnSuccess() {
										temp.setDisabledIcon(null);										
										temp.setIcon(Resource.IMG_RESOURCE_TREE_JD_ICON.getImageIcon(100,100));
										temp.setEnabled(true);										
										temp.repaint();
									}
								});
							}
							break;
						case SELECT_VIEW_ICON_SCANNER_OPEN:
							Launcher.run(resPath);
							break;
						case SELECT_VIEW_ICON_CHOOSE_APPLICATION:
							break;
						case SELECT_VIEW_ICON_EXPLORER:
							break;
						case SELECT_VIEW_ICON_OPEN:
							String openner;
							if(System.getProperty("os.name").indexOf("Window") >-1) {
								openner = "explorer";
							} else {  //for linux
								openner = "xdg-open";
							}
							try {
									new ProcessBuilder(openner, resPath).start();
								} catch (IOException e1) { }
							break;
						default:							
							Log.e("unknown Label : " + ClickedObject + " JLabel : " + temp);
						}
						
					}
				});
			}
			
			this.add(IconPanel);
			
			
		}
		
		public void setMenu(int Flag) {
			for(int i=0; i< IconHashMap.size(); i++) {				
				if((Flag & 1<<i) ==0) {
					IconHashMap.get(1<<i).setVisible(false);
					
				} else {
					IconHashMap.get(1<<i).setVisible(true);
				}
			}
			this.invalidate();
			this.repaint();
		}
		
	}
	
    private void setTextContentPanel(ResourceObject obj) {
    	String content = null;
    	
		switch(obj.attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {obj.path});
			StringBuilder sb = new StringBuilder();
			for(String s: xmlbuffer) sb.append(s+"\n");
			content = sb.toString();
			break;
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
			ZipFile zipFile;
			try {
				zipFile = new ZipFile(apkinfo.filePath);
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
					 ((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_TABLE_VIEWER);
					//content =  Arrays.toString(resourcesWithValue);
					return;
				} else {
					content = "lodding...";
				}
				break;
			}
		default:
			//content = "This type is unsupported by preview.";
			content=null;
			((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_SELECT_VIEWER);
			
			if(obj.path.endsWith(".dex")) {
				selectPanel.setMenu(SelectViewPanel.SELECT_VIEW_ICON_JD_OPEN | SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION);
			} else if(obj.path.endsWith(".apk")) {
				selectPanel.setMenu(SelectViewPanel.SELECT_VIEW_ICON_SCANNER_OPEN | SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION
						| SelectViewPanel.SELECT_VIEW_ICON_EXPLORER);
			} else if(obj.path.endsWith(".qmg")) {
				
			} else {
				selectPanel.setMenu(SelectViewPanel.SELECT_VIEW_ICON_OPEN | SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION);
			}
			
			break;
		}
		
		if(content != null) {
			//htmlViewer.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
			//textViewerPanel.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("[\r]\n", "<br/>") + "</pre>");
			//htmlViewer.setCaretPosition(0);
			
			xmltextArea.setText(content);
			xmltextArea.setCaretPosition(0);
			((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_HTML_VIEWER);
			//xmltextArea.setHighlightCurrentLine(true);
			//xmltextArea.setHighlighter(h);
			
			
		}		
    }
    
    public void selectContentAndLine(JTree tree, int line, String Findstr) {
    	SearchContext context = new SearchContext();
        context.setMatchCase(false);
        context.setMarkAll(true);
        context.setSearchFor(Findstr);
        context.setWholeWord(false);
        
        org.fife.ui.rtextarea.SearchResult result = SearchEngine.find(xmltextArea, context);
        
        //xmltextArea.getText().s
        
        //xmltextArea.setCaretPosition(0);
        
        if (!result.wasFound()) {
        	xmltextArea.setCaretPosition(0);
        	SearchEngine.find(xmltextArea, context);
        	Log.d("not found");
        }	
    	
    }
    
    public void selectContent(JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        if(node == null) return;
		
        CurrentresObj = null;
		if(node.getUserObject() instanceof ResourceObject) {
			CurrentresObj = (ResourceObject)node.getUserObject();
		}
		
		if(CurrentresObj != null && CurrentresObj == currentSelectedObj) {
			Log.v("select same object");
			Log.d("" +node.getPath().toString());
			return;
		}
		currentSelectedObj = CurrentresObj;

		if(CurrentresObj == null || CurrentresObj.isFolder) {
			//htmlViewer.setText("");
			//((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_HTML_VIEWER);
			FilePathtextField.setText("folder");
		} else {
			switch(CurrentresObj.attr) {
			case ResourceObject.ATTR_IMG:
			case ResourceObject.ATTR_QMG:
				drawImageOnPanel(CurrentresObj);
				break;
			case ResourceObject.ATTR_AXML:
			case ResourceObject.ATTR_XML:
			case ResourceObject.ATTR_TXT:
			    setTextContentPanel(CurrentresObj);
				break;
			default:
			    setTextContentPanel(CurrentresObj);
				break;
			}
			FilePathtextField.setText(CurrentresObj.path);
		}		
    }
    
    private void drawImageOnPanel(ResourceObject obj) {
		try {
			imageViewerPanel.setImage(apkinfo.filePath, obj.path);
			imageViewerPanel.repaint();
			 ((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_IMAGE_VIEWER);
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
