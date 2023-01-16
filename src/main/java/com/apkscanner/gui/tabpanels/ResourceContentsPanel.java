package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Objects;

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

import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.resource.RConst;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ActionEventHandler;
import com.apkspectrum.swing.ImageControlPanel;
import com.apkspectrum.swing.KeyStrokeAction;

public class ResourceContentsPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = -934921813626224616L;

    public static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
    public static final String CONTENT_SYNTAX_TEXT_VIEWER = "SyntaxTextViewer";
    public static final String CONTENT_TABLE_VIEWER = "TableViewer";
    public static final String CONTENT_SELECT_VIEWER = "SelectViewer";
    public static final String CONTENT_INIT_VIEWER = "InitViewer";

    protected static final String ACT_CMD_FIND = "ACT_CMD_FIND";
    protected static final String ACT_CMD_FIND_NEXT = "ACT_CMD_FIND_NEXT";
    protected static final String ACT_CMD_FIND_PREV = "ACT_CMD_FIND_PREV";
    protected static final String ACT_CMD_SAVE = "ACT_CMD_SAVE";

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
    private TreeNodeData currentSelectedObj;

    private ActionListener listener;
    private String currentContentViewer;

    public ResourceContentsPanel(final ActionListener listener) {
        super(new BorderLayout());

        this.listener = listener;

        // CONTENT_IMAGE_VIEWER
        imageViewerPanel = new ImageControlPanel();

        // CONTENT_HTML_VIEWER
        xmltextArea = new RSyntaxTextArea();
        // xmltextArea.createToolTip();
        xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        xmltextArea.setCodeFoldingEnabled(true);
        xmltextArea.setMarkOccurrences(true);
        xmltextArea.setEditable(false);

        RTextScrollPane sp = new RTextScrollPane(xmltextArea);

        JPanel textAreaPanel = new JPanel(new BorderLayout());
        textAreaPanel.add(sp);

        ErrorStrip errorStrip = new ErrorStrip(xmltextArea);
        textAreaPanel.add(errorStrip, BorderLayout.LINE_END);

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
        if (listener instanceof ActionEventHandler) {
            ((ActionEventHandler) listener).addActionListener(
                    SelectViewPanel.ACT_CMD_OPEN_WITH_TEXTVIEWER, new AbstractAction() {
                        private static final long serialVersionUID = 7778558784965803320L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setTextContentPanel(currentSelectedObj);
                            resToolbar.setVisibleTextTools(true);
                            resToolbar.setVisibleAXmlTools(false);
                        }
                    });
        }
        selectPanel = new SelectViewPanel(listener);
        JScrollPane selectPanelScroll = new JScrollPane(selectPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectPanelScroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        contentsviewPanel = new JPanel(new CardLayout());
        contentsviewPanel.add(textAreaPanel, CONTENT_SYNTAX_TEXT_VIEWER);
        contentsviewPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
        contentsviewPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
        contentsviewPanel.add(selectPanelScroll, CONTENT_SELECT_VIEWER);
        contentsviewPanel.add(new JPanel(), CONTENT_INIT_VIEWER);

        setContentViewPanel(CONTENT_INIT_VIEWER);

        // Toobar
        resToolbar = new ResourceToolBarPanel(this);
        resToolbar.setVisibleSaveTools(listener != null);
        JScrollPane northPanelScroll = new JScrollPane(resToolbar,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        northPanelScroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        add(northPanelScroll, BorderLayout.NORTH);
        add(contentsviewPanel, BorderLayout.CENTER);

        KeyStrokeAction.registerKeyStrokeAction(xmltextArea,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, RConst.CTRL_MASK), ACT_CMD_FIND, this);
        KeyStrokeAction.registerKeyStrokeAction(xmltextArea,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, RConst.CTRL_MASK), ACT_CMD_SAVE, this);
        KeyStrokeAction.registerKeyStrokeAction(xmltextArea,
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), ACT_CMD_FIND_NEXT, this);
        KeyStrokeAction.registerKeyStrokeAction(xmltextArea,
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, RConst.SHIFT_MASK), ACT_CMD_FIND_PREV, this);

        KeyStrokeAction.registerKeyStrokeAction(textTableViewer,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, RConst.CTRL_MASK), ACT_CMD_FIND, this);
        KeyStrokeAction.registerKeyStrokeAction(textTableViewer,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, RConst.CTRL_MASK), ACT_CMD_SAVE, this);
        KeyStrokeAction.registerKeyStrokeAction(textTableViewer,
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), ACT_CMD_FIND_NEXT, this);
        KeyStrokeAction.registerKeyStrokeAction(textTableViewer,
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, RConst.SHIFT_MASK), ACT_CMD_FIND_PREV, this);
    }

    public void setContentViewPanel(String viewName) {
        currentContentViewer = viewName;
        ((CardLayout) contentsviewPanel.getLayout()).show(contentsviewPanel, viewName);
    }

    public void setData(ApkInfo apkInfo) {
        if (!Objects.equals(this.apkInfo, apkInfo)) {
            this.apkInfo = apkInfo;
            resToolbar.setToolbarPolicy(null);
            setContentViewPanel(CONTENT_INIT_VIEWER);
        }
    }

    private FindDialog getFindDialog() {
        if (finddlg == null) {
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
        boolean findflag = false;

        if (selectindex == -1) selectindex = 0;

        textTableViewer.getSelectionModel().clearSelection();

        int maxscroolbar = textTableScroll.getVerticalScrollBar().getMaximum();
        int rowCount = textTableViewer.getRowCount();
        int i;


        if (selectindex > rowCount) {
            textTableViewer.getSelectionModel().addSelectionInterval(0, 0);
            return;
        }

        if (selectindex < 0) {
            textTableViewer.getSelectionModel().addSelectionInterval(maxscroolbar, maxscroolbar);
            return;
        }

        String textFieldstr = resToolbar.getFindText().toLowerCase();

        boolean roop = false;
        int to;
        if (next) {
            to = rowCount;
            for (i = selectindex + 1; i < to; i++) {
                String str = "" + textTableViewer.getModel().getValueAt(i, 0);
                str = str.toLowerCase();

                if (str.contains(textFieldstr)) {
                    textTableViewer.getSelectionModel().addSelectionInterval(i, i);
                    findflag = true;
                    break;
                }
                if (i == rowCount - 1 && roop == false) {
                    roop = true;
                    i = 0;
                    to = selectindex - 1;
                }
            }
        } else {
            to = 0;
            for (i = selectindex - 1; i >= to; i--) {
                String str = "" + textTableViewer.getModel().getValueAt(i, 0);
                if (str.contains(textFieldstr)) {
                    textTableViewer.getSelectionModel().addSelectionInterval(i, i);
                    findflag = true;
                    break;
                }
                if (i == 0 && roop == false) {
                    roop = true;
                    i = rowCount;
                    to = selectindex + 1;
                }
            }
        }

        // Log.d(" i = " + i + " max scrool = " + maxscroolbar + "rowCount : " + rowCount);
        if (!findflag) Log.d("Not Found");
        textTableScroll.getVerticalScrollBar().setValue((i * (maxscroolbar / rowCount)));
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
        } else if (type == SearchEvent.Type.MARK_ALL) {
            if (result.getMarkedCount() > 0) {
                text = "Occurrences marked: " + result.getMarkedCount();
            } else {
                text = "";
            }
        } else {
            int offset = xmltextArea.getCaretPosition();
            if (context.getSearchForward()) {
                xmltextArea.setCaretPosition(0);

            } else {
                xmltextArea.setCaretPosition(xmltextArea.getText().length());
            }
            result = SearchEngine.find(xmltextArea, context);

            if (result.wasFound()) {
                text = "Text found; occurrences marked: " + result.getMarkedCount();
            } else {
                text = "Text not found";
                xmltextArea.setCaretPosition(offset);
            }
        }
        Log.d("Found : " + text);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String actCmd = evt.getActionCommand();
        if (evt.getSource() instanceof JComponent) {
            switch (actCmd) {
                case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_FIND:
                    if (CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
                        getFindDialog().setVisible(true);
                    } else {
                        String pattern = resToolbar.getFindText();
                        renderer.setPattern(pattern);
                        findNextTable(true);
                        textTableViewer.repaint();
                    }
                    break;
                case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_NEXT:
                    if (CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
                        getFindDialog().getSearchContext().setSearchForward(true);
                        searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
                    } else {
                        findNextTable(true);
                    }
                    break;
                case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_PREV:
                    if (CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
                        getFindDialog().getSearchContext().setSearchForward(false);
                        searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
                    } else {
                        findNextTable(false);
                    }
                    break;
                case ResourceToolBarPanel.TEXTVIEWER_TOOLBAR_FIND_TEXTAREA:
                    if (CONTENT_SYNTAX_TEXT_VIEWER.equals(currentContentViewer)) {
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
                    if (currentSelectedObj == null || listener == null) return;
                    JComponent comp = (JComponent) evt.getSource();
                    comp.putClientProperty(TreeNodeData.class, currentSelectedObj);
                    listener.actionPerformed(evt);
                    break;
            }
        } else if (evt.getSource() instanceof KeyStrokeAction) {
            KeyStrokeAction keyAction = (KeyStrokeAction) evt.getSource();
            JComponent comp = keyAction.getComponent();
            switch (actCmd) {
                case ACT_CMD_FIND: ///////// F key
                    if (comp instanceof JTable) {
                        resToolbar.setFocusFindTextField();
                    } else {
                        getFindDialog().setVisible(true);
                    }
                    break;
                case ACT_CMD_SAVE: ///////// S key
                    if (listener == null) return;
                    comp.putClientProperty(TreeNodeData.class, currentSelectedObj);
                    listener.actionPerformed(new ActionEvent(comp, ActionEvent.ACTION_PERFORMED,
                            UiEventHandler.ACT_CMD_SAVE_RESOURCE_FILE, evt.getWhen(),
                            evt.getModifiers()));
                    break;
                case ACT_CMD_FIND_NEXT: ///////// F3
                    if (CONTENT_TABLE_VIEWER.equals(currentContentViewer)) {
                        findNextTable(true);
                    } else {
                        Log.d("F3 Key");
                        getFindDialog().getSearchContext().setSearchForward(true);
                        searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
                    }
                    break;
                case ACT_CMD_FIND_PREV: ///////// F3
                    if (CONTENT_TABLE_VIEWER.equals(currentContentViewer)) {
                        findNextTable(false);
                    } else {
                        getFindDialog().getSearchContext().setSearchForward(false);
                        searchAndNext(SearchEvent.Type.FIND, getFindDialog().getSearchContext());
                    }
                    break;
                default:
                    Log.d("unknown");
                    break;
            }
        }
    }

    public void selectContent(Object userData) {
        if (!(userData instanceof TreeNodeData)) return;

        TreeNodeData resObj = (TreeNodeData) userData;

        if (resObj.isFolder()) return;

        if (resObj.equals(currentSelectedObj)) {
            Log.v("select same object " + currentSelectedObj.toString());
            return;
        }
        currentSelectedObj = resObj;

        resToolbar.setToolbarPolicy(resObj);

        switch (resObj.getDataType()) {
            case TreeNodeData.DATA_TYPE_IMAGE:
                drawImageOnPanel(resObj);
                break;
            case TreeNodeData.DATA_TYPE_TEXT:
            case TreeNodeData.DATA_TYPE_CERTIFICATION:
                setTextContentPanel(resObj);
                break;
            case TreeNodeData.DATA_TYPE_UNKNOWN:
            default:
                switch (resObj.getExtension()) {
                    case ".arsc":
                        setTextTablePanel(resObj);
                        break;
                    case ".img":
                        setTextContentPanel(resObj);
                        break;
                    default:
                        setSelectViewPanel(resObj);
                }
                break;
        }
        revalidate();
    }

    private void drawImageOnPanel(TreeNodeData resObj) {
        if (resObj.getDataType() != TreeNodeData.DATA_TYPE_IMAGE) return;
        Object data = resObj.getData();
        if (data instanceof Image) {
            imageViewerPanel.setImage((Image) data);
            setContentViewPanel(CONTENT_IMAGE_VIEWER);
        }
    }

    private void setTextContentPanel(TreeNodeData resObj) {
        Object data = null;
        if (resObj instanceof ResourceObject) {
            data = ((ResourceObject) resObj).getData(apkInfo.a2xConvert);
        } else {
            data = resObj.getData();
        }

        String content = null;
        if (data != null) {
            if (data instanceof byte[]) {
                content = new String((byte[]) data);
            } else {
                content = data.toString();
            }
        } else {
            content = "This type is unsupported by preview.";
        }

        xmltextArea.setSyntaxEditingStyle(getSyntaxStyle(resObj.getExtension()));
        xmltextArea.setText(content);
        xmltextArea.setCaretPosition(0);
        setContentViewPanel(CONTENT_SYNTAX_TEXT_VIEWER);
    }

    private void setTextTablePanel(TreeNodeData resObj) {
        if (!"resources.arsc".equals(resObj.getPath())) return;

        final String[] data = (apkInfo.resourcesWithValue != null) ? apkInfo.resourcesWithValue
                : new String[] {"lodding..."};

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

    private void setSelectViewPanel(TreeNodeData resObj) {
        selectPanel.setMenu(resObj);
        setContentViewPanel(CONTENT_SELECT_VIEWER);
    }

    private String getSyntaxStyle(String suffix) {
        switch (suffix.toLowerCase()) {
            case ".as":
                return SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT;
            case ".asm":
                return SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86;
            // case ".": return SyntaxConstants.SYNTAX_STYLE_BBCODE;
            case ".c":
                return SyntaxConstants.SYNTAX_STYLE_C;
            case ".cljs":
                return SyntaxConstants.SYNTAX_STYLE_CLOJURE;
            case ".cpp":
                return SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case ".cs":
                return SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case ".css":
                return SyntaxConstants.SYNTAX_STYLE_CSS;
            case ".d":
                return SyntaxConstants.SYNTAX_STYLE_D;
            case "dockerfile":
                return SyntaxConstants.SYNTAX_STYLE_DOCKERFILE;
            case ".dart":
                return SyntaxConstants.SYNTAX_STYLE_DART;
            case ".pas":
                return SyntaxConstants.SYNTAX_STYLE_DELPHI;
            case ".dtd":
                return SyntaxConstants.SYNTAX_STYLE_DTD;
            case ".f":
                return SyntaxConstants.SYNTAX_STYLE_FORTRAN;
            case ".groovy":
                return SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case "hosts":
                return SyntaxConstants.SYNTAX_STYLE_HOSTS;
            case ".htaccess":
                return SyntaxConstants.SYNTAX_STYLE_HTACCESS;
            case ".html":
            case ".htm":
                return SyntaxConstants.SYNTAX_STYLE_HTML;
            case ".ini":
                return SyntaxConstants.SYNTAX_STYLE_INI;
            case ".java":
                return SyntaxConstants.SYNTAX_STYLE_JAVA;
            case ".js":
                return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case ".json":
                return SyntaxConstants.SYNTAX_STYLE_JSON;
            // case ".json": return SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
            case ".jsp":
                return SyntaxConstants.SYNTAX_STYLE_JSP;
            case ".tex":
            case ".ltx":
                return SyntaxConstants.SYNTAX_STYLE_LATEX;
            case ".less":
                return SyntaxConstants.SYNTAX_STYLE_LESS;
            case ".lisp":
                return SyntaxConstants.SYNTAX_STYLE_LISP;
            case ".lua":
                return SyntaxConstants.SYNTAX_STYLE_LUA;
            case ".mk":
                return SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
            case ".mxml":
                return SyntaxConstants.SYNTAX_STYLE_MXML;
            case ".nsi":
                return SyntaxConstants.SYNTAX_STYLE_NSIS;
            case ".p6":
            case ".pl":
                return SyntaxConstants.SYNTAX_STYLE_PERL;
            case ".php":
                return SyntaxConstants.SYNTAX_STYLE_PHP;
            case ".properties":
                return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            case ".py":
                return SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case ".rb":
                return SyntaxConstants.SYNTAX_STYLE_RUBY;
            case ".rbw":
                return SyntaxConstants.SYNTAX_STYLE_RUBY;
            case ".sas":
                return SyntaxConstants.SYNTAX_STYLE_SAS;
            case ".scala":
                return SyntaxConstants.SYNTAX_STYLE_SCALA;
            case ".sql":
                return SyntaxConstants.SYNTAX_STYLE_SQL;
            case ".tcl":
                return SyntaxConstants.SYNTAX_STYLE_TCL;
            case ".ts":
                return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case ".sh":
                return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case ".vb":
                return SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC;
            case ".bat":
                return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case ".xml":
            case ".xsd":
            case ".svg":
                return SyntaxConstants.SYNTAX_STYLE_XML;
            case ".yaml":
                return SyntaxConstants.SYNTAX_STYLE_YAML;
            default:
                return SyntaxConstants.SYNTAX_STYLE_NONE;
        }
    }

    public void selectContentAndLine(int line, String Findstr) {
        if ("resources.arsc".equals(currentSelectedObj.getPath())) {
            textTableViewer.getSelectionModel().clearSelection();
            textTableViewer.getSelectionModel().addSelectionInterval(line, line);

            int maxscroolbar = textTableScroll.getVerticalScrollBar().getMaximum();
            int rowCount = textTableViewer.getRowCount();
            textTableScroll.getVerticalScrollBar().setValue((line * (maxscroolbar / rowCount)));
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
