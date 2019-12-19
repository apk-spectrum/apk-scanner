package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

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

import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.action.ActionEventHandler;
import com.apkscanner.gui.component.ImageControlPanel;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AxmlToXml;
import com.apkscanner.tool.external.ImgExtractorWrapper;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class ResourceContentsPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -934921813626224616L;

	public static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
	public static final String CONTENT_SYNTAX_TEXT_VIEWER = "SyntaxTextViewer";
	public static final String CONTENT_TABLE_VIEWER = "TableViewer";
	public static final String CONTENT_SELECT_VIEWER = "SelectViewer";
	public static final String CONTENT_INIT_VIEWER = "InitViewer";

	private ResourceToolBarPanel resToolbar;

	private JTable textTableViewer;
	private JScrollPane textTableScroll;
	private SearchRenderer renderer;
	private ImageControlPanel imageViewerPanel;
	private JPanel contentsviewPanel;
	private SelectViewPanel selectPanel;
	private RSyntaxTextArea xmltextArea;

	private FindDialog finddlg;

	private ApkInfo apkInfo;
	private ResourceObject currentSelectedObj;

	private ActionListener listener;
	private String currentContentViewer;

	public ResourceContentsPanel(final ActionListener listener) {
		super(new BorderLayout());

		this.listener = listener;

		// CONTENT_IMAGE_VIEWER
		imageViewerPanel = new ImageControlPanel();

		// CONTENT_HTML_VIEWER
		xmltextArea  = new RSyntaxTextArea();
		//xmltextArea.createToolTip();
		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmltextArea.setCodeFoldingEnabled(true);
		xmltextArea.setMarkOccurrences(true);
		xmltextArea.setEditable(false);

		RTextScrollPane sp = new RTextScrollPane(xmltextArea);

		JPanel textAreaPanel = new JPanel(new BorderLayout());
		textAreaPanel.add(sp);

		ErrorStrip errorStrip = new ErrorStrip(xmltextArea);
		textAreaPanel.add(errorStrip,BorderLayout.LINE_END);

		// CONTENT_TABLE_VIEWER
		textTableViewer = new JTable();
		textTableViewer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		renderer = new SearchRenderer();
		textTableViewer.setDefaultRenderer(Object.class, renderer);

		textTableViewer.setShowHorizontalLines(false);
		textTableViewer.setTableHeader(null);
		textTableViewer.setCellSelectionEnabled(true);
		textTableViewer.setRowSelectionAllowed(true);
		textTableViewer.setColumnSelectionAllowed(false);
		textTableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		textTableScroll = new JScrollPane(textTableViewer);

		// CONTENT_SELECT_VIEWER
		if(listener instanceof ActionEventHandler) {
			((ActionEventHandler)listener).addAction(SelectViewPanel.ACT_CMD_OPEN_WITH_TEXTVIEWER, new AbstractAction() {
				private static final long serialVersionUID = 7778558784965803320L;
				@Override
				public void actionPerformed(ActionEvent e) {
					setTextContentPanel(currentSelectedObj, ResourceObject.ATTR_TXT);
					resToolbar.setVisibleTextTools(true);
					resToolbar.setVisibleAXmlTools(false);
				}
			});
		}
		selectPanel = new SelectViewPanel(listener);
		JScrollPane selectPanelScroll = new JScrollPane(selectPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		selectPanelScroll.setBorder(new EmptyBorder(0,0,0,0));

		contentsviewPanel = new JPanel(new CardLayout());
		contentsviewPanel.add(textAreaPanel, CONTENT_SYNTAX_TEXT_VIEWER);
		contentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		contentsviewPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
		contentsviewPanel.add(selectPanelScroll, CONTENT_SELECT_VIEWER);
		contentsviewPanel.add(new JPanel(), CONTENT_INIT_VIEWER);

		setContentViewPanel(CONTENT_INIT_VIEWER);

		// Toobar
		resToolbar = new ResourceToolBarPanel(this);
		JScrollPane northPanelScroll = new JScrollPane(resToolbar, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		northPanelScroll.setBorder(new EmptyBorder(0,0,0,0));

		add(northPanelScroll, BorderLayout.NORTH);
		add(contentsviewPanel, BorderLayout.CENTER);

		KeyStrokeAction.registerKeyStrokeAction(xmltextArea, KeyStroke.getKeyStroke("control F"), this);
		KeyStrokeAction.registerKeyStrokeAction(xmltextArea, KeyStroke.getKeyStroke("control S"), this);
		KeyStrokeAction.registerKeyStrokeAction(xmltextArea, KeyStroke.getKeyStroke("F3"), this);
		KeyStrokeAction.registerKeyStrokeAction(xmltextArea, KeyStroke.getKeyStroke("shift F3"), this);

		KeyStrokeAction.registerKeyStrokeAction(textTableViewer, KeyStroke.getKeyStroke("control F"), this);
		KeyStrokeAction.registerKeyStrokeAction(textTableViewer, KeyStroke.getKeyStroke("control S"), this);
		KeyStrokeAction.registerKeyStrokeAction(textTableViewer, KeyStroke.getKeyStroke("F3"), this);
		KeyStrokeAction.registerKeyStrokeAction(textTableViewer, KeyStroke.getKeyStroke("shift F3"), this);
	}

	public void setContentViewPanel(String viewName) {
		currentContentViewer = viewName;
		((CardLayout)contentsviewPanel.getLayout()).show(contentsviewPanel, viewName);
	}

	public void setData(ApkInfo apkInfo) {
		if(!Objects.equals(this.apkInfo, apkInfo)) {
			this.apkInfo = apkInfo;
			resToolbar.setToolbarPolicy(null);
			setContentViewPanel(CONTENT_INIT_VIEWER);
		}
	}

	private FindDialog getFindDialog() {
		if(finddlg == null) {
			JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
			finddlg = new FindDialog(window, new SearchListener() {
				@Override
				public void searchEvent(SearchEvent e) {
					searchAndNext(e.getType(), e.getSearchContext());
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

	private void findNextTable(boolean next) {

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

		String textFieldstr = resToolbar.getFindText().toLowerCase();

		boolean roop = false;
		int to;
		if(next){
			to = rowCount;
			for(i=selectindex+1; i < to; i++) {
				String str = ""+textTableViewer.getModel().getValueAt(i, 0);
				str = str.toLowerCase();

				if(str.contains(textFieldstr)) {
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
				if(str.contains(textFieldstr)) {
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

	private void searchAndNext(SearchEvent.Type type, SearchContext context) {
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
		}
		Log.d("Found : " + text);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if(evt.getSource() instanceof JComponent) {
			switch(evt.getActionCommand()) {
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_FIND:
				if(CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
					getFindDialog().setVisible(true);
				} else {
					String pattern = resToolbar.getFindText();
					renderer.setPattern(pattern);
					findNextTable(true);
					textTableViewer.repaint();
				}
				break;
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_NEXT:
				if(CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
					getFindDialog().getSearchContext().setSearchForward(true);
					searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
				} else {
					findNextTable(true);
				}
				break;
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_PREV:
				if(CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
					getFindDialog().getSearchContext().setSearchForward(false);
					searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
				} else {
					findNextTable(false);
				}
				break;
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_FIND_TEXTAREA:
				if(CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
					getFindDialog().getSearchContext().setSearchFor(resToolbar.getFindText());
					getFindDialog().getSearchContext().setSearchForward(true);
					searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
				} else {
					renderer.setPattern(resToolbar.getFindText());
					findNextTable(true);
					textTableViewer.repaint();
				}
				break;
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_VIEW_TYPE:
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_MULTI_LINE:
				setTextContentPanel(currentSelectedObj);
				break;
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_OPEN:
			case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_SAVE:
				if(currentSelectedObj == null) return;
				JComponent comp = (JComponent) evt.getSource();
				comp.putClientProperty(ResourceObject.class, currentSelectedObj);
				listener.actionPerformed(evt);
				break;
			}
		} else if(evt.getSource() instanceof KeyStrokeAction) {
			KeyStrokeAction keyAction = (KeyStrokeAction) evt.getSource();
			String keyStroke = KeyEvent.getKeyText( keyAction.getKeyStroke().getKeyCode() );
			String number = keyStroke.substring(0);
			int funcKey = keyAction.getModifiers();

			JComponent comp = keyAction.getComponent();
			switch (number) {
			case "F":	  /////////F key
				if(comp instanceof JTable) {
					resToolbar.setFocusFindTextField();
				} else {
					getFindDialog().setVisible(true);
				}

				break;
			case "S":	  /////////S key
				comp.putClientProperty(ResourceObject.class, currentSelectedObj);
				listener.actionPerformed(new ActionEvent(comp, ActionEvent.ACTION_PERFORMED,
						UiEventHandler.ACT_CMD_SAVE_RESOURCE_FILE, evt.getWhen(), evt.getModifiers()));
				break;
			case "F3":	  /////////F3
				if(CONTENT_TABLE_VIEWER.equals(currentContentViewer)) {
					if(funcKey==1) {
						findNextTable(false);
					} else {
						findNextTable(true);
					}
				} else {
					if(funcKey==1) {
						Log.d("shift F3 Key");
						getFindDialog().getSearchContext().setSearchForward(false);
						searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
					} else {
						Log.d("F3 Key");
						getFindDialog().getSearchContext().setSearchForward(true);
						searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
					}
				}
				break;
			default:
				Log.d("unknown");
				break;
			}
		}
	}

	public void selectContent(Object userData) {
		if(!(userData instanceof ResourceObject)) return;

		ResourceObject resObj = (ResourceObject) userData;

		if(resObj.isFolder) return;

		if(resObj.equals(currentSelectedObj)) {
			Log.v("select same object " + currentSelectedObj.toString());
			return;
		}
		currentSelectedObj = resObj;

		resToolbar.setToolbarPolicy(resObj);

		switch(resObj.attr) {
		case ResourceObject.ATTR_IMG:
			drawImageOnPanel(resObj);
			break;
		case ResourceObject.ATTR_AXML:
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
		case ResourceObject.ATTR_CERT:
		case ResourceObject.ATTR_FS_IMG:
			setTextContentPanel(resObj);
			break;
		case ResourceObject.ATTR_ETC:
			if("resources.arsc".equals(resObj.path)) {
				setTextTablePanel(resObj);
				break;
			}
		default:
			setSelectViewPanel(resObj);
			break;
		}
		revalidate();
	}

	private void drawImageOnPanel(ResourceObject resObj) {
		try {
			imageViewerPanel.setImage(apkInfo.filePath, resObj.path);
			imageViewerPanel.repaint();
			setContentViewPanel(CONTENT_IMAGE_VIEWER);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}

	private void setTextContentPanel(ResourceObject resObj) {
		setTextContentPanel(resObj, -1);
	}

	private void setTextContentPanel(ResourceObject resObj, int attr) {
		String content = null;

		if (attr == -1) attr = resObj.attr;

		switch(attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptNativeWrapper.Dump.getXmltree(apkInfo.filePath, new String[] {resObj.path});
			if(RConst.AXML_VEIWER_TYPE_XML.equals(RProp.S.AXML_VIEWER_TYPE.get())) {
				AxmlToXml a2x = new AxmlToXml(xmlbuffer, (apkInfo != null) ? apkInfo.resourceScanner : null);
				a2x.setMultiLinePrint(RProp.B.PRINT_MULTILINE_ATTR.get());
				content = a2x.toString();
			} else {
				StringBuilder sb = new StringBuilder();
				for(String s: xmlbuffer) sb.append(s+"\n");
				content = sb.toString();
			}
			break;
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
			byte[] buffer = null;
			if(resObj.type == ResourceType.LOCAL) {
				buffer = FileUtil.readData(resObj.path);
			} else {
				buffer = ZipFileUtil.readData(apkInfo.filePath, resObj.path);
			}
			if(buffer != null) {
				content = new String(buffer);
			}
			break;
		case ResourceObject.ATTR_CERT:
			try(ZipFile zipFile = new ZipFile(apkInfo.filePath)) {
				ZipEntry entry = zipFile.getEntry(resObj.path);
				if(entry != null) {
					try(InputStream is = zipFile.getInputStream(entry)) {
						SignatureReport sr = new SignatureReport(is);
						content = sr.toString();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case ResourceObject.ATTR_FS_IMG:
			String imgPath = apkInfo.tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
			if(!new File(imgPath).exists()) {
				ZipFileUtil.unZip(apkInfo.filePath, resObj.path, imgPath);
			}
			content = ImgExtractorWrapper.getSuperblockInfo(imgPath);
			content += ImgExtractorWrapper.getLsInfo(imgPath);
			break;
		default:
			content = "This type is unsupported by preview.";
		}

		xmltextArea.setSyntaxEditingStyle(getSyntaxStyle(resObj.path));
		xmltextArea.setText(content);
		xmltextArea.setCaretPosition(0);
		setContentViewPanel(CONTENT_SYNTAX_TEXT_VIEWER);
	}

	private void setTextTablePanel(ResourceObject resObj) {
		if(!"resources.arsc".equals(resObj.path))
			return;

		final String[] data = (apkInfo.resourcesWithValue != null)
				? apkInfo.resourcesWithValue : new String[] { "lodding..." };

		textTableViewer.setModel(new AbstractTableModel() {
			private static final long serialVersionUID = 4679744294449713522L;

			@Override
			public int getRowCount() {
				return data.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return data[rowIndex];
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				return true;
			}
		});
		setContentViewPanel(CONTENT_TABLE_VIEWER);
	}

	private void setSelectViewPanel(ResourceObject resObj) {
		selectPanel.setMenu(resObj);
		setContentViewPanel(CONTENT_SELECT_VIEWER);
	}

	private String getSyntaxStyle(String path) {
		String extension = path.replaceAll(".*/", "").replaceAll(".*\\.", ".");

		switch(extension.toLowerCase()) {
		case ".as": return SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
		case ".asm": return SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86;
		//case ".": return SyntaxConstants.SYNTAX_STYLE_BBCODE;
		case ".c": return SyntaxConstants.SYNTAX_STYLE_C;
		case ".cljs": return SyntaxConstants.SYNTAX_STYLE_CLOJURE;
		case ".cpp": return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
		case ".cs": return SyntaxConstants.SYNTAX_STYLE_CSHARP;
		case ".css": return SyntaxConstants.SYNTAX_STYLE_CSS;
		case ".d": return SyntaxConstants.SYNTAX_STYLE_D;
		case "dockerfile": return SyntaxConstants.SYNTAX_STYLE_DOCKERFILE;
		case ".dart": return SyntaxConstants.SYNTAX_STYLE_DART;
		case ".pas": return SyntaxConstants.SYNTAX_STYLE_DELPHI;
		case ".dtd": return SyntaxConstants.SYNTAX_STYLE_DTD;
		case ".f": return SyntaxConstants.SYNTAX_STYLE_FORTRAN;
		case ".groovy": return SyntaxConstants.SYNTAX_STYLE_GROOVY;
		case "hosts": return SyntaxConstants.SYNTAX_STYLE_HOSTS;
		case ".htaccess": return SyntaxConstants.SYNTAX_STYLE_HTACCESS;
		case ".html": case ".htm": return SyntaxConstants.SYNTAX_STYLE_HTML;
		case ".ini": return SyntaxConstants.SYNTAX_STYLE_INI;
		case ".java": return SyntaxConstants.SYNTAX_STYLE_JAVA;
		case ".js": return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
		case ".json": return SyntaxConstants.SYNTAX_STYLE_JSON;
		//case ".json": return SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
		case ".jsp": return SyntaxConstants.SYNTAX_STYLE_JSP;
		case ".tex": case ".ltx": return SyntaxConstants.SYNTAX_STYLE_LATEX;
		case ".less": return SyntaxConstants.SYNTAX_STYLE_LESS;
		case ".lisp": return SyntaxConstants.SYNTAX_STYLE_LISP;
		case ".lua": return SyntaxConstants.SYNTAX_STYLE_LUA;
		case ".mk": return SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
		case ".mxml": return SyntaxConstants.SYNTAX_STYLE_MXML;
		case ".nsi": return SyntaxConstants.SYNTAX_STYLE_NSIS;
		case ".p6": case ".pl": return SyntaxConstants.SYNTAX_STYLE_PERL;
		case ".php": return SyntaxConstants.SYNTAX_STYLE_PHP;
		case ".properties": return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
		case ".py": return SyntaxConstants.SYNTAX_STYLE_PYTHON;
		case ".rb": return SyntaxConstants.SYNTAX_STYLE_RUBY;
		case ".rbw": return SyntaxConstants.SYNTAX_STYLE_RUBY;
		case ".sas": return SyntaxConstants.SYNTAX_STYLE_SAS;
		case ".scala": return SyntaxConstants.SYNTAX_STYLE_SCALA;
		case ".sql": return SyntaxConstants.SYNTAX_STYLE_SQL;
		case ".tcl": return SyntaxConstants.SYNTAX_STYLE_TCL;
		case ".ts": return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
		case ".sh": return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
		case ".vb": return SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC;
		case ".bat": return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
		case ".xml": case ".xsd": case ".svg": return SyntaxConstants.SYNTAX_STYLE_XML;
		case ".yaml": return SyntaxConstants.SYNTAX_STYLE_YAML;
		default: return SyntaxConstants.SYNTAX_STYLE_NONE;
		}
	}

	public void selectContentAndLine(int line, String Findstr) {
		if("resources.arsc".equals(currentSelectedObj.path)) {
			textTableViewer.getSelectionModel().clearSelection();
			textTableViewer.getSelectionModel().addSelectionInterval(line, line);

			int maxscroolbar = textTableScroll.getVerticalScrollBar().getMaximum();
			int rowCount = textTableViewer.getRowCount();
			textTableScroll.getVerticalScrollBar().setValue((line*(maxscroolbar/rowCount)));
		} else {
			SearchContext context = new SearchContext();
			context.setMatchCase(false);
			context.setMarkAll(true);
			context.setSearchFor(Findstr);
			context.setWholeWord(false);

			org.fife.ui.rtextarea.SearchResult result = SearchEngine.find(xmltextArea, context);

			if (!result.wasFound()) {
				xmltextArea.setCaretPosition(0);
				SearchEngine.find(xmltextArea, context);
				Log.d("not found");
			}
		}
	}
}
