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

import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.action.ActionEventHandler;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RImg;
import com.apkspectrum.util.Log;

public class Resources extends AbstractTabbedPanel implements TreeSelectionListener
{
	private static final long serialVersionUID = -934921813626224616L;

	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_FIND = "TREE_FIND";
	private static final String RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH = "TREE_REFRESH";

	private ResourceTree resTree;
	private ResourceContentsPanel contentPanel;

	private JTextField textField;
	private JButton findicon;
	private JButton refreshicon;

	private ActionListener listener;

	public Resources(ActionListener listener) {
		setTitle(RComp.TABBED_RESOURCES);
		setTabbedEnabled(false);

		this.listener = listener;
		if(listener instanceof ActionEventHandler) {
			((ActionEventHandler) listener).putData(ResContentFocusChanger.class, new ResContentFocusChanger() {
				@Override
				public void setResContentFocus(String path, int line, String string) {
					Log.v("path : " + path + ", line " + line + ", text " + string);
					setSeletected();
					if(resTree.setSelectNodeByPath(path)) {
						contentPanel.selectContentAndLine(line, string);
					}
				}
			});
		}
	}

	@Override
	public void initialize() {
		this.setLayout(new GridLayout(1, 1));

		resTree = new ResourceTree(listener);
		resTree.addTreeSelectionListener(this);

		contentPanel = new ResourceContentsPanel(listener);

		TreeFindFildListener findListener = new TreeFindFildListener();

		findicon = new JButton(RImg.RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		findicon.setFocusPainted(false);
		findicon.setPreferredSize(new Dimension(22, 22));
		findicon.setActionCommand(RESOURCE_TREE_TOOLBAR_BUTTON_FIND);
		findicon.addActionListener(findListener);

		refreshicon = new JButton(RImg.RESOURCE_TREE_TOOLBAR_REFRESH.getImageIcon(16, 16));
		refreshicon.setEnabled(false);
		refreshicon.setFocusPainted(false);
		refreshicon.setPreferredSize(new Dimension(22, 22));
		refreshicon.setActionCommand(RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH);
		refreshicon.addActionListener(findListener);

		textField = new JTextField();
		textField.setActionCommand(RESOURCE_TREE_TOOLBAR_BUTTON_FIND);
		textField.addActionListener(findListener);
		textField.addFocusListener(findListener);
		textField.addKeyListener(findListener);

		JPanel treeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		treeButtonPanel.add(findicon);
		treeButtonPanel.add(refreshicon);

		JPanel treeModePanel = new JPanel(new BorderLayout());
		treeModePanel.add(textField, BorderLayout.CENTER);
		treeModePanel.add(treeButtonPanel, BorderLayout.EAST);

		JScrollPane treeNaviScroll = new JScrollPane(treeModePanel);
		treeNaviScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		treeNaviScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		treeNaviScroll.setBorder(new EmptyBorder(0,0,0,0));

		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(treeNaviScroll, BorderLayout.NORTH);
		treePanel.add(new JScrollPane(resTree), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(treePanel);
		splitPane.setRightComponent(contentPanel);
		splitPane.setDividerLocation(200);

		add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, int status) {
		if(ApkScanner.STATUS_RESOURCE_COMPLETED != status) {
			if(ApkScanner.STATUS_RES_DUMP_COMPLETED == status
					&& contentPanel != null) {
				contentPanel.setData(apkInfo);
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

	@Override
	public void valueChanged(TreeSelectionEvent evt) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) resTree.getLastSelectedPathComponent();
		if(node == null) return;
		contentPanel.selectContent(node.getUserObject());
	}

	class TreeFindFildListener implements ActionListener, KeyListener, FocusListener
	{
		@Override
		public void actionPerformed(ActionEvent evt) {
			String searchText = "";
			switch(evt.getActionCommand()) {
			case RESOURCE_TREE_TOOLBAR_BUTTON_FIND:
				searchText = textField.getText().trim();
				break;
			case RESOURCE_TREE_TOOLBAR_BUTTON_REFRESH:
				textField.setText(searchText);
				break;
			}
			refreshicon.setEnabled(!searchText.isEmpty());
			resTree.searchTree(searchText);
		}

		@Override
		public void focusLost(FocusEvent evt) {
			textField.setBackground(new Color(255, 255, 255));
		}

		@Override
		public void focusGained(FocusEvent evt) {
			textField.setBackground(new Color(178, 235, 244));
		}

		@Override
		public void keyReleased(KeyEvent evt) {
			findicon.setEnabled(!textField.getText().isEmpty());
		}

		@Override
		public void keyTyped(KeyEvent evt) { }

		@Override
		public void keyPressed(KeyEvent evt) { }
	}
}
