package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
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

import com.apkscanner.Launcher;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.tabpanels.Resources.ResourceObject;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.dex2jar.Dex2JarWrapper;
import com.apkscanner.tool.jd_gui.JDGuiLauncher;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.apkscanner.util.ZipFileUtil;

public class ResouceContentsPanel extends JPanel{
	private static final long serialVersionUID = -934921813626224616L;
	
	public static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
	public static final String CONTENT_HTML_VIEWER = "HtmlViewer";
	public static final String CONTENT_TABLE_VIEWER = "TableViewer";
	public static final String CONTENT_SELECT_VIEWER = "SelectViewer";
	public static final String CONTENT_INIT_VIEWER = "InitViewer";
	
	public static final String TEXTVIEWER_TOOLBAR_OPEN = "textviewer_toolbar_open";
	public static final String TEXTVIEWER_TOOLBAR_SAVE= "textviewer_toolbar_save";
	public static final String TEXTVIEWER_TOOLBAR_FIND = "textviewer_toolbar_find";
	public static final String TEXTVIEWER_TOOLBAR_NEXT = "textviewer_toolbar_next";
	public static final String TEXTVIEWER_TOOLBAR_PREV = "textviewer_toolbar_prev";
	public static final String TEXTVIEWER_TOOLBAR_FIND_TEXTAREA = "textviewer_toolbar_findtextarea";
	
	public static final String RESOURCE_LISTVIEW_TOOLBAR = "_resource_toolbar" ;
	
	
	private static final int VEIW_TYPE_XML = 0;
	private static final int VEIW_TYPE_ARSC = 1;
	
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
	private JTextField findtextField;
	private JTextField findtextField_ResourceTable;
	private ToolbarActionListener toolbarListener;
	private SearchRenderer renderer;
	
	private int axmlVeiwType;
	private boolean isMultiLinePrint;
	private JComboBox<String> resTypeCombobox;
	private JSeparator resTypeSep;
	private JToggleButton multiLinePrintButton;
	private JScrollPane textTableScroll;
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
		toolbarListener = new ToolbarActionListener();
		initToolbar(toolBar, toolbarListener, "");
		
		axmlVeiwType = VEIW_TYPE_XML;
		isMultiLinePrint = false;
		
		String[] petStrings = { "XML", "ARSC"};
		resTypeCombobox = new JComboBox<String>(petStrings);
		resTypeCombobox.addActionListener(toolbarListener);
		
		multiLinePrintButton = new JToggleButton(Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_INDENT.getImageIcon());
		multiLinePrintButton.addActionListener(toolbarListener);
		multiLinePrintButton.setFocusPainted(false);
		
		resTypeSep = getNewSeparator(JSeparator.VERTICAL, new Dimension(5,16));
		toolBar.add(resTypeSep);
		toolBar.add(resTypeCombobox);
		toolBar.add(multiLinePrintButton);
		
		TextAreaPanel.add(toolBar,BorderLayout.PAGE_START);

		textTableViewer = new JTable(); 
		
		renderer = new SearchRenderer();
		textTableViewer.setDefaultRenderer(Object.class, renderer);
		
		textTableViewer.setShowHorizontalLines(false);
		textTableViewer.setTableHeader(null);
		textTableViewer.setCellSelectionEnabled(true);
		textTableViewer.setRowSelectionAllowed(true);
		textTableViewer.setColumnSelectionAllowed(false);
		textTableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		
		
		textTableScroll = new JScrollPane(textTableViewer);

		JToolBar toolBar2 = new JToolBar("");
		initToolbar(toolBar2, toolbarListener, RESOURCE_LISTVIEW_TOOLBAR);
		
		JPanel textTablePanel = new JPanel(new BorderLayout());
		textTablePanel.add(toolBar2,BorderLayout.PAGE_START);
		textTablePanel.add(textTableScroll);
		
		setLayout(new BorderLayout());
		
		imageViewerPanel = new ImageControlPanel();
		
		selectPanel = new SelectViewPanel();
		
		JPanel initPanel = new JPanel();
		
		ContentsviewPanel = new JPanel(new CardLayout());
		ContentsviewPanel.add(TextAreaPanel, CONTENT_HTML_VIEWER);
		ContentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		ContentsviewPanel.add(textTablePanel, CONTENT_TABLE_VIEWER);
		ContentsviewPanel.add(selectPanel, CONTENT_SELECT_VIEWER);
		ContentsviewPanel.add(initPanel, CONTENT_INIT_VIEWER);
		
		((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_INIT_VIEWER);
		
		JPanel northPanel = new JPanel(new BorderLayout());
		FilePathtextField = new JTextField("");
		FilePathtextField.setEditable(false);
		FilePathtextField.setBackground(Color.WHITE);
		
		TextViewKeyInputAction keyInputListener = new TextViewKeyInputAction();
		
		ComponentkeyInput(xmltextArea,"control F", keyInputListener);
		ComponentkeyInput(xmltextArea,"control S", keyInputListener);
		ComponentkeyInput(xmltextArea,"F3", keyInputListener);
		ComponentkeyInput(xmltextArea,"shift F3", keyInputListener);
		
		ComponentkeyInput(textTableViewer,"control F", keyInputListener);
		ComponentkeyInput(textTableViewer,"control S", keyInputListener);
		ComponentkeyInput(textTableViewer,"F3", keyInputListener);
		ComponentkeyInput(textTableViewer,"shift F3", keyInputListener);
		
		ComponentkeyInput(findtextField_ResourceTable,"F3", keyInputListener);
		ComponentkeyInput(findtextField_ResourceTable,"shift F3", keyInputListener);
		
		
		northPanel.add(FilePathtextField, BorderLayout.CENTER);
		
		this.add(northPanel, BorderLayout.NORTH);
		this.add(ContentsviewPanel, BorderLayout.CENTER);
	}
	
	private FindDialog getFindDialog() {
		if(finddlg == null) {
			SwingUtilities.getRoot(this);
			JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
			finddlg = new FindDialog(window, new SearchListener() {			
				@Override
				public void searchEvent(SearchEvent e) {
					SearchAndNext(e.getType(), e.getSearchContext());
				}
				
				@Override
				public String getSelectedText() {
					return null;
				}
			});
			finddlg.setResizable(false);
		}
		return finddlg;
	}
	
	private void FindNextTable(boolean next) {
		
		int selectindex = textTableViewer.getSelectedRow();
		boolean findflag= false;
		
		if(selectindex== -1) selectindex = 0;
		
		textTableViewer.getSelectionModel().clearSelection();
		
		int maxscroolbar = textTableScroll.getVerticalScrollBar().getMaximum();
		int rowCount = textTableViewer.getRowCount(); 
		int i;
		
		
		if(selectindex > rowCount) {
			textTableViewer.getSelectionModel().addSelectionInterval(0, 0);
			return ;
		}
		
		if(selectindex < 0) {
			textTableViewer.getSelectionModel().addSelectionInterval(maxscroolbar, maxscroolbar);
			return ;
		}
		
		String textFieldstr = findtextField_ResourceTable.getText().toLowerCase();
		
		boolean roop = false;
		int to;
		if(next){
			to = rowCount;
			for(i=selectindex+1; i < to; i++) {
				String str = ""+textTableViewer.getModel().getValueAt(i, 0);
				str = str.toLowerCase();
				
				if(str.indexOf(textFieldstr) != -1) {
					textTableViewer.getSelectionModel().addSelectionInterval(i, i);
					findflag = true;
					break;
				}
				if(i== rowCount-1 && roop ==false) {
					roop = true;
					i = 0;
					to = selectindex-1;
				}
			}
		} else {
			to = 0;
			for(i=selectindex-1; i >= to; i--) {
				String str = ""+textTableViewer.getModel().getValueAt(i, 0);				
				if(str.indexOf(textFieldstr) != -1) {
					textTableViewer.getSelectionModel().addSelectionInterval(i, i);
					findflag = true;
					break;
				}
				if(i== 0 && roop ==false) {
					roop = true;
					i = rowCount;
					to = selectindex+1;
				}
			}
		}
		
		//Log.d(" i = " + i  + " max scrool = " + maxscroolbar + "rowCount : " + rowCount);
		if(!findflag) Log.d("Not Found");
		textTableScroll.getVerticalScrollBar().setValue((i*(maxscroolbar/rowCount)));
	}
	
	
	class TextViewKeyInputAction extends AbstractAction {

		private static final long serialVersionUID = 2157003820138446772L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
	        //EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
	        KeyEvent ke = (KeyEvent)EventQueue.getCurrentEvent();
	        String keyStroke = KeyEvent.getKeyText( ke.getKeyCode() );
	        String number = keyStroke.substring(0);
			int FuncKey = arg0.getModifiers(); 
			
			switch (number) {
			case "F":      /////////F key
				
				if(arg0.getSource() instanceof JTable) {					
					EventQueue.invokeLater( new Runnable(){ 
	                 public void run() {
	                	 findtextField_ResourceTable.requestFocusInWindow();	                	 
	                 	}
	                 });					
					//findtextField_ResourceTable.requestFocusInWindow();
				} else {
					getFindDialog().setVisible(true);
				}
				
				break;			
			case "S":      /////////S key
				toolbarListener.exportContent(ToolbarActionListener.EXPORT_TYPE_SAVE);
				break;
			case "F3":      /////////F3
				
				if(arg0.getSource() instanceof JTable || findtextField_ResourceTable.equals(arg0.getSource())) {
					if(FuncKey==1) {
						FindNextTable(false);
					} else {
						FindNextTable(true);
					}
				} else {				
					if(FuncKey==1) {
						Log.d("shift F3 Key");
						finddlg.getSearchContext().setSearchForward(false);
						SearchAndNext(SearchEvent.Type.FIND, finddlg.getSearchContext());
					} else {
						Log.d("F3 Key");
						finddlg.getSearchContext().setSearchForward(true);
						SearchAndNext(SearchEvent.Type.FIND, finddlg.getSearchContext());
					}
				}
				break;			
			default:
				Log.d("unknown");
				break;
			}
		}
	}
	
	private void ComponentkeyInput(JComponent component, String keyStrokeAndKey, AbstractAction abstractAction) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
        component.getInputMap().put(keyStroke, keyStrokeAndKey);
        component.getActionMap().put(keyStrokeAndKey, abstractAction);        
	}
	
	private void SearchAndNext(SearchEvent.Type type, SearchContext context) {		
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
			if(finddlg.getWrapSearch()) {						
				int offset = xmltextArea.getCaretPosition();
				if(context.getSearchForward()){
					xmltextArea.setCaretPosition(0);
					
				} else {
					xmltextArea.setCaretPosition(xmltextArea.getText().length());
				}
				result = SearchEngine.find(xmltextArea, context);
				
				if(result.wasFound()) {
					text = "Text found; occurrences marked: " + result.getMarkedCount();
				}
				else {
					text = "Text not found";
					xmltextArea.setCaretPosition(offset);
				}
			} else {
				text = "Text not found";
			}
		}
		Log.d("Found : " + text);
	}
	
	public void setData(ApkInfo apkinfo) {
		
		if(apkinfo == null || this.apkinfo != apkinfo) {
			FilePathtextField.setText("");
			((CardLayout)ContentsviewPanel.getLayout()).show(ContentsviewPanel, CONTENT_INIT_VIEWER);
		}
		
		this.apkinfo = apkinfo;
		if(apkinfo != null) {
			this.resourcesWithValue = apkinfo.resourcesWithValue;
		} else {
			this.resourcesWithValue = null;
		}
	}

    private JSeparator getNewSeparator(int orientation, Dimension size)
    {
        JSeparator separator = new JSeparator(orientation);
        separator.setPreferredSize(size);
    	return separator;
    }
    
	private void initToolbar(JToolBar toolbar, ToolbarActionListener toolbarListener, String Type) {
	
		JButton OpenBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_OPEN.getImageIcon(16, 16));
		JButton saveBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_SAVE.getImageIcon(16, 16));
		JButton FindBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		JButton NextBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_NEXT.getImageIcon(16, 16));
		JButton PrevBtn = new JButton("",Resource.IMG_RESOURCE_TEXTVIEWER_TOOLBAR_PREV.getImageIcon(16, 16));
		
		OpenBtn.setName(TEXTVIEWER_TOOLBAR_OPEN+Type);
		saveBtn.setName(TEXTVIEWER_TOOLBAR_SAVE+Type);
		FindBtn.setName(TEXTVIEWER_TOOLBAR_FIND+Type);
		NextBtn.setName(TEXTVIEWER_TOOLBAR_NEXT+Type);
		PrevBtn.setName(TEXTVIEWER_TOOLBAR_PREV+Type);
		
		OpenBtn.setFocusPainted(false);
		saveBtn.setFocusPainted(false);
		FindBtn.setFocusPainted(false);
		NextBtn.setFocusPainted(false);
		PrevBtn.setFocusPainted(false);
		
		JTextField tempfield = new JTextField();
		 
		
		if(Type.equals(RESOURCE_LISTVIEW_TOOLBAR)) {
			findtextField_ResourceTable = tempfield;
			findtextField_ResourceTable.setName(TEXTVIEWER_TOOLBAR_FIND_TEXTAREA+Type);
		} else {			
			findtextField = tempfield;
			findtextField.setName(TEXTVIEWER_TOOLBAR_FIND_TEXTAREA); 
		}
		Dimension size = tempfield.getPreferredSize();
		tempfield.setPreferredSize(new Dimension(size.width+100, size.height));
		tempfield.addActionListener(toolbarListener);
		
		tempfield.setFocusable(true);
		
		tempfield.addFocusListener(new FocusListener() {			
			@Override
			public void focusLost(FocusEvent arg0) {
				((JTextField)(arg0.getSource())).setBackground(new Color(255,255,255));					
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				((JTextField)(arg0.getSource())).setBackground(new Color(178,235,244));				
			}
		});
		
		OpenBtn.addActionListener(toolbarListener);
		saveBtn.addActionListener(toolbarListener);
		FindBtn.addActionListener(toolbarListener);
		NextBtn.addActionListener(toolbarListener);
		PrevBtn.addActionListener(toolbarListener);
		
		Dimension sepSize = new Dimension(5,16);
		
		toolbar.add(OpenBtn);
		toolbar.add(saveBtn);
		toolbar.add(getNewSeparator(JSeparator.VERTICAL, sepSize));
		toolbar.add(tempfield);
		toolbar.add(FindBtn);
		toolbar.add(PrevBtn);
		toolbar.add(NextBtn);
		
		toolbar.setFloatable(false);
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		
	}

	class ToolbarActionListener implements ActionListener {
		public static final int EXPORT_TYPE_OPEN = 0;
		public static final int EXPORT_TYPE_SAVE = 1;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(arg0.getSource() instanceof JButton) {
				String name = ((JButton)arg0.getSource()).getName();
				switch(name) {
				case TEXTVIEWER_TOOLBAR_OPEN:
				case TEXTVIEWER_TOOLBAR_OPEN+RESOURCE_LISTVIEW_TOOLBAR:	
					exportContent(EXPORT_TYPE_OPEN);
					break;
				case TEXTVIEWER_TOOLBAR_SAVE:
				case TEXTVIEWER_TOOLBAR_SAVE+RESOURCE_LISTVIEW_TOOLBAR:					
					exportContent(EXPORT_TYPE_SAVE);
					break;
				case TEXTVIEWER_TOOLBAR_FIND:					
					getFindDialog().setVisible(true);
					break;
				case TEXTVIEWER_TOOLBAR_FIND+RESOURCE_LISTVIEW_TOOLBAR:					
				    String pattern = findtextField_ResourceTable.getText().trim();
			    	renderer.setPattern(pattern);
			    	FindNextTable(true);
			    	textTableViewer.repaint();
					break;
				case TEXTVIEWER_TOOLBAR_NEXT+RESOURCE_LISTVIEW_TOOLBAR:					
					FindNextTable(true);					
					break;
				case TEXTVIEWER_TOOLBAR_PREV+RESOURCE_LISTVIEW_TOOLBAR:
					FindNextTable(false);
					break;
				case TEXTVIEWER_TOOLBAR_NEXT:
					finddlg.getSearchContext().setSearchForward(true);
					SearchAndNext(SearchEvent.Type.FIND, finddlg.getSearchContext());
					break;
				case TEXTVIEWER_TOOLBAR_PREV:
					finddlg.getSearchContext().setSearchForward(false);
					SearchAndNext(SearchEvent.Type.FIND, finddlg.getSearchContext());
					break;
				}				
			} else if(arg0.getSource() instanceof JTextField) {
				String findstr = ((JTextField)(arg0.getSource())).getText();
				String name = ((JTextField)(arg0.getSource())).getName();
				switch(name) {
				case TEXTVIEWER_TOOLBAR_FIND_TEXTAREA:
					finddlg.getSearchContext().setSearchFor(findstr);
					finddlg.getSearchContext().setSearchForward(true);
					SearchAndNext(SearchEvent.Type.FIND, finddlg.getSearchContext());
					break;
				case TEXTVIEWER_TOOLBAR_FIND_TEXTAREA+RESOURCE_LISTVIEW_TOOLBAR:
				    String pattern = findstr.trim();
			    	renderer.setPattern(pattern);
			    	FindNextTable(true);
			    	textTableViewer.repaint();
					break;
				}				
				
			} else if(arg0.getSource() instanceof JComboBox) {
				@SuppressWarnings("rawtypes")
				String fileType = ((JComboBox)(arg0.getSource())).getSelectedItem().toString();
				Log.d("fileType : " + fileType);
				if("XML".equals(fileType)) {
					axmlVeiwType = VEIW_TYPE_XML;
					multiLinePrintButton.setEnabled(true);
				} else if("ARSC".equals(fileType)) {
					axmlVeiwType = VEIW_TYPE_ARSC;
					multiLinePrintButton.setEnabled(false);
				}
				setTextContentPanel(currentSelectedObj);
			} else if(arg0.getSource() instanceof JToggleButton) {
				isMultiLinePrint = ((JToggleButton)(arg0.getSource())).isSelected();
				setTextContentPanel(currentSelectedObj);
			}
		}
		
	    public void exportContent(int type) {
	    	String resPath = null;
	    	File resFile = null;
	    	if(type == EXPORT_TYPE_OPEN) {
				resPath = apkinfo.tempWorkPath + File.separator + currentSelectedObj.path.replace("/", File.separator);
				resFile = new File(resPath);
	    	} else {
	    		resFile = getSaveFile(null, currentSelectedObj.path.replace("/", File.separator));
	    		if(resFile == null) return;
	    		resPath = resFile.getAbsolutePath(); 
	    	}

			if(!resFile.exists()) {
				if(!resFile.getParentFile().exists()) {
					if(FileUtil.makeFolder(resFile.getParentFile().getAbsolutePath())) {
						Log.i("sucess make folder : " + resFile.getParentFile().getAbsolutePath());
					}
				}
			}

			String[] convStrings = null;
			boolean convAxml2Xml = false;
			if(currentSelectedObj.attr == ResourceObject.ATTR_AXML) {
				convStrings = AaptNativeWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {currentSelectedObj.path});
				if(axmlVeiwType == VEIW_TYPE_XML) convAxml2Xml = true;
			} else if("resources.arsc".equals(currentSelectedObj.path)) {
				convStrings = resourcesWithValue;
				if(type == EXPORT_TYPE_OPEN) {
					resPath += ".txt";
				}
			} else {
				ZipFileUtil.unZip(apkinfo.filePath, currentSelectedObj.path, resPath);
			}
			
			if(convStrings != null) {
				String writeString = null;
				if(convAxml2Xml) {
					Log.i("conv AxmlToXml");
					AxmlToXml a2x = new AxmlToXml(convStrings, (apkinfo != null) ? apkinfo.resourceScanner : null);
					a2x.setMultiLinePrint(isMultiLinePrint);
					writeString = a2x.toString();
				} else {
					StringBuilder sb = new StringBuilder();
					for(String s: convStrings) sb.append(s+"\n");
					writeString = sb.toString();
				}
				try {
					FileWriter fw = new FileWriter(new File(resPath));
					fw.write(writeString);
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(type == EXPORT_TYPE_OPEN) {
				SystemUtil.openArchiveExplorer(resPath);
			}
	    }
	    
		public File getSaveFile(Component component, String defaultFilePath)
		{
			JFileChooser jfc = ApkFileChooser.getFileChooser((String)Resource.PROP_LAST_FILE_SAVE_PATH.getData(), JFileChooser.SAVE_DIALOG, new File(defaultFilePath));
			//jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(Resource.STR_LABEL_APK_FILE_DESC.getString(),"apk"));

			if(jfc.showSaveDialog(component) != JFileChooser.APPROVE_OPTION)
				return null;

			File dir = jfc.getSelectedFile();
			if(dir != null) {
				Resource.PROP_LAST_FILE_SAVE_PATH.setData(dir.getParentFile().getAbsolutePath());
			}
			return dir;
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
			buttonMap.get(ButtonSet.CHOOSE_APPLICATION).setVisible(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			String resPath = apkinfo.tempWorkPath + File.separator + currentSelectedObj.path.replace("/", File.separator);
			ZipFileUtil.unZip(apkinfo.filePath, currentSelectedObj.path, resPath);
			
			if (ButtonSet.OS_SETTING.matchActionEvent(e)) {
				SystemUtil.openFile(resPath);
			} else if (ButtonSet.JD_GUI.matchActionEvent(e)) {
				final JButton btn = buttonMap.get(ButtonSet.JD_GUI);
				btn.setEnabled(false);
				Dex2JarWrapper.convert(resPath, new Dex2JarWrapper.DexWrapperListener() {
					@Override
					public void onError(String message) {
					}
					@Override
					public void onSuccess(String jarFilePath) {
						JDGuiLauncher.run(jarFilePath);
					}
					@Override
					public void onCompleted() {
						btn.setEnabled(true);						
					}
				});
			} else if (ButtonSet.APK_SCANNER.matchActionEvent(e)) {
				Launcher.run(resPath);
			} else if (ButtonSet.EXPLORER.matchActionEvent(e)) {
				SystemUtil.openArchiveExplorer(resPath);
			} else if (ButtonSet.CHOOSE_APPLICATION.matchActionEvent(e)) {
				
			}
		}
	}

	private String getSyntaxStyle(String extension) {
		switch(extension.toLowerCase()) {
		case ".as": return SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
		case ".asm": return SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_BBCODE;
		case ".c": return SyntaxConstants.SYNTAX_STYLE_C;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_CLOJURE;
		case ".cpp": return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_CSHARP;
		case ".css": return SyntaxConstants.SYNTAX_STYLE_CSS;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_D;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_DART;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_DELPHI;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_DTD;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_FORTRAN;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_GROOVY;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_HTACCESS;
		case ".html": return SyntaxConstants.SYNTAX_STYLE_HTML;
		case ".java": return SyntaxConstants.SYNTAX_STYLE_JAVA;
		case ".js": return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_JSHINTRC;
		case ".json": return SyntaxConstants.SYNTAX_STYLE_JSON;
		case ".jsp": return SyntaxConstants.SYNTAX_STYLE_JSP;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_LATEX;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_LISP;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_LUA;
		case ".mk": return SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_MXML;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_NSIS;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_PERL;
		case ".php": return SyntaxConstants.SYNTAX_STYLE_PHP;
		case ".properties": return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_PYTHON;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_RUBY;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_SAS;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_SCALA;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_SQL;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_TCL;
		case ".sh": return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
		case ".vb": return SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC;
		case ".bat": return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
		case ".xml": return SyntaxConstants.SYNTAX_STYLE_XML;
		default: return SyntaxConstants.SYNTAX_STYLE_NONE;
		}
	}
	
    private void setTextContentPanel(ResourceObject obj) {
    	String content = null;
    	
		switch(obj.attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptNativeWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {obj.path});
			resTypeSep.setVisible(true);
			resTypeCombobox.setVisible(true);
			multiLinePrintButton.setVisible(true);
			if(axmlVeiwType == VEIW_TYPE_XML) {
				AxmlToXml a2x = new AxmlToXml(xmlbuffer, (apkinfo != null) ? apkinfo.resourceScanner : null);
				a2x.setMultiLinePrint(isMultiLinePrint);
				content = a2x.toString();
			} else {
				StringBuilder sb = new StringBuilder();
				for(String s: xmlbuffer) sb.append(s+"\n");
				content = sb.toString();
			}
			break;
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
			resTypeSep.setVisible(false);
			resTypeCombobox.setVisible(false);
			multiLinePrintButton.setVisible(false);
			ZipFile zipFile = null;
			InputStream is = null;
			try {
				zipFile = new ZipFile(apkinfo.filePath);
				ZipEntry entry = zipFile.getEntry(obj.path);
				byte[] buffer = new byte[(int) entry.getSize()];
				is = zipFile.getInputStream(entry);
				is.read(buffer);
				is.close();
				content = new String(buffer);
				zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case ResourceObject.ATTR_CERT:
			Double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
			String keytoolPackage;
			if(javaVersion >= 1.8) {
				keytoolPackage = "sun.security.tools.keytool.Main";
			} else {
				keytoolPackage = "sun.security.tools.KeyTool";
			}
			String filePath = apkinfo.tempWorkPath + File.separator + obj.path;
			String[] cmd = {"java","-Dfile.encoding=utf8",keytoolPackage,"-printcert","-v","-file", filePath};
			String[] result = ConsolCmd.exc(cmd, false, null);
			StringBuilder sb = new StringBuilder();
			for(String s: result) sb.append(s+"\n");
			content = sb.toString();
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

			xmltextArea.setSyntaxEditingStyle(getSyntaxStyle(obj.path.replaceAll(".*/", "").replaceAll(".*\\.", ".")));
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
			case ResourceObject.ATTR_CERT:
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
