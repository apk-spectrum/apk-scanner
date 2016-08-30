package com.apkscanner.gui.dialog.install;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.gui.dialog.ApkInstallWizard.InstallDlgFuncListener;
import com.apkscanner.gui.dialog.install.InstallDlg.CHECKLIST_MODE;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.ArrowTraversalPane;
import com.apkscanner.gui.util.BooleanTableModel;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.tool.adb.AdbPackageManager;
import com.apkscanner.tool.adb.AdbPackageManager.PackageListObject;
import com.apkscanner.tool.adb.AdbWrapper;
import com.apkscanner.util.Log;

import java.util.ArrayList;
import java.io.File;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
 
public class InstallWizardDlg extends JPanel
                      implements ActionListener{
	private static final long serialVersionUID = 813267847663868531L;
	
	public static final int CANCEL_OPTION = 1;
	public static final int APPROVE_OPTION = 0;
	public static final int ERROR_OPTION = -1;
	
    private JDialog dialog;
    static private int result;    
    private DlgFuncListener CoreInstallLitener;
    
    class DlgFuncListener implements InstallDlgFuncListener {

		@Override
		public void Complete(String str) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int ShowQuestion(Runnable runnable, Object message, String title, int optionType, int messageType,
				Icon icon, Object[] options, Object initialValue) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void AddLog(String str) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getResult() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void SetResult(int i) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int ShowDeviceList(Runnable runnable) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void AddCheckList(String name, String t, CHECKLIST_MODE mode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public DeviceStatus getSelectDev() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getValue(String text) {
			// TODO Auto-generated method stub
			return 0;
		}
    	
    }
        
    
	public InstallDlgFuncListener getInstallDlgFuncListener() {
		return this.CoreInstallLitener;
	}
	
	public InstallWizardDlg() {
        super(new BorderLayout());
        CoreInstallLitener = new DlgFuncListener();
    }
    
    public static void expandOrCollapsePath (JTree tree,TreePath treePath,int level,int currentLevel,boolean expand) {
//      System.err.println("Exp level "+currentLevel+", exp="+expand);
      if (expand && level<=currentLevel && level>0) return;

      TreeNode treeNode = ( TreeNode ) treePath.getLastPathComponent();
      TreeModel treeModel=tree.getModel();
      if ( treeModel.getChildCount(treeNode) >= 0 ) {
         for ( int i = 0; i < treeModel.getChildCount(treeNode); i++  ) {
            TreeNode n = ( TreeNode )treeModel.getChild(treeNode, i);
            TreePath path = treePath.pathByAddingChild( n );
            expandOrCollapsePath(tree,path,level,currentLevel+1,expand);
         }
         if (!expand && currentLevel<level) return;
      }      
      if (expand) {
         tree.expandPath( treePath );
//         System.err.println("Path expanded at level "+currentLevel+"-"+treePath);
      } else {
         tree.collapsePath(treePath);
//         System.err.println("Path collapsed at level "+currentLevel+"-"+treePath);
      }
   }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    @SuppressWarnings("unused")
	private void createAndShowGUI(Component component)
    {
    	final InstallWizardDlg ptg = new InstallWizardDlg();
    	
        //Create and set up the window.
    	dialog = new JDialog(new JFrame(), Resource.STR_TREE_OPEN_PACKAGE.getString(), true);
    	dialog.setIconImage(Resource.IMG_USB_ICON.getImageIcon().getImage());
    	//dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    	
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 8368291008098324014L;
			public void actionPerformed(ActionEvent e) {
				result = CANCEL_OPTION;
				dialog.dispose();
		    }
		});
 
        //Add content to the window.
    	dialog.add(ptg);

        //Display the window.
    	dialog.pack();
    	dialog.setBounds(100, 100, 600, 400);
    	dialog.setMinimumSize(new Dimension(600, 400));
		
    	dialog.setLocationRelativeTo(component);
    	dialog.setVisible(true);
    	dialog.dispose();
    	
    	Log.i("package dialog closed");
    }
 
    public int showTreeDlg(Component component)
    {
    	result = APPROVE_OPTION;
        
        //Create and set up the window.
    	dialog = new JDialog(new JFrame(), Resource.STR_TREE_OPEN_PACKAGE.getString(), true);
    	dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	dialog.setIconImage(Resource.IMG_USB_ICON.getImageIcon().getImage());

    	KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 8368291008098324014L;
			public void actionPerformed(ActionEvent e) {
				result = CANCEL_OPTION;
				dialog.dispose();
		    }
		});
 		
        //Add content to the window.
    	dialog.add(this);
    	dialog.pack();
    	dialog.setBounds(0, 0, 600, 400);
    	dialog.setMinimumSize(new Dimension(600, 400));
		dialog.setResizable(false);
    	dialog.setLocationRelativeTo(component);
    	dialog.setVisible(true);
    	dialog.dispose();
    	
    	return result;
    }

    
	@Override
	public void actionPerformed(ActionEvent e)
	{

	}	
}