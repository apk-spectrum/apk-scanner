package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RImg;
import com.apkscanner.util.Log;

public class Resources extends AbstractTabbedPanel {
	private static final long serialVersionUID = -934921813626224616L;

	private ResourceTree resTree;
	private ResourceContentsPanel contentPanel;

	private JTextField textField;
	JButton findicon;
	JButton refreshicon;

	private ActionListener listener;

	static public TreeFocusChanger treefocuschanger;

	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_FIND = "TREE FIND";
	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH = "TREE REFRESH";

	public static abstract interface TreeFocusChanger {
		public void setTreeFocus(String path, int line, String string);
	}

	public Resources(ActionListener listener) {
		setTitle(RComp.TABBED_RESOURCES);
		setTabbedEnabled(false);

		this.listener = listener;
	}

	private void makeTreeFocusChanger() {
		treefocuschanger = new TreeFocusChanger() {
			@Override
			public void setTreeFocus(String path, int line, String string) {
				Log.v("path : " + path + ", line " + line + ", text " + string);
				setSeletected();
				if(resTree.setSelectNodeByPath(path)) {
					contentPanel.selectContentAndLine(line, string);
				}
			}
		};
	}

	public static TreeFocusChanger getTreeFocuschanger() {
		return treefocuschanger;
	}

	class TreeFindFildListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {

			if (arg0.getSource() instanceof JTextField) {
				String temp = ((JTextField) (arg0.getSource())).getText();
				searchTree(temp);
			} else if (arg0.getSource() instanceof JButton) {
				JButton temp = (JButton) (arg0.getSource());

				if (temp.getName().equals(RESOURCE_TREE_TOOLBAR_BUTTON_FIND)) {
					String strtemp = textField.getText();
					searchTree(strtemp);
				} else if (temp.getName().equals(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH)) {
					searchTree("");
				}
				resTree.repaint();
			}
		}

		void searchTree(String str) {
			if(str.length() > 0) {
				refreshicon.setEnabled(true);
			} else {
				refreshicon.setEnabled(false);
			}
			resTree.searchTree(str);
		}
	}

	@Override
	public void initialize() {
		this.setLayout(new GridLayout(1, 1));

		resTree = new ResourceTree(listener);
		resTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
				if(node == null) return;
				contentPanel.selectContent(node.getUserObject());
			}
		});
		makeTreeFocusChanger();

		textField = new JTextField("");
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				((JTextField) (arg0.getSource())).setBackground(new Color(255, 255, 255));
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				((JTextField) (arg0.getSource())).setBackground(new Color(178, 235, 244));
			}
		});

		findicon = new JButton(RImg.RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		refreshicon = new JButton(RImg.RESOURCE_TREE_TOOLBAR_REFRESH.getImageIcon(16, 16));

		findicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setEnabled(false);

		findicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_FIND);
		refreshicon.setName(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH);

		findicon.setFocusPainted(false);
		refreshicon.setFocusPainted(false);

		TreeFindFildListener findListener = new TreeFindFildListener();

		textField.addActionListener(findListener);
		findicon.addActionListener(findListener);
		refreshicon.addActionListener(findListener);

		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(textField.getText().length() > 0) {
					findicon.setEnabled(true);
				} else {
					findicon.setEnabled(false);
				}
			}
			@Override
			public void keyPressed(KeyEvent arg0) {

			}
		});

		JPanel TreeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		TreeButtonPanel.add(findicon);
		TreeButtonPanel.add(refreshicon);

		JPanel TreeModePanel = new JPanel(new BorderLayout());
		TreeModePanel.add(textField, BorderLayout.CENTER);
		TreeModePanel.add(TreeButtonPanel, BorderLayout.EAST);

		JScrollPane treeNaviScroll = new JScrollPane(TreeModePanel);
		treeNaviScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		treeNaviScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		treeNaviScroll.setBorder(new EmptyBorder(0,0,0,0));

		JPanel TreePanel = new JPanel(new BorderLayout());
		TreePanel.add(treeNaviScroll, BorderLayout.NORTH);
		TreePanel.add(new JScrollPane(resTree), BorderLayout.CENTER);

		contentPanel = new ResourceContentsPanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(TreePanel);
		splitPane.setRightComponent(contentPanel);
		splitPane.setDividerLocation(200);

		add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, Status status) {
		if(!Status.RESOURCE_COMPLETED.equals(status)) {
			if(Status.RES_DUMP_COMPLETED.equals(status)) {
				if (contentPanel != null) {
					contentPanel.setData(apkInfo);
				}
			}
			return;
		}

		if (resTree == null)
			initialize();

		if(apkInfo.resources == null)
			return;

		resTree.addTreeNodes(apkInfo.filePath, apkInfo.resources);
		contentPanel.setData(apkInfo);

		setDataSize(apkInfo.resources.length, true, false);
	}
}