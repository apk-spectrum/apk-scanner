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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.apkscanner.DexLuncher;
import com.apkscanner.Launcher;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.apkscanner.AxmlToXml;
import com.apkscanner.core.AaptWrapper;
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
	
	//JHtmlEditorPane htmlViewer;
	private JTable textTableViewer;
	private ImageControlPanel imageViewerPanel;
	private ResourceObject currentSelectedObj = null;
	private String[] resourcesWithValue = null;
	private JPanel ContentsviewPanel;
	private JTextField FilePathtextField;	
	private SelectViewPanel selectPanel;
	private ResourceObject CurrentresObj = null;
	private ApkInfo apkinfo;
	private RSyntaxTextArea xmltextArea;
	
	public ResouceContentsPanel() {
		
		CustomLabel temptextarea = new CustomLabel();
		temptextarea.createToolTip();
		xmltextArea  = (RSyntaxTextArea)temptextarea;
		//xmltextArea.createToolTip();
				
		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);				
		xmltextArea.setEditable(false);		
		RTextScrollPane sp = new RTextScrollPane(xmltextArea);
		
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
		
		ContentsviewPanel.add(sp, CONTENT_HTML_VIEWER);
		ContentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		ContentsviewPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
		ContentsviewPanel.add(selectPanel, CONTENT_SELECT_VIEWER);
		
		FilePathtextField = new JTextField("FilePath");
		FilePathtextField.setEditable(false);
		
		this.add(FilePathtextField, BorderLayout.NORTH);
		this.add(ContentsviewPanel, BorderLayout.CENTER);
	}
	
	public void setData(ApkInfo apkinfo) {
		this.apkinfo = apkinfo;
		this.resourcesWithValue = apkinfo.resourcesWithValue;
	}

    private class CustomLabel extends RSyntaxTextArea {
		private static final long serialVersionUID = 4552614645476575656L;

		private CustomTooltip m_tooltip;
        
        @Override public JToolTip createToolTip() {
            if (m_tooltip == null) {
                m_tooltip = new CustomTooltip();
                m_tooltip.setComponent(this);
                Log.d("createTool");
            }
            return m_tooltip;
        }
        public String getToolTipText(MouseEvent e) {
        	String str = this.getText();
        	String selectedstr;
        	int startindex = this.getSelectionStart();
        	int endindex = this.getSelectionEnd();
        	int i;
        	//Log.d(startindex + ": " + endindex);
        	
        	
        	if(endindex - startindex<3) {
        		return null;
        	}
        	
        	for(i=startindex; str.charAt(i) != "\"".toCharArray()[0]; i--) {
        		//Log.d(i+"" +str.charAt(i));
        		if(startindex-i > 20) break;
        	}
        	startindex = i+1;
        	
        	for(i=endindex; str.charAt(i) != "\"".toCharArray()[0]; i++) {
        		if(i-endindex > 20) break;
        	}
        	endindex = i;
        	this.setSelectionStart(startindex);
        	this.setSelectionEnd(endindex);        	
        	
        	selectedstr = this.getSelectedText();
        	
        	if(selectedstr.startsWith("@drawable/")) {
        		
        		selectedstr = selectedstr.substring("@drawable/".length());
        		
        		String ImagefilePath = ImageResource.getTreeFocuschanger().getImagefilePath(selectedstr);        		
        		
        		ImagefilePath = "jar:file:"+apkinfo.filePath.replaceAll("#", "%23")+"!/" + ImagefilePath;
        		Log.d(ImagefilePath);
        		if (m_tooltip != null) {
        			try {
						m_tooltip.setImage(new ImageIcon( new URL(ImagefilePath)));
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
        		}        		
        	} else {
        		if (m_tooltip != null) {
        			m_tooltip.setImage(null);
        		}
        	}
        	Log.d("returnstr");
        	return selectedstr;        	
        }
    }
	
    private static class CustomTooltip extends JToolTip {
		private static final long serialVersionUID = -1891800310718474313L;
		private JLabel m_label;
        private JLabel text_label;
        //private JButton m_button;
        private JPanel m_panel;
        private ImageIcon img;
        public CustomTooltip() {
            super();
            text_label = new JLabel();
            m_label = new JLabel();
            //m_button = new JButton("See, I am a button!");
            m_panel = new JPanel(new BorderLayout());
            m_panel.add(BorderLayout.NORTH, text_label);
            m_panel.add(BorderLayout.CENTER, m_label);
            //m_panel.add(BorderLayout.SOUTH, m_button);
            setLayout(new BorderLayout());
            //m_panel.setPreferredSize(new Dimension(200,100));
            
            add(m_panel);
        }

        @Override public Dimension getPreferredSize() {
            return m_panel.getPreferredSize();
        }

        @Override public void setTipText(String tipText) {
        	Log.d("setTip");
            if (tipText != null && !tipText.isEmpty()) {
                //m_label.setText(tipText);
                text_label.setText(tipText);                
                Log.d(""+this.img);                
                m_label.setIcon(this.img);
                
                
                
                this.repaint();                
            } else {
                super.setTipText(tipText);
            }
        }
        public void setImage(ImageIcon img) {
        	this.img = img;
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

		public final static int SELECT_VIEW_ICON_OPEN = 0x01;
		public final static int SELECT_VIEW_ICON_JD_OPEN = 0x02;
		public final static int SELECT_VIEW_ICON_SCANNER_OPEN = 0x04;
		public final static int SELECT_VIEW_ICON_EXPLORER = 0x08;
		public final static int SELECT_VIEW_ICON_CHOOSE_APPLICATION = 0x10;

		public SelectViewPanel() {
			
			JLabel warringLabel = new JLabel(Resource.IMG_WARNING2.getImageIcon(80,80));
			
			JTextArea textArea = new JTextArea(Resource.STR_MSG_UNSUPPORTED_PREVIEW.getString());
			textArea.setEditable(false);
			
			JLabel openWithLabel = new JLabel(Resource.STR_LABEL_OPEN_WITH.getString());
			openWithLabel.setBorder(new EmptyBorder(20, 10, 0, 0));
			
			JPanel MessagePanel = new JPanel(new FlowLayout());
	        MessagePanel.add(warringLabel);
	        MessagePanel.add(textArea);
	        MessagePanel.setBorder(new EmptyBorder(40, 0, 0, 0));
	        MessagePanel.setBackground(Color.WHITE);
	        MessagePanel.setMaximumSize(MessagePanel.getPreferredSize());
	        MessagePanel.setAlignmentX(LEFT_ALIGNMENT);

			buttonMap = ButtonSet.getButtonMap(this);
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
			for (ButtonSet key : buttonMap.keySet()) {
				buttonMap.get(key).setVisible((key.getButtonId() & Flag) != 0);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String resPath = apkinfo.tempWorkPath + File.separator + CurrentresObj.path.replace("/", File.separator);
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
				btn.setDisabledIcon(Resource.IMG_RESOURCE_TREE_OPEN_JD_LOADING.getImageIcon());
				btn.setEnabled(false);
				DexLuncher.openDex(resPath, new DexLuncher.DexWrapperListener() {
					@Override
					public void OnError() {}
					@Override
					public void OnSuccess() {
						btn.setDisabledIcon(null);										
						//btn.setIcon(Resource.IMG_RESOURCE_TREE_JD_ICON.getImageIcon(100,100));
						btn.setEnabled(true);
					}
				});
			} else if (ButtonSet.APK_SCANNER.matchActionEvent(e)) {
				Launcher.run(resPath);
			} else if (ButtonSet.EXPLORER.matchActionEvent(e)) {
				
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
