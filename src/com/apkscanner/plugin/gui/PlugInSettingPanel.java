package com.apkscanner.plugin.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import com.apkscanner.gui.messagebox.MessageBoxPane;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInGroup;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.PlugInPackage;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class PlugInSettingPanel extends JPanel implements TreeSelectionListener {
	private static final long serialVersionUID = 1234825421488294264L;

	private static final String TREE_NODE_TOP_PLUGINS = "TREE_NODE_TOP_PLUGINS";
	private static final String TREE_NODE_DESCRIPTION = "TREE_NODE_DESCRIPTION";
	private static final String TREE_NODE_NETWORK_SETTING = "TREE_NODE_NETWORK_SETTING";
	private static final String TREE_NODE_CONFIGURATION_SETTING = "TREE_NODE_CONFIGURATION_SETTING";
	private static final String TREE_NODE_NO_PLUGINS = "TREE_NODE_NO_PLUGINS";

	private JTree tree;
	private DefaultMutableTreeNode root;
	private JTextArea description;
	private JPanel extraPanel;

	private NetworkProxySettingPanel proxySettingPanel;

	static class CheckBoxNodeRenderer implements TreeCellRenderer {
		private ItemListener listener;

		public static Color selectionBorderColor, selectionForeground, selectionBackground,
		      textForeground, textBackground;

		static {
			selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
			selectionForeground = UIManager.getColor("Tree.selectionForeground");
			selectionBackground = UIManager.getColor("Tree.selectionBackground");
			textForeground = UIManager.getColor("Tree.textForeground");
			textBackground = UIManager.getColor("Tree.textBackground");
		}

		public CheckBoxNodeRenderer() {
			//Log.v("CheckBoxNodeRenderer() Constructor");
		}

		public CheckBoxNodeRenderer(ItemListener listener) {
			//Log.v("CheckBoxNodeRenderer() Constructor, " + listener);
			this.listener = listener;
		}

		private JPanel makeCellComponent(String text, Icon icon, boolean selected,
				boolean usedCheckbox, boolean enabledCheckBox, boolean selectedCheckBox, ItemListener listener) {
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			panel.setBorder(new EmptyBorder(0,0,0,0));
			//panel.setOpaque(false);
			panel.setForeground(selected ? selectionForeground : textForeground);
			panel.setBackground(selected ? selectionBackground : textBackground);

			JCheckBox check = new JCheckBox();
			check.setBorder(new EmptyBorder(2,0,0,0));
			check.setOpaque(false);
			check.setVisible(usedCheckbox);
			check.setEnabled(enabledCheckBox);
			check.setSelected(selectedCheckBox);
			check.addItemListener(listener);

			Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
			check.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));

			JLabel label = new JLabel();
			label.setBorder(new EmptyBorder(0,0,0,0));
			label.setOpaque(false);
			label.setText(text);
			label.setIcon(icon);

			Font fontValue;
			fontValue = UIManager.getFont("Tree.font");
			if (fontValue != null) {
				label.setFont(fontValue);
			}

			panel.add(check);
			panel.add(label);

			return panel;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			//Log.v("getTreeCellRendererComponent() ");

			String nodeText = "";
			Icon nodeIcon = null;
			boolean visibledCheckBox = false;
			boolean enabledCheckBox = false;
			boolean selectedCheckBox = false;

			Object userObject = null;

			if (value instanceof DefaultMutableTreeNode) {
				userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if(userObject instanceof String) {
					nodeText = (String)userObject;
					switch((String)userObject) {
					case TREE_NODE_NETWORK_SETTING:
						nodeText = "Network Setting";
						break;
					case TREE_NODE_CONFIGURATION_SETTING:
						nodeText = "Configurations Setting";
						break;
					case TREE_NODE_TOP_PLUGINS:
						nodeText = "APK Scanner Plugins";
						nodeIcon = Resource.IMG_APP_ICON.getImageIcon(16, 16);
						break;
					case TREE_NODE_NO_PLUGINS:
						nodeText = "No plugins";
						break;
					}
				} else if(userObject instanceof IPlugIn) {
					IPlugIn plugin = (IPlugIn) userObject;
					URL iconUrl = plugin.getIconURL();
					if(iconUrl != null) {
						nodeIcon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(iconUrl), 16, 16));
					}
					if(plugin.getGroupName() != null) {
						enabledCheckBox = plugin.getParantGroup().isEnabled();
					} else {
						enabledCheckBox = plugin.getPlugInPackage().isEnabled();
					}
					nodeText = plugin.getLabel();
					visibledCheckBox = true;
					selectedCheckBox = plugin.isEnabled(false);
				} else if(userObject instanceof PlugInPackage) {
					PlugInPackage pluginPackage = (PlugInPackage) userObject;
					URL iconUrl = pluginPackage.getIconURL();
					if(iconUrl != null) {
						nodeIcon = new ImageIcon(ImageScaler.getScaledImage(new ImageIcon(iconUrl), 16, 16));
					}
					nodeText = pluginPackage.getLabel() + " / " + pluginPackage.getVersionName();
					visibledCheckBox = true;
					enabledCheckBox = tree.isEnabled();
					selectedCheckBox = pluginPackage.isEnabled();
				}
			}

			final Object source = userObject;
			return makeCellComponent(nodeText, nodeIcon, selected, visibledCheckBox, enabledCheckBox, selectedCheckBox, 
				new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent ie) {
						ie.setSource(source);
						listener.itemStateChanged(ie);
					}
				}
			);
		}
	}

	class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor, ItemListener {
		private static final long serialVersionUID = -931248963141465409L;

		private CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer(this);

		@Override
		public Object getCellEditorValue() {
			//Log.v("getCellEditorValue");
			return getUserObject(tree.getSelectionPath());
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			//Log.v("isCellEditable " + event);
			if (!(event instanceof MouseEvent)) return false;
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			Object userObject = getUserObject(path);
			return (userObject instanceof IPlugIn || userObject instanceof PlugInPackage);
		}

		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			boolean selected = itemEvent.getStateChange() == ItemEvent.SELECTED;
			Object userObject = itemEvent.getSource();
			//Log.v("itemStateChanged " + itemEvent.getStateChange() + ", " + userObject);
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

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row) {
			//Log.v("getTreeCellEditorComponent() ");
			return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}
	}

	public PlugInSettingPanel() {
		setLayout(new BorderLayout());

		JPanel pluginTreePanel = new JPanel();
		pluginTreePanel.setLayout(new BoxLayout(pluginTreePanel, BoxLayout.Y_AXIS));

		tree = new JTree(root = new DefaultMutableTreeNode(TREE_NODE_TOP_PLUGINS));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new CheckBoxNodeRenderer());
		tree.setCellEditor(new CheckBoxNodeEditor());
		tree.setEditable(true);
		tree.setRootVisible(true);

		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setAlignmentX(0);

		JLabel label = new JLabel("Enable PlugIns : ");
		label.setAlignmentX(0);

		pluginTreePanel.add(label);
		pluginTreePanel.add(scrollPane);


		CardLayout extraPanelLayout = new CardLayout();
		extraPanel = new JPanel(extraPanelLayout);

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


		JPanel netSettingPanel = new JPanel();
		netSettingPanel.setLayout(new BoxLayout(netSettingPanel, BoxLayout.Y_AXIS));

		proxySettingPanel = new NetworkProxySettingPanel(null);
		proxySettingPanel.setAlignmentX(0);

		JButton btnTruststore = new JButton("Manage certificates");
		btnTruststore.setAlignmentX(0);
		btnTruststore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(tree.isSelectionEmpty()) return;
				Object userObject = getUserObject(tree.getSelectionPath().getParentPath());
				if(!(userObject instanceof PlugInPackage)) return;

				MessageBoxPane.showMessageDialog(null, new NetworkTruststoreSettingPanel((PlugInPackage)userObject), "Network Truststore Setting", JOptionPane.DEFAULT_OPTION);
			}
		});

		netSettingPanel.add(proxySettingPanel);
		netSettingPanel.add(btnTruststore);
		//netSettingPanel.add(new NetworkTruststoreSettingPanel(null));

		extraPanel.add(pluginDescPanel, TREE_NODE_DESCRIPTION);
		extraPanel.add(new JScrollPane(netSettingPanel), TREE_NODE_NETWORK_SETTING);
		extraPanel.add(new JPanel(), TREE_NODE_CONFIGURATION_SETTING);

		extraPanelLayout.show(extraPanel, TREE_NODE_DESCRIPTION);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitPane.setTopComponent(pluginTreePanel);
		splitPane.setBottomComponent(extraPanel);
		splitPane.setBorder(new EmptyBorder(0,0,0,0));
		splitPane.setDividerLocation(180);

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
		if(root.isLeaf()) {
			root.add(new DefaultMutableTreeNode(TREE_NODE_NO_PLUGINS));
		}
		tree.expandPath(new TreePath(root.getPath()));
		tree.updateUI();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object userObject = getUserObject(e.getPath());

		CardLayout extraPanelLayout = (CardLayout) extraPanel.getLayout();
		String layoutPage = TREE_NODE_DESCRIPTION;
		if(userObject instanceof IPlugIn) {
			description.setText(((IPlugIn)userObject).getDescription());
			description.setCaretPosition(0);
		} else if(userObject instanceof PlugInPackage) {
			description.setText(((PlugInPackage)userObject).getDescription());
			description.setCaretPosition(0);
		} else if(userObject instanceof String){
			switch((String)userObject) {
			case TREE_NODE_NETWORK_SETTING:
				layoutPage = TREE_NODE_NETWORK_SETTING;
				userObject = getUserObject(e.getPath().getParentPath());
				if(userObject instanceof PlugInPackage) {
					proxySettingPanel.setPluginPackage((PlugInPackage) userObject);
				} else {
					Log.w("Parent is not package : " + userObject);
				}
				break;
			case TREE_NODE_CONFIGURATION_SETTING:
				layoutPage = TREE_NODE_CONFIGURATION_SETTING;
				break;
			case TREE_NODE_TOP_PLUGINS:
				description.setText("APK Scanner Plugins");
				break;
			case TREE_NODE_NO_PLUGINS:
				description.setText("No plugins");
				break;
			default:
				Log.v("Unknown string node : " + userObject);;
				break;
			}
		} else {
			Log.v("Other: " + userObject.toString());
		}
		extraPanelLayout.show(extraPanel, layoutPage);
	}

	private Object getUserObject(TreePath path) {
		if(path == null) return null;
		Object userObject = path.getLastPathComponent();
		if(!(userObject instanceof DefaultMutableTreeNode)) return null;
		userObject = ((DefaultMutableTreeNode)userObject).getUserObject();
		return userObject;
	}
}
