package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.dialog.SimpleCheckTableModel.TableRowObject;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ApkFileChooser;
import com.apkspectrum.swing.FilteredTreeModel;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.swing.SortedMutableTreeNode;
import com.apkspectrum.swing.TreeNodeIconRefresher;
import com.apkspectrum.swing.WindowSizeMemorizer;
import com.apkspectrum.tool.adb.AdbServerMonitor;
import com.apkspectrum.tool.adb.IPackageStateListener;
import com.apkspectrum.tool.adb.PackageInfo;
import com.apkspectrum.tool.adb.PackageManager;
import com.apkspectrum.tool.adb.WindowStateInfo;
import com.apkspectrum.util.SystemUtil;

public class PackageTreeDlg extends JDialog implements TreeSelectionListener, ActionListener {
    private static final long serialVersionUID = 813267847663868531L;

    public static final int CANCEL_OPTION = 1;
    public static final int APPROVE_OPTION = 0;
    public static final int ERROR_OPTION = -1;

    private JTextField textFieldapkPath;
    private JTree tree;
    private DefaultMutableTreeNode top;
    private JPanel gifPanel;
    private JCheckBox checkboxUseframework;

    private JPanel ListPanel;
    private JButton refreshbtn;
    static private int result;
    private String selDevice;
    private String selPackage;
    private String selApkPath;
    private String tmpApkPath;
    private JTextField textSearchFilter;

    private final String[] columnNames = {"", RStr.LABEL_DEVICE.get(), RStr.LABEL_PATH.get()};
    private ArrayList<TableRowObject> tableListArray = new ArrayList<TableRowObject>();
    private JTable table;

    private DeviceHandler deviceHandler;

    // Must be accessing to pullingNodes in EventDispatchThread
    private ArrayList<TreeNode> pullingNodes = new ArrayList<TreeNode>();

    public class FrameworkTableObject implements TableRowObject {
        public Boolean buse;
        public String location;
        public String deviceID;
        public String path;

        FrameworkTableObject(Boolean buse, String location, String deviceID, String path) {
            this.buse = buse;
            this.location = location;
            this.deviceID = deviceID;
            this.path = path;
        }

        @Override
        public Object get(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return buse;
                case 1:
                    return deviceID + "(" + location + ")";
                case 2:
                    return path;
            }
            return null;
        }

        @Override
        public void set(int columnIndex, Object obj) {
            switch (columnIndex) {
                case 0:
                    buse = (Boolean) obj;
                    break;
                case 1:
                    location = (String) obj;
                    break;
                case 2:
                    path = (String) obj;
                    break;
            }
        }
    }

    public PackageTreeDlg(Window owner) {
        super(owner);
        initialize(owner);

        deviceHandler = new DeviceHandler();
        deviceHandler.execute();

        refreshTreeList(true);
    }

    private void initialize(Window window) {
        setTitle(RStr.TREE_OPEN_PACKAGE.get());
        setIconImage(RImg.USB_ICON.getImage());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setModal(true);
        setLayout(new BorderLayout());

        Dimension size = new Dimension(600, 400);
        WindowSizeMemorizer.apply(this, size);
        setMinimumSize(size);

        setLocationRelativeTo(window);

        addWindowListener(new WindowEventHandler());



        KeyStrokeAction.registerKeyStrokeAction(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), RStr.BTN_CANCEL.get(), this);

        KeyStrokeAction.registerKeyStrokeAction(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), RStr.BTN_REFRESH.get(), this);

        top = new DefaultMutableTreeNode(RStr.TREE_NODE_DEVICE.get());

        tree = new JTree(new FilteredTreeModel(top));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseEventHandler());

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = 8466579635851211258L;

            private final ImageIcon iconApk = RImg.TREE_APK.getImageIcon(16, 16);
            private final ImageIcon iconDevice = RImg.TREE_DEVICE.getImageIcon(16, 16);
            private final ImageIcon iconTop = RImg.TREE_TOP.getImageIcon(16, 16);
            private final ImageIcon iconFolder = RImg.TREE_FOLDER.getImageIcon();
            private final ImageIcon iconLoading = RImg.TREE_LOADING.getImageIcon();
            private final ImageIcon iconFavor = RImg.TREE_FAVOR.getImageIcon();

            private TreeNodeIconRefresher treeIconRefresher = new TreeNodeIconRefresher(tree);
            {
                iconLoading.setImageObserver(treeIconRefresher);
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded,
                        isLeaf, row, focused);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                int level = node.getLevel();

                if (level == 0) {
                    setIcon(iconTop);
                } else if (node.getUserObject() instanceof IDevice) {
                    setText(((IDevice) node.getUserObject())
                            .getProperty(IDevice.PROP_DEVICE_MODEL));
                    setIcon(iconDevice);
                } else {
                    if (node.getUserObject() instanceof PackageInfo) {
                        if (pullingNodes.contains(node)) {
                            setIcon(iconLoading);
                            treeIconRefresher.addTreeNode(node);
                        } else {
                            PackageInfo pack = (PackageInfo) node.getUserObject();
                            if (pack.packageName.equals(
                                    PackageManager.getCurrentFocusPackage(pack.device, false))) {
                                setIcon(iconFavor);
                            } else {
                                setIcon(iconApk);
                            }
                            treeIconRefresher.removeTreeNode(node);
                        }
                    } else if (node.getUserObject() instanceof String) {
                        String nodeName = (String) node.getUserObject();
                        if (nodeName.startsWith("*")) {
                            setText(nodeName.substring(1));
                            setIcon(iconFavor);
                        } else if (nodeName.startsWith("#")) {
                            setText(nodeName.substring(1));
                            setIcon(iconLoading);
                            treeIconRefresher.addTreeNode(node);
                        } else {
                            setIcon(iconFolder);
                        }
                    } else {
                        setIcon(iconFolder);
                    }
                }
                return c;
            }
        });

        // Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        textFieldapkPath = new JTextField();
        textFieldapkPath.setEditable(false);

        textSearchFilter = new JTextField();
        textSearchFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setFilter();
            }
        });

        textSearchFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node != null) {
                    openPackage();
                }
            }

        });

        textSearchFilter.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP) {
                    tree.dispatchEvent(ke);
                }
            }
        });

        ListPanel = makeListTable();
        ListPanel.setVisible(false);

        checkboxUseframework = new JCheckBox(RStr.LABEL_USES_RESOURCE.get());
        checkboxUseframework.setSelected(false);
        checkboxUseframework.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkboxUseframework.isSelected()) {
                    ListPanel.setVisible(true);
                } else {
                    ListPanel.setVisible(false);
                }
            }
        });

        JButton openbtn = new JButton(RStr.BTN_OPEN.get());
        refreshbtn = new JButton(RStr.BTN_REFRESH.get());
        JButton exitbtn = new JButton(RStr.BTN_CANCEL.get());

        openbtn.addActionListener(this);
        refreshbtn.addActionListener(this);
        exitbtn.addActionListener(this);

        JLabel GifLabel = new JLabel(RImg.WAIT_BAR.getImageIcon());
        JLabel Loading = new JLabel(RStr.LABEL_LOADING.get());

        gifPanel = new JPanel();
        gifPanel.add(Loading);
        gifPanel.add(GifLabel);
        gifPanel.setVisible(false);

        JPanel ButtonPanelWest = new JPanel();
        ButtonPanelWest.add(gifPanel);
        ButtonPanelWest.add(refreshbtn);

        JPanel ButtonPanelEast = new JPanel();
        ButtonPanelEast.add(openbtn);
        ButtonPanelEast.add(exitbtn);

        JPanel ButtonPanel = new JPanel(new BorderLayout());
        ButtonPanel.add(ButtonPanelWest, BorderLayout.WEST);
        ButtonPanel.add(ButtonPanelEast, BorderLayout.EAST);

        Dimension minimumSize = new Dimension(100, 50);
        treeView.setMinimumSize(minimumSize);

        // Add the split pane to this panel.
        // add(splitPane);
        JPanel panelnorth = new JPanel(new BorderLayout());
        JPanel panelsourth = new JPanel(new BorderLayout());
        JPanel panelsearch = new JPanel(new BorderLayout());

        panelsearch.add(new JLabel(RStr.LABEL_SEARCH.get() + " : "), BorderLayout.WEST);
        panelsearch.add(textSearchFilter, BorderLayout.CENTER);

        panelnorth.add(textFieldapkPath, BorderLayout.NORTH);
        panelnorth.add(treeView, BorderLayout.CENTER);
        panelnorth.add(panelsearch, BorderLayout.SOUTH);


        panelsourth.add(checkboxUseframework, BorderLayout.NORTH);
        panelsourth.add(ListPanel, BorderLayout.CENTER);
        panelsourth.add(ButtonPanel, BorderLayout.SOUTH);

        add(panelnorth, BorderLayout.CENTER);
        add(panelsourth, BorderLayout.SOUTH);
    }

    public String getSelectedDevice() {
        return selDevice;
    }

    public String getSelectedPackage() {
        return selPackage;
    }

    public String getSelectedApkPath() {
        return selApkPath;
    }

    public String getSelectedFrameworkRes() {
        String resList = null;
        if (checkboxUseframework.isSelected()) {
            resList = "";
            for (TableRowObject res : tableListArray) {
                FrameworkTableObject fwres = (FrameworkTableObject) res;
                if (!fwres.buse) continue;
                if (fwres.deviceID.equals("local")) {
                    resList += fwres.path + ";";
                } else {
                    resList += "@" + fwres.deviceID + fwres.path + ";";
                }
            }
        }
        return resList;
    }

    public File getSelectedFile() {
        if (tmpApkPath != null) return new File(tmpApkPath);
        return null;
    }

    private IDevice getCurrentSelectedDevice() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node.getChildCount() != 0) {
            Log.w("not node!");
            return null;
        }

        DefaultMutableTreeNode deviceNode = null;
        for (deviceNode = node; !(deviceNode.getUserObject() instanceof IDevice); deviceNode =
                ((DefaultMutableTreeNode) deviceNode.getParent())) {
        }

        return ((IDevice) deviceNode.getUserObject());
    }

    private void addframeworkresIntree() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node.getChildCount() != 0) {
            Log.w("not node!");
            return;
        }
        PackageInfo tempObject = ((PackageInfo) node.getUserObject());

        IDevice device = getCurrentSelectedDevice();
        // for(deviceNode = node ; deviceNode.getUserObject() instanceof DeviceStatus==false;
        // deviceNode = ((DefaultMutableTreeNode)deviceNode.getParent())) { }
        // Log.i(deviceNode.getUserObject());
        FrameworkTableObject temp =
                new FrameworkTableObject(true, device.getProperty(IDevice.PROP_DEVICE_MODEL),
                        device.getSerialNumber(), tempObject.getApkPath());

        tableListArray.add(temp);
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();

        table.updateUI();

    }

    private void refreshTreeList(final boolean allowCache) {
        if (!refreshbtn.isVisible()) {
            Log.i("Already refreshing...");
            return;
        }

        tableListArray.clear();
        table.updateUI();

        top.removeAllChildren();
        top.add(new DefaultMutableTreeNode(RStr.MSG_DEVICE_NOT_FOUND.get().replace("\n", " ")));
        tree.expandPath(new TreePath(top.getPath()));
        tree.updateUI();

        SwingWorker<Integer, Object> task = new SwingWorker<Integer, Object>() {
            @Override
            protected Integer doInBackground() throws Exception {
                AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
                IDevice[] devices = adb.getDevices();
                for (IDevice device : devices) {
                    if (!allowCache) {
                        PackageManager.removeListCache(device);
                    }
                    deviceHandler.deviceConnected(device);
                }
                return devices.length;
            }
        };
        task.execute();

    }

    public static void expandOrCollapsePath(JTree tree, TreePath treePath, int level, int curLevel,
            boolean expand, List<String> exclude) {
        if (expand && level <= curLevel && level > 0) return;

        TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
        TreeModel treeModel = tree.getModel();

        int childCnt = treeModel.getChildCount(treeNode);
        if (childCnt == 0) return;

        for (int i = 0; i < childCnt; i++) {
            DefaultMutableTreeNode node = null;
            node = (DefaultMutableTreeNode) treeModel.getChild(treeNode, i);
            if (node.getUserObject() instanceof PackageInfo) return;

            if (exclude != null && exclude.contains(node.getUserObject())) {
                continue;
            }

            TreePath path = treePath.pathByAddingChild(node);
            expandOrCollapsePath(tree, path, level, curLevel + 1, expand, exclude);
        }

        if (expand) {
            tree.expandPath(treePath);
        } else if (curLevel >= level) {
            tree.collapsePath(treePath);
        }
    }

    /** Required by TreeSelectionListener interface. */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;
        // Object nodeInfo = node.getUserObject();

        if (node.getUserObject() instanceof PackageInfo) {
            PackageInfo tempObject = ((PackageInfo) node.getUserObject());
            textFieldapkPath.setText(tempObject.getApkPath() + " - " + tempObject.packageName);
        } else {
            TreeNode[] treenode = node.getPath();
            TreePath path = new TreePath(treenode);
            textFieldapkPath.setText(path.toString());
        }
    }

    private void expandAll(final JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void collapseAll(JTree tree) {
        for (int row = tree.getRowCount() - 1; row >= 0; row--) {
            tree.collapseRow(row);
        }
    }

    private void forSelectionTree(FilteredTreeModel filteredModel) {
        DefaultMutableTreeNode currentNode = top.getNextNode();
        while (currentNode != null) {
            if (filteredModel.isMatched(currentNode)
                    && currentNode.getUserObject() instanceof PackageInfo) {
                TreePath temptreePath = new TreePath(currentNode.getPath());
                tree.setSelectionPath(temptreePath);
                tree.scrollPathToVisible(temptreePath);
                return;
            }
            currentNode = currentNode.getNextNode();
        }
    }

    private void setFilter() {
        String filter = textSearchFilter.getText().trim();

        FilteredTreeModel filteredModel = (FilteredTreeModel) tree.getModel();
        filteredModel.setFilter(filter);

        if (!filter.isEmpty()) {
            expandAll(tree);
            forSelectionTree(filteredModel);
        } else {
            collapseAll(tree);
            TreePath path = new TreePath(top.getPath());
            expandOrCollapsePath(tree, path, 3, 0, true, Arrays.asList("apex"));
        }
    }

    public int showTreeDlg() {
        result = APPROVE_OPTION;
        selDevice = null;
        selPackage = null;
        selApkPath = null;

        setVisible(true);

        return result;
    }

    private void openPackage() {
        Log.i("open package");

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node.getDepth() > 0 || node.getLevel() < 3) {
            return;
        }

        PackageInfo tempObject = ((PackageInfo) node.getUserObject());

        Log.i(tempObject.packageName);
        Log.i(tempObject.getLabel());
        Log.i(tempObject.getApkPath());

        DefaultMutableTreeNode deviceNode = null;

        for (deviceNode = node; !(deviceNode.getUserObject() instanceof IDevice); deviceNode =
                ((DefaultMutableTreeNode) deviceNode.getParent())) {

        }

        Log.i(deviceNode.getUserObject().toString());

        selDevice = ((IDevice) deviceNode.getUserObject()).getSerialNumber();
        selPackage = tempObject.packageName;
        selApkPath = tempObject.getRealApkPath();
        // selFrameworkRes = null;

        if (selApkPath == null) {
            Log.e("No Such File : " + tempObject.getApkPath());
            return;
        }

        deviceHandler.quit();
        dispose();
    }

    private boolean uninstallApk(final IDevice device, final PackageInfo packageInfo) {
        String errMessage = null;
        if (!packageInfo.isSystemApp()) {
            errMessage = PackageManager.uninstallPackage(packageInfo);
        } else {
            int n = MessageBoxPool.show(PackageTreeDlg.this,
                    MessageBoxPool.QUESTION_REMOVE_SYSTEM_APK);
            if (n == MessageBoxPane.NO_OPTION) {
                return false;
            }

            errMessage = PackageManager.removePackage(packageInfo);
            if (errMessage == null || errMessage.isEmpty()) {
                n = MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.QUESTION_REBOOT_SYSTEM);
                if (n == MessageBoxPane.YES_OPTION) {
                    try {
                        device.reboot(null);
                    } catch (TimeoutException | IOException e) {
                        e.printStackTrace();
                    } catch (AdbCommandRejectedException e1) {
                        Log.w(e1.getMessage());
                    }
                }
            }
        }

        if (errMessage != null && !errMessage.isEmpty()) {
            final String errMsg = errMessage;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_UNINSTALLED,
                            errMsg);
                }
            });

            return false;
        }

        return true;
    }

    private void clearData() {
        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            Log.i("node == null");
            return;
        }

        IDevice device = getCurrentSelectedDevice();
        final PackageInfo packageInfo = ((PackageInfo) node.getUserObject());

        Log.i("clearData :" + device.getSerialNumber() + "," + packageInfo.packageName);
        new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                return PackageManager.clearData(packageInfo);
            }

            @Override
            protected void done() {
                String errMessage = null;
                try {
                    errMessage = get();
                } catch (InterruptedException | ExecutionException e) {
                    errMessage = e.getMessage();
                    e.printStackTrace();
                }
                if (errMessage == null) {
                    MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_SUCCESS_CLEAR_DATA);
                } else {
                    Log.e(errMessage);
                    MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_CLEAR_DATA,
                            errMessage);
                }
            };
        }.execute();
    }

    private void removePackage() {
        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            Log.i("node == null");
            return;
        }

        IDevice device = getCurrentSelectedDevice();
        PackageInfo tempObject = ((PackageInfo) node.getUserObject());

        Log.i("remove :" + device.getSerialNumber() + "," + tempObject.getCodePath());
        boolean run = uninstallApk(device, tempObject);

        if (run) {
            TreePath path = new TreePath(node.getPath());
            MutableTreeNode nodepath = (MutableTreeNode) path.getLastPathComponent();
            Log.i("Trying to remove tree : " + nodepath.toString());
            MutableTreeNode parent = (MutableTreeNode) nodepath.getParent();

            parent.remove(nodepath);
            // FilteredTreeModel model=(FilteredTreeModel)tree.getModel();
            // model.nodesWereRemoved(parent,new int[]{index},null);
            tree.updateUI();
        }
    }

    private void showDetailInfo() {
        Log.i("showDetailInfo()");

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node.getChildCount() != 0) {
            Log.i("not node!");
            return;
        }

        PackageInfo info = ((PackageInfo) node.getUserObject());
        PackageInfoPanel packageInfoPanel = new PackageInfoPanel();
        packageInfoPanel.setPackageInfo(info);
        packageInfoPanel.showDialog(this);
    }

    private void pullPackage() {
        Log.i("pullPackage()");

        if (!EventQueue.isDispatchThread()) {
            Log.w("Must be accessing to pullingNodes in EventDispatchThread");
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        pullPackage();
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

        if (tree == null) {
            Log.e("tree is null");
            return;
        }

        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof PackageInfo)) {
            Log.w("node is null or no node of PackageInfo!");
            return;
        }

        if (pullingNodes.contains(node)) {
            Log.e("already pulling..");
            return;
        }

        final PackageInfo packageInfo = (PackageInfo) node.getUserObject();

        Log.i(packageInfo.packageName);
        Log.i(packageInfo.getLabel());
        Log.i(packageInfo.getApkPath());

        final String apkPath = packageInfo.getApkPath();
        if (apkPath == null) {
            Log.e("apkPath is null");
            return;
        }

        DefaultMutableTreeNode deviceNode = null;
        for (deviceNode = ((DefaultMutableTreeNode) node.getParent()); deviceNode != null
                && !(deviceNode.getUserObject() instanceof IDevice); deviceNode =
                        ((DefaultMutableTreeNode) deviceNode.getParent())) {
        }

        if (deviceNode == null) {
            Log.e("no such device node");
            return;
        }

        final IDevice device = (IDevice) deviceNode.getUserObject();
        Log.i(device.toString());

        String saveFileName;
        if (apkPath.endsWith("base.apk")) {
            saveFileName = apkPath.replaceAll(".*/(.*)/base.apk", "$1.apk");
        } else {
            saveFileName = apkPath.replaceAll(".*/", "");
        }

        final File destFile = ApkFileChooser.saveApkFile(PackageTreeDlg.this, saveFileName);
        if (destFile == null) return;

        pullingNodes.add(node);

        new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                return PackageManager.pullApk(device, apkPath, destFile.getAbsolutePath());
            }

            @Override
            protected void done() {
                if (pullingNodes.contains(node)) {
                    pullingNodes.remove(node);
                }

                String errMessage = null;
                try {
                    errMessage = get();
                } catch (InterruptedException | ExecutionException e) {
                    errMessage = e.getMessage();
                    e.printStackTrace();
                }
                if (errMessage == null) {
                    int n = MessageBoxPool.show(PackageTreeDlg.this,
                            MessageBoxPool.QUESTION_SUCCESS_PULL_APK, destFile.getAbsolutePath());
                    switch (n) {
                        case 0: // explorer
                            SystemUtil.openFileExplorer(destFile);
                            break;
                        case 1: // open
                            Launcher.run(destFile.getAbsolutePath());
                            break;
                        default:
                            break;
                    }
                } else {
                    MessageBoxPool.show(PackageTreeDlg.this, MessageBoxPool.MSG_FAILURE_PULLED,
                            errMessage);
                }
            };
        }.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(RStr.BTN_OPEN.get())) {
            openPackage();
        } else if (e.getActionCommand().equals(RStr.BTN_CANCEL.get())) {
            // Log.i("exit");
            selDevice = null;
            selPackage = null;
            selApkPath = null;
            result = CANCEL_OPTION;

            deviceHandler.quit();
            dispose();
        } else if (e.getActionCommand().equals(RStr.BTN_REFRESH.get())) {
            refreshTreeList(false);
        }
    }

    public JPanel makeListTable() {
        JPanel panel = new JPanel(new BorderLayout());

        // JTable table = new JTable(new BooleanTableModel());
        table = new JTable();
        // table.setPreferredScrollableViewportSize(table.getPreferredSize());
        // table.setFillsViewportHeight(true);
        JButton addbtn = new JButton(RStr.BTN_ADD.get());

        addbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        RStr.LABEL_APK_FILE_DESC.get(), "apk"));

                if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

                File dir = jfc.getSelectedFile();
                String file = null;
                if (dir != null) {
                    file = dir.getPath();
                }

                Log.i("Select Apk File" + file);

                if (file == null || file.isEmpty()) return;

                FrameworkTableObject temp = new FrameworkTableObject(true, "local", "local", file);

                tableListArray.add(temp);
                ((AbstractTableModel) table.getModel()).fireTableDataChanged();
                table.updateUI();

            }
        });

        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);
        setJTableColumnsWidth(table, 550, 10, 120, 410);
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane pane = new JScrollPane(table);

        // pane.setPreferredSize(new Dimension(550, 80) );

        panel.add(pane, BorderLayout.CENTER);
        panel.add(addbtn, BorderLayout.EAST);

        return panel;
    }

    public static void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
            double... percentages) {
        double total = 0;
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            total += percentages[i];
        }

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth((int) (tablePreferredWidth * (percentages[i] / total)));
        }
    }

    private void setRefreshBtnVisible(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (gifPanel != null && gifPanel.isVisible() != !visible) {
                    gifPanel.setVisible(!visible);
                }
                if (refreshbtn != null && refreshbtn.isVisible() != visible) {
                    refreshbtn.setVisible(visible);
                }
            }
        });
    }

    private class DevicePackageInfo {
        IDevice device;
        PackageInfo[] packages;
        PackageInfo[] displayedPackages;
        PackageInfo[] recentPackages;
        PackageInfo[] runningPackages;
    }

    private class DeviceHandler extends SwingWorker<Object, DevicePackageInfo>
            implements IDeviceChangeListener, IPackageStateListener {
        private volatile boolean quit;
        Queue<IDevice> devQueue = new LinkedList<IDevice>();

        public DeviceHandler() {
            AdbServerMonitor.startServerAndCreateBridgeAsync();
            registerEventListeners();
        }

        @Override
        protected Object doInBackground() throws Exception {
            boolean isWakeUp = false;
            while (!quit) {
                IDevice dev = null;
                synchronized (devQueue) {
                    if (devQueue.isEmpty()) {
                        Log.v("Event queue is empty");
                        setRefreshBtnVisible(true);
                        devQueue.wait();
                        if (quit) break;
                        isWakeUp = true;
                    }
                    dev = devQueue.poll();
                    if (dev != null && isWakeUp) {
                        isWakeUp = false;
                        setRefreshBtnVisible(false);
                    }
                }
                if (dev == null) continue;

                Log.v("start scanning device " + dev);

                DevicePackageInfo devPack = new DevicePackageInfo();

                devPack.device = dev;
                devPack.packages = PackageManager.getPackageList(dev);

                if (devPack.packages == null || devPack.packages.length == 0) {
                    continue;
                }

                List<String> recently = PackageManager.getRecentlyActivityPackages(dev);
                List<PackageInfo> recentlyPackages = new ArrayList<>(recently.size());

                List<String> running = PackageManager.getCurrentlyRunningPackages(dev);
                List<PackageInfo> runningPackages = new ArrayList<>(running.size());

                List<WindowStateInfo> winStates = PackageManager.getCurrentlyDisplayedPackages(dev);
                List<PackageInfo> displayedPackages = new ArrayList<>(winStates.size());

                for (PackageInfo obj : devPack.packages) {
                    if (recently.contains(obj.packageName)) {
                        recentlyPackages.add(obj);
                    }
                    if (running.contains(obj.packageName)) {
                        runningPackages.add(obj);
                    }
                    for (WindowStateInfo info : winStates) {
                        if (obj.packageName.equals(info.packageName)
                                && !displayedPackages.contains(obj)) {
                            if (info.isCurrentFocus) {
                                displayedPackages.add(0, obj);
                            } else {
                                displayedPackages.add(obj);
                            }
                        }
                    }
                }

                devPack.recentPackages =
                        recentlyPackages.toArray(new PackageInfo[recentlyPackages.size()]);
                devPack.runningPackages =
                        runningPackages.toArray(new PackageInfo[runningPackages.size()]);
                devPack.displayedPackages =
                        displayedPackages.toArray(new PackageInfo[displayedPackages.size()]);

                publish(devPack);
            }
            Log.v("doInBackground() Quit");
            return null;
        }

        @Override
        protected void process(List<DevicePackageInfo> chunks) {
            chunks.stream().filter(e -> e.device.isOnline()).forEach(this::addPackageNode);
        }

        public void addPackageNode(DevicePackageInfo devPack) {
            DefaultMutableTreeNode devNode = getDeviceNode(devPack.device);
            devNode.removeAllChildren();

            Map<String, DefaultMutableTreeNode> cache = new HashMap<>();
            for (PackageInfo info : devPack.packages) {
                String apkPath = info.getApkPath();
                if (apkPath == null || apkPath.isEmpty()) continue;
                apkPath = apkPath.substring(0, apkPath.lastIndexOf("/"));

                DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
                DefaultMutableTreeNode parent = cache.get(apkPath);
                if (parent == null) {
                    parent = devNode;
                    for (String dir : apkPath.split("/")) {
                        if (dir.isEmpty()) continue;
                        DefaultMutableTreeNode dirNode = null;
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            DefaultMutableTreeNode tmp = null;
                            tmp = (DefaultMutableTreeNode) parent.getChildAt(i);
                            if (dir.equals(tmp.getUserObject())) {
                                dirNode = tmp;
                                break;
                            }
                        }
                        if (dirNode == null) {
                            dirNode = new SortedMutableTreeNode(dir);
                            parent.add(dirNode);
                        }
                        parent = dirNode;
                    }
                    cache.put(apkPath, parent);
                }
                parent.add(node);

                if (apkPath.equals("/system/framework")) {
                    apkPath = info.getApkPath();
                    FrameworkTableObject tableObject = new FrameworkTableObject(false,
                            devPack.device.getProperty(IDevice.PROP_DEVICE_MODEL),
                            devPack.device.getSerialNumber(), apkPath);

                    if (apkPath.endsWith("framework-res.apk")) {
                        tableObject.buse = true;
                    }

                    tableListArray.add(tableObject);
                }
            }

            DefaultMutableTreeNode node = null;
            if (devPack.displayedPackages.length > 0) {
                node = new SortedMutableTreeNode("*" + RStr.TREE_NODE_DISPLAYED.get());
                for (PackageInfo info : devPack.displayedPackages) {
                    node.add(new DefaultMutableTreeNode(info));
                }
                devNode.add(node);
            }

            if (devPack.recentPackages.length > 0) {
                node = new SortedMutableTreeNode("*" + RStr.TREE_NODE_RECENTLY.get());
                for (PackageInfo info : devPack.recentPackages) {
                    node.add(new DefaultMutableTreeNode(info));
                }
                devNode.add(node);
            }

            if (devPack.runningPackages.length > 0) {
                node = new SortedMutableTreeNode("*" + RStr.TREE_NODE_RUNNING_PROC.get());
                for (PackageInfo info : devPack.runningPackages) {
                    node.add(new DefaultMutableTreeNode(info));
                }
                devNode.add(node);
            }

            tree.updateUI();
            setFilter();

            table.setModel(new SimpleCheckTableModel(columnNames, tableListArray));
            table.setPreferredScrollableViewportSize(new Dimension(0, 80));
            setJTableColumnsWidth(table, 550, 10, 120, 410);
        }

        public void quit() {
            quit = true;
            synchronized (devQueue) {
                devQueue.notifyAll();
            }
            unregisterEventListeners();
        }

        public void registerEventListeners() {
            AndroidDebugBridge.addDeviceChangeListener(this);
            PackageManager.addPackageStateListener(this);
        }

        public void unregisterEventListeners() {
            AndroidDebugBridge.removeDeviceChangeListener(this);
            PackageManager.removePackageStateListener(this);
        }

        private DefaultMutableTreeNode getDeviceNode(IDevice device) {
            for (int i = 0; i < top.getChildCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) top.getChildAt(i);
                if (node.getUserObject() instanceof IDevice
                        && device.equals(node.getUserObject())) {
                    return node;
                }
            }
            return null;
        }

        private DefaultMutableTreeNode addDeviceNode(IDevice device) {
            DefaultMutableTreeNode node = getDeviceNode(device);
            if (node == null) {
                removeNoDeviceNodes();
                node = new SortedMutableTreeNode(device);
            }
            node.removeAllChildren();
            if (device.isOnline()) {
                node.add(new DefaultMutableTreeNode("#" + RStr.LABEL_LOADING.get()));

                synchronized (devQueue) {
                    if (!devQueue.contains(device)) {
                        devQueue.add(device);
                        devQueue.notifyAll();
                    } else {
                        Log.v("Added device to event queue : " + device.getSerialNumber());
                    }
                }
            } else {
                node.add(new DefaultMutableTreeNode(RStr.MSG_DEVICE_UNAUTHORIZED.get()));
            }
            top.add(node);
            tree.expandPath(new TreePath(node.getPath()));
            return node;
        }

        private void removeNoDeviceNodes() {
            for (int i = 0; i < top.getChildCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) top.getChildAt(i);
                if (!(node.getUserObject() instanceof IDevice)) {
                    top.remove(node);
                }
            }
        }

        @Override
        public void deviceChanged(final IDevice device, int changeMask) {
            Log.v("deviceChanged() " + device.getSerialNumber() + ", " + device.getState()
                    + ", changeMask " + changeMask);
            if ((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        addDeviceNode(device);
                        tree.updateUI();
                    }
                });
            }
        }

        @Override
        public void deviceConnected(final IDevice device) {
            Log.v("deviceConnected() " + device.getSerialNumber() + ", " + device.getState());

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addDeviceNode(device);
                    tree.updateUI();
                }
            });
        }

        @Override
        public void deviceDisconnected(final IDevice device) {
            Log.v("deviceDisconnected() " + device.getSerialNumber() + ", " + device.getState());

            synchronized (devQueue) {
                if (devQueue.contains(device)) {
                    devQueue.remove(device);
                }
            }

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DefaultMutableTreeNode node = getDeviceNode(device);
                    if (node != null) {
                        top.remove(node);
                        if (top.getChildCount() == 0) {
                            String msg = RStr.MSG_DEVICE_NOT_FOUND.get().replace("\n", " ");
                            top.add(new DefaultMutableTreeNode(msg));
                        }
                        tree.updateUI();
                    }
                }
            });
        }

        @Override
        public void packageInstalled(PackageInfo packageInfo) {}

        @Override
        public void packageUninstalled(PackageInfo packageInfo) {}

        @Override
        public void enableStateChanged(PackageInfo packageInfo) {}
    }

    class WindowEventHandler extends WindowAdapter {
        @Override
        public void windowOpened(WindowEvent e) {
            textSearchFilter.requestFocus();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            selDevice = null;
            selPackage = null;
            selApkPath = null;
            result = CANCEL_OPTION;

            deviceHandler.quit();
        }
    }

    class MouseEventHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selRow != -1) {
                if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    tree.setSelectionPath(selPath);
                    Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
                    if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
                        DefaultMutableTreeNode node =
                                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        if (node.getDepth() > 0 || node.getLevel() < 3) {
                            return;
                        }

                        JPopupMenu menu = new JPopupMenu();

                        JMenuItem menuitemOpen = new JMenuItem(RStr.BTN_OPEN.get());
                        menuitemOpen.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                openPackage();
                            }
                        });
                        menuitemOpen.setIcon(RImg.TREE_MENU_OPEN.getImageIcon());
                        menu.add(menuitemOpen);

                        JMenuItem menuitemDetail = new JMenuItem(RStr.BTN_DETAILS_INFO.get());
                        menuitemDetail.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                showDetailInfo();
                            }
                        });
                        menuitemDetail.setIcon(RImg.TREE_APK.getImageIcon());
                        menu.add(menuitemDetail);

                        JMenuItem menuitemSave = new JMenuItem(RStr.BTN_SAVE.get());
                        menuitemSave.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                pullPackage();
                            }
                        });
                        menuitemSave.setIcon(RImg.TREE_MENU_SAVE.getImageIcon());
                        menu.add(menuitemSave);

                        JMenuItem menuitemClear = new JMenuItem(RStr.MENU_CLEAR_DATA.get());
                        menuitemClear.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                clearData();
                            }
                        });
                        menuitemClear.setIcon(RImg.TREE_MENU_CLEARDATA.getImageIcon());
                        menu.add(menuitemClear);

                        JMenuItem menuitemDel = new JMenuItem(RStr.BTN_DEL.get());
                        menuitemDel.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                removePackage();
                            }
                        });
                        menuitemDel.setIcon(RImg.TREE_MENU_DELETE.getImageIcon());
                        menu.add(menuitemDel);

                        JMenuItem menuitemaddframeworkres = new JMenuItem(RStr.SETTINGS_RES.get());
                        menuitemaddframeworkres.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                addframeworkresIntree();
                            }
                        });
                        menuitemaddframeworkres.setIcon(RImg.TREE_MENU_LINK.getImageIcon());
                        menu.add(menuitemaddframeworkres);

                        menu.show(tree, e.getX(), e.getY());
                    }
                } else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openPackage();
                }
            }
        }
    }
}
