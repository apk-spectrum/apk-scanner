package com.apkscanner.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInGroup;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.PlugInPackage;
import com.apkscanner.util.Log;

public class PlugInSettingPanel extends JPanel implements TreeSelectionListener {
	private static final long serialVersionUID = 1234825421488294264L;

	private static final String TREE_NODE_NETWORK_SETTING = "TREE_NODE_NETWORK_SETTING";
	private static final String TREE_NODE_CONFIGURATION_SETTING = "TREE_NODE_CONFIGURATION_SETTING";
	
	private JTree tree;
	private DefaultMutableTreeNode root;
	private JTextArea description;

	class CheckBoxNodeRenderer extends JPanel implements TreeCellRenderer, ItemSelectable {
		private static final long serialVersionUID = -6067593221379257354L;

		private JCheckBox check;
		private JLabel label;
		private Object userObject;

		Color selectionBorderColor, selectionForeground, selectionBackground,
		      textForeground, textBackground;

		public CheckBoxNodeRenderer() {
			Log.e("new CheckBoxNodeRenderer()");
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			setBorder(new EmptyBorder(0,0,0,0));
			//setOpaque(false);
			
			check = new JCheckBox();
			check.setBorder(new EmptyBorder(2,0,0,0));
			check.setOpaque(false);
			
			label = new JLabel();
			label.setBorder(new EmptyBorder(0,0,0,0));
			label.setOpaque(false);
			
			add(check);
			add(label);
			
			Font fontValue;
			fontValue = UIManager.getFont("Tree.font");
			if (fontValue != null) {
				label.setFont(fontValue);
			}
			Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
			check.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));

			selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
			selectionForeground = UIManager.getColor("Tree.selectionForeground");
			selectionBackground = UIManager.getColor("Tree.selectionBackground");
			textForeground = UIManager.getColor("Tree.textForeground");
			textBackground = UIManager.getColor("Tree.textBackground");
		}

		public String getText() {
			return label.getText();
		}

		public void setText(String text) {
			label.setText(text);
		}

		public boolean isSelected() {
			return check.isSelected();
		}

		public void setSelected(boolean selected) {
			check.setSelected(selected);;
		}

		public Object getUserObject() {
			return userObject;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
				userObject = ((DefaultMutableTreeNode) value).getUserObject();

				if(userObject instanceof String) {
					label.setText(userObject.toString());
					label.setIcon(null);
					check.setVisible(false);
					check.setEnabled(false);
					check.setSelected(false);
				} else if(userObject instanceof IPlugIn) {
					IPlugIn plugin = (IPlugIn) userObject;
					URL iconUrl = plugin.getIconURL();
					Icon icon = null;
					if(iconUrl != null) {
						icon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(iconUrl), 16, 16));
					}
					boolean enabled = true;
					if(plugin.getGroupName() != null) {
						enabled = plugin.getParantGroup().isEnabled();
					} else {
						enabled = plugin.getPlugInPackage().isEnabled();
					}
					label.setText(plugin.getLabel());
					label.setIcon(icon);
					check.setVisible(true);
					check.setEnabled(enabled);
					check.setSelected(plugin.isEnabled(false));
				} else if(userObject instanceof PlugInPackage) {
					PlugInPackage pluginPackage = (PlugInPackage) userObject;
					URL iconUrl = pluginPackage.getIconURL();
					Icon icon = null;
					if(iconUrl != null) {
						icon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(iconUrl), 16, 16));
					}
					label.setText(pluginPackage.getLabel() + " / " + pluginPackage.getVersionName());
					label.setIcon(icon);
					check.setVisible(true);
					check.setEnabled(tree.isEnabled());
					check.setSelected(pluginPackage.isEnabled());
				}
			}

			if (selected) {
				setForeground(selectionForeground);
				setBackground(selectionBackground);
			} else {
				setForeground(textForeground);
				setBackground(textBackground);
			}

			return this;
		}

		@Override
		public void addItemListener(ItemListener listener) {
			check.addItemListener(listener);
		}

		@Override
		public Object[] getSelectedObjects() {
			return check.getSelectedObjects();
		}

		@Override
		public void removeItemListener(ItemListener listener) {
			check.removeItemListener(listener);
		}
	}

	class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
		private static final long serialVersionUID = -931248963141465409L;

		private CheckBoxNodeRenderer renderer;

		public CheckBoxNodeEditor(final JTree tree) {
			renderer = new CheckBoxNodeRenderer();
			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					boolean selected = itemEvent.getStateChange() == ItemEvent.SELECTED;
					Object userObject = renderer.getUserObject();
					if(userObject instanceof IPlugIn) {
						((IPlugIn) userObject).setEnabled(selected);
					} else if(userObject instanceof PlugInPackage) {
						((PlugInPackage) userObject).setEnabled(selected);
					}
					if (stopCellEditing()) {
						fireEditingStopped();
					}
					tree.repaint();
				}
			};
			renderer.addItemListener(itemListener);
		}

		@Override
		public Object getCellEditorValue() {
			return renderer.getUserObject();
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			boolean returnValue = false;
			if (event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) event;
				TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
				if (path != null) {
					Object node = path.getLastPathComponent();
					if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
						Object userObject = treeNode.getUserObject();
						returnValue = (userObject instanceof IPlugIn || userObject instanceof PlugInPackage);
					}
				}
			}
			return returnValue;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row) {
			return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}
	}

	public PlugInSettingPanel() {
		setLayout(new BorderLayout());

		JPanel pluginTreePanel = new JPanel();
		pluginTreePanel.setLayout(new BoxLayout(pluginTreePanel, BoxLayout.Y_AXIS));

		tree = new JTree(root = new DefaultMutableTreeNode("PlugIn"));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new CheckBoxNodeRenderer());
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
		tree.setEditable(true);
		tree.setRootVisible(true);


		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setAlignmentX(0);
		
		JLabel label = new JLabel("Enable PlugIns : ");
		label.setAlignmentX(0);
		
		pluginTreePanel.add(label);
		pluginTreePanel.add(scrollPane);
		
		JPanel pluginDescPanel = new JPanel();
		pluginDescPanel.setLayout(new BoxLayout(pluginDescPanel, BoxLayout.Y_AXIS));
		
		label = new JLabel("Description");
		label.setAlignmentX(0);
		
		description = new JTextArea();
		description.setEditable(false);
		
		scrollPane = new JScrollPane(description);
		scrollPane.setAlignmentX(0);

		pluginDescPanel.add(label);
		pluginDescPanel.add(scrollPane);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitPane.setTopComponent(pluginTreePanel);
		splitPane.setBottomComponent(pluginDescPanel);
		splitPane.setBorder(new EmptyBorder(0,0,0,0));
		splitPane.setDividerLocation(200);

		add(splitPane, BorderLayout.CENTER);

		loadPlugins();
	}
	
	public void loadPlugins() {
		root.removeAllChildren();
		for(PlugInPackage pack: PlugInManager.getPlugInPackages()) {
			HashMap<DefaultMutableTreeNode, PlugInGroup[]> groupMap = new HashMap<>();
			HashMap<DefaultMutableTreeNode, IPlugIn[]> pluginMap = new HashMap<>();
			Queue<DefaultMutableTreeNode> groupQueue = new LinkedList<DefaultMutableTreeNode>();

			DefaultMutableTreeNode packNode = new DefaultMutableTreeNode(pack);
			groupMap.put(packNode, pack.getTopPlugInGroup());
			pluginMap.put(packNode, pack.getPlugInWithoutGroup());
			groupQueue.offer(packNode);

			DefaultMutableTreeNode node = null;
			while((node = groupQueue.poll()) != null) {
				for(PlugInGroup group: groupMap.get(node)) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(group);
					node.add(child);
					groupMap.put(child, group.getChildrenGroup());
					pluginMap.put(child, group.getPlugIn());
					groupQueue.offer(child);
				}
				for(IPlugIn plugin: pluginMap.get(node)) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(plugin);
					node.add(child);
				}
			}
			if(pack.useNetworkSetting()) {
				packNode.add(new DefaultMutableTreeNode(TREE_NODE_NETWORK_SETTING));
			}
			if(pack.useConfigurationSetting()) {
				packNode.add(new DefaultMutableTreeNode(TREE_NODE_CONFIGURATION_SETTING));
			}
			root.add(packNode);
			tree.expandPath(new TreePath(packNode.getPath()));
		}
		tree.expandPath(new TreePath(root.getPath()));
		tree.updateUI();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object userObject = e.getPath().getLastPathComponent();
		if(!(userObject instanceof DefaultMutableTreeNode)) return;

		userObject = ((DefaultMutableTreeNode)userObject).getUserObject();
		if(userObject instanceof IPlugIn) {
			description.setText(((IPlugIn)userObject).getDescription());
			description.setCaretPosition(0);
		} else if(userObject instanceof PlugInPackage) {
			description.setText(((PlugInPackage)userObject).getDescription());
			description.setCaretPosition(0);
		} else {
			Log.v("Other: " + userObject.toString());
		}
	}
}
