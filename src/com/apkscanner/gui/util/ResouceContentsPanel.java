package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import com.apkscanner.DexLuncher;
import com.apkscanner.Launcher;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.apkscanner.AxmlToXml;
import com.apkscanner.core.AaptWrapper;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.tabpanels.ImageResource;
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
	
	public static final String TEXTVIEWER_TOOLBAR_OPEN = "textviewer_toolbar_open";
	public static final String TEXTVIEWER_TOOLBAR_SAVE= "textviewer_toolbar_save";
	public static final String TEXTVIEWER_TOOLBAR_FIND = "textviewer_toolbar_find";
	
	//JHtmlEditorPane htmlViewer;
	private JTable textTableViewer;
	private ImageControlPanel imageViewerPanel;
	private ResourceObject currentSelectedObj = null;
	private String[] resourcesWithValue = null;
	private JPanel ContentsviewPanel;
	private JTextField FilePathtextField;	
	private SelectViewPanel selectPanel;
	private ApkInfo apkinfo;
	private RSyntaxTextArea xmltextArea;
	private FindDialog finddlg;
	private JToolBar toolBar;
	
	public ResouceContentsPanel() {
		
		xmltextArea  = new RSyntaxTextArea();
		//xmltextArea.createToolTip();
		
		JPanel TextAreaPanel = new JPanel(new BorderLayout());
		
		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);
		xmltextArea.setMarkOccurrences(true);
		xmltextArea.setEditable(false);	
		RTextScrollPane sp = new RTextScrollPane(xmltextArea);
		
		TextAreaPanel.add(sp);
		
		ErrorStrip errorStrip = new ErrorStrip(xmltextArea);
		TextAreaPanel.add(errorStrip,BorderLayout.LINE_END);
		
		
		toolBar = new JToolBar("");
		initToolbar(toolBar);
		
		TextAreaPanel.add(toolBar,BorderLayout.PAGE_START);
		
		finddlg = new FindDialog(MainUI.getCurrentParentsFrame(), new SearchListener() {			
			@Override
			public void searchEvent(SearchEvent e) {
				// TODO Auto-generated method stub
				SearchEvent.Type type = e.getType();
				SearchContext context = e.getSearchContext();
				SearchResult result = null;

				switch (type) {
					default: // Prevent FindBugs warning later
					case MARK_ALL:
						result = SearchEngine.markAll(xmltextArea, context);
						break;
					case FIND:
						result = SearchEngine.find(xmltextArea, context);
						if (!result.wasFound()) {
							UIManager.getLookAndFeel().provideErrorFeedback(xmltextArea);
						}
						break;
				}

				String text = null;
				if (result.wasFound()) {
					text = "Text found; occurrences marked: " + result.getMarkedCount();
				}
				else if (type==SearchEvent.Type.MARK_ALL) {
					if (result.getMarkedCount()>0) {
						text = "Occurrences marked: " + result.getMarkedCount();
					}
					else {
						text = "";
					}
				}
				else {
					text = "Text not found";
				}				
			}
			
			@Override
			public String getSelectedText() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		finddlg.setResizable(false);
		
		
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
		
		ContentsviewPanel = new JPanel(new CardLayout());
		
		ContentsviewPanel.add(TextAreaPanel, CONTENT_HTML_VIEWER);
		ContentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		ContentsviewPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
		ContentsviewPanel.add(selectPanel, CONTENT_SELECT_VIEWER);
		
		
		JPanel northPanel = new JPanel(new BorderLayout());
		FilePathtextField = new JTextField("FilePath");
		FilePathtextField.setEditable(false);
		FilePathtextField.setBackground(Color.WHITE);
		
        String keyStrokeAndKey = "control F";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        xmltextArea.getInputMap().put(keyStroke, keyStrokeAndKey);
        xmltextArea.getActionMap().put(keyStrokeAndKey, new AbstractAction() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				finddlg.setVisible(true);
			}
		});
		
		northPanel.add(FilePathtextField, BorderLayout.CENTER);
		
		this.add(northPanel, BorderLayout.NORTH);
		this.add(ContentsviewPanel, BorderLayout.CENTER);
	}
	
	public void setData(ApkInfo apkinfo) {
		this.apkinfo = apkinfo;
		if(apkinfo != null) {
			this.resourcesWithValue = apkinfo.resourcesWithValue;
		} else {
			this.resourcesWithValue = null;
		}
	}

	private void initToolbar(JToolBar toolbar) {
		
		ToolbarActionListener toolbarListener = new ToolbarActionListener();
		String[] petStrings = { "XML", "ARSC"};
		
		JButton OpenBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_OPEN.getImageIcon(16, 16));
		JButton saveBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_SAVE.getImageIcon(16, 16));
		JButton FindBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		
		OpenBtn.setName(TEXTVIEWER_TOOLBAR_OPEN);
		saveBtn.setName(TEXTVIEWER_TOOLBAR_SAVE);
		FindBtn.setName(TEXTVIEWER_TOOLBAR_FIND);
		
		
		JTextField findtextField = new JTextField();
		Dimension size = findtextField.getPreferredSize();
		findtextField.setPreferredSize(new Dimension(size.width+100, size.height));
		JComboBox combobox = new JComboBox(petStrings);

		combobox.addActionListener(toolbarListener);
		findtextField.addActionListener(toolbarListener);
		
		OpenBtn.addActionListener(toolbarListener);
		saveBtn.addActionListener(toolbarListener);
		FindBtn.addActionListener(toolbarListener);
		
		toolbar.add(OpenBtn);
		toolbar.add(saveBtn);
		toolbar.add(findtextField);
		toolbar.add(FindBtn);
		toolbar.add(combobox);
		
		toolbar.setFloatable(false);
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		
	}
	class ToolbarActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getSource() instanceof JButton) {
				String name = ((JButton)arg0.getSource()).getName();
				switch(name) {
				case TEXTVIEWER_TOOLBAR_OPEN:
					Log.d("open");
					break;
				case TEXTVIEWER_TOOLBAR_SAVE:
					Log.d("save");
					break;
				case TEXTVIEWER_TOOLBAR_FIND:
					Log.d("find");
					finddlg.setVisible(true);
					break;
				}				
			} else if(arg0.getSource() instanceof JTextField) {
				String findstr = ((JTextField)(arg0.getSource())).getText();
				
				
				Log.d("find : " + findstr);
				
			} else if(arg0.getSource() instanceof JComboBox) {
				String fileType = ((JComboBox)(arg0.getSource())).getSelectedItem().toString();
				
				Log.d("fileType : " + fileType);
			}
		}		
	}
	
    public enum ButtonSet
    {
    	OS_SETTING			(0x01, Type.NORMAL, Resource.STR_LABEL_OPEN_WITH_SYSTEM.getString(), Resource.STR_LABEL_OPEN_WITH_SYSTEM.getString(), Resource.IMG_RESOURCE_TREE_OPEN_ICON.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	JD_GUI				(0x02, Type.NORMAL, Resource.STR_LABEL_OPEN_WITH_JDGUI.getString(), Resource.STR_LABEL_OPEN_WITH_JDGUI.getString(), Resource.IMG_RESOURCE_TREE_JD_ICON.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	APK_SCANNER			(0x04, Type.NORMAL, Resource.STR_LABEL_OPEN_WITH_SCANNER.getString(), Resource.STR_LABEL_OPEN_WITH_SCANNER.getString(), Resource.IMG_APP_ICON.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	EXPLORER			(0x08, Type.NORMAL, Resource.STR_LABEL_OPEN_WITH_EXPLORER.getString(), Resource.STR_LABEL_OPEN_WITH_EXPLORER.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	CHOOSE_APPLICATION	(0x10, Type.NORMAL, Resource.STR_LABEL_OPEN_WITH_CHOOSE.getString(), Resource.STR_LABEL_OPEN_WITH_CHOOSE.getString(), Resource.IMG_RESOURCE_TREE_OPEN_OTHERAPPLICATION_ICON.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize));

    	private enum Type {
    		NONE, NORMAL, HOVER, EXTEND
    	}
    	
    	static private final int IconSize = 80;

    	private Type type = null;
    	private String text = null;
    	private String toolTipText = null;
    	private ImageIcon icon = null;
    	//private ImageIcon hoverIcon = null;
    	private String actionCommand = null;
    	private int id = -1;
    	
    	ButtonSet(int id, Type type, String text, ImageIcon icon)
    	{
    		this(id, type, text, null, icon, icon);
    	}
    	
    	ButtonSet(int id, Type type, String text, String toolTipText, ImageIcon icon)
    	{
    		this(id, type, text, toolTipText, icon, icon);
    	}
    	
    	ButtonSet(int id, Type type, String text, String toolTipText, ImageIcon icon, ImageIcon hoverIcon)
    	{
    		this.id = id;
    		this.type = type;
    		this.text = text;
    		this.toolTipText = toolTipText;
    		this.icon = icon;
    		//this.hoverIcon = hoverIcon;
    		this.actionCommand = this.getClass().getName()+"."+this.toString();
    	}
    	
    	public boolean matchActionEvent(ActionEvent e)
    	{
    		return actionCommand.equals(e.getActionCommand());
    	}
    	
    	public int getButtonId() {
    		return id;
    	}

    	private JButton getButton(ActionListener listener)
    	{
    		final JButton button = new JButton(text, icon);
			button.setToolTipText(toolTipText);
			button.setOpaque(false);
			button.setBorderPainted(false);
			
    		switch(type) {
    		case NORMAL:
    			button.addActionListener(listener);
    			button.setVerticalTextPosition(JLabel.BOTTOM);
    			button.setHorizontalTextPosition(JLabel.CENTER);
    			//button.setFocusable(false);
    			button.setPreferredSize(new Dimension(120,120));
    			button.setContentAreaFilled(false);
    			button.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent evt) {
                    	button.setContentAreaFilled(true);
                    }
                    public void mouseExited(MouseEvent evt) {
                    	button.setContentAreaFilled(false);
                    }
                });
    			break;
    		case EXTEND:
    			button.setMargin(new Insets(27,0,27,0));
    			button.setFocusable(false);
    			break;
    		default:
    			return null;
    		}
			button.setActionCommand(actionCommand);
    		
    		return button;
    	}
    	
    	static private HashMap<ButtonSet, JButton> getButtonMap(ActionListener listener)
    	{
    		HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>();
            for(ButtonSet bs: values()) {
            	buttonMap.put(bs, bs.getButton(listener));
            }
            return buttonMap;
    	}
    }
	
	
	public class SelectViewPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = -5260902185163996992L;

		private HashMap<ButtonSet, JButton> buttonMap;
		private JLabel openWithLabel;

		public final static int SELECT_VIEW_ICON_OPEN = 0x01;
		public final static int SELECT_VIEW_ICON_JD_OPEN = 0x02;
		public final static int SELECT_VIEW_ICON_SCANNER_OPEN = 0x04;
		public final static int SELECT_VIEW_ICON_EXPLORER = 0x08;
		public final static int SELECT_VIEW_ICON_CHOOSE_APPLICATION = 0x10;

		public SelectViewPanel() {
			
			JLabel warringLabel = new JLabel(Resource.IMG_WARNING2.getImageIcon(80,80));
			
			JTextArea textArea = new JTextArea(Resource.STR_MSG_UNSUPPORTED_PREVIEW.getString());
			textArea.setEditable(false);
			
			openWithLabel = new JLabel(Resource.STR_LABEL_OPEN_WITH.getString());
			openWithLabel.setBorder(new EmptyBorder(20, 10, 0, 0));
			
			JPanel MessagePanel = new JPanel(new FlowLayout());
	        MessagePanel.add(warringLabel);
	        MessagePanel.add(textArea);
	        MessagePanel.setBorder(new EmptyBorder(40, 5, 0, 0));
	        MessagePanel.setBackground(Color.WHITE);
	        MessagePanel.setMaximumSize(MessagePanel.getPreferredSize());
	        MessagePanel.setAlignmentX(LEFT_ALIGNMENT);

			buttonMap = ButtonSet.getButtonMap(this);
			buttonMap.get(ButtonSet.JD_GUI).setDisabledIcon(Resource.IMG_RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon());
			
			JPanel IconPanel = new JPanel(new GridBagLayout());
			IconPanel.add(buttonMap.get(ButtonSet.OS_SETTING));
			IconPanel.add(buttonMap.get(ButtonSet.JD_GUI));
			IconPanel.add(buttonMap.get(ButtonSet.APK_SCANNER));
			IconPanel.add(buttonMap.get(ButtonSet.EXPLORER));
			IconPanel.add(buttonMap.get(ButtonSet.CHOOSE_APPLICATION));
	        IconPanel.setBackground(Color.WHITE);
	        IconPanel.setMaximumSize(MessagePanel.getPreferredSize());
	        IconPanel.setAlignmentX(LEFT_ALIGNMENT);
	        
	        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	        this.setBackground(Color.WHITE);
	        
			this.add(MessagePanel);
			this.add(openWithLabel);
			this.add(IconPanel);
		}
		
		public void setMenu(int Flag) {
			openWithLabel.setVisible(Flag != 0);
			for (ButtonSet key : buttonMap.keySet()) {
				buttonMap.get(key).setVisible((key.getButtonId() & Flag) != 0);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String resPath = apkinfo.tempWorkPath + File.separator + currentSelectedObj.path.replace("/", File.separator);
			ZipFileUtil.unZip(apkinfo.filePath, currentSelectedObj.path, resPath);
			
			if (ButtonSet.OS_SETTING.matchActionEvent(e)) {
				String openner;
				if(System.getProperty("os.name").indexOf("Window") >-1) {
					openner = "explorer";
				} else {  //for linux
					openner = "xdg-open";
				}
				try {
					new ProcessBuilder(openner, resPath).start();
				} catch (IOException e1) { }
			} else if (ButtonSet.JD_GUI.matchActionEvent(e)) {
				final JButton btn = buttonMap.get(ButtonSet.JD_GUI);
				btn.setEnabled(false);
				DexLuncher.openDex(resPath, new DexLuncher.DexWrapperListener() {
					@Override
					public void OnError() {}
					@Override
					public void OnSuccess() {
						btn.setEnabled(true);
					}
				});
			} else if (ButtonSet.APK_SCANNER.matchActionEvent(e)) {
				Launcher.run(resPath);
			} else if (ButtonSet.EXPLORER.matchActionEvent(e)) {
				try {
					if(System.getProperty("os.name").indexOf("Window") >-1) {
						new ProcessBuilder("explorer", resPath).start();
					} else {  //for linux
						new ProcessBuilder("file-roller", resPath).start();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if (ButtonSet.CHOOSE_APPLICATION.matchActionEvent(e)) {
				
			}
		}
		
	}
	
    private void setTextContentPanel(ResourceObject obj) {
    	String content = null;
    	
		switch(obj.attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {obj.path});
			AxmlToXml a2x = new AxmlToXml(xmlbuffer, resourcesWithValue);
			//StringBuilder sb = new StringBuilder();
			//for(String s: xmlbuffer) sb.append(s+"\n");
			content = a2x.toString();
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
				selectPanel.setMenu(0);
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
		
        ResourceObject CurrentresObj = null;
		if(node.getUserObject() instanceof ResourceObject) {
			CurrentresObj = (ResourceObject)node.getUserObject();
		}
		
		if(CurrentresObj != null && CurrentresObj == currentSelectedObj) {
			Log.v("select same object");
			Log.d("" +node.getPath().toString());
			return;
		}

		if(CurrentresObj == null || CurrentresObj.isFolder) {
			//htmlViewer.setText("");
			//((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_HTML_VIEWER);
			//FilePathtextField.setText("folder");
		} else {
			currentSelectedObj = CurrentresObj;
			switch(CurrentresObj.attr) {
			case ResourceObject.ATTR_IMG:
				drawImageOnPanel(CurrentresObj);
				break;
			case ResourceObject.ATTR_QMG:
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
