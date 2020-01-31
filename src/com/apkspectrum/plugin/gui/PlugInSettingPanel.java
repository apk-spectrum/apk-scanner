package com.apkspectrum.plugin.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.apkspectrum.plugin.IPlugIn;
import com.apkspectrum.plugin.PlugInGroup;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.plugin.PlugInPackage;
import com.apkspectrum.resource._RImg;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.swing.ImageScaler;
import com.apkspectrum.util.Log;

public class PlugInSettingPanel extends JPanel implements TreeSelectionListener {
	private static final long serialVersionUID = 1234825421488294264L;

	private static final String TREE_NODE_TOP_PLUGINS = "TREE_NODE_TOP_PLUGINS";
	private static final String TREE_NODE_DESCRIPTION = "TREE_NODE_DESCRIPTION";
	private static final String TREE_NODE_NETWORK_SETTING = "TREE_NODE_NETWORK_SETTING";
	private static final String TREE_NODE_CONFIGURATION_SETTING = "TREE_NODE_CONFIGURATION_SETTING";
	private static final String TREE_NODE_GLOBAL_SETTINGS = "TREE_NODE_GLOBAL_SETTINGS";
	private static final String TREE_NODE_NO_PLUGINS = "TREE_NODE_NO_PLUGINS";

	private JTree tree;
	private DefaultMutableTreeNode root;
	private JTextArea description;
	private JPanel extraPanel;

	private NetworkProxySettingPanel proxySettingPanel;
	private NetworkTruststoreSettingPanel trustSettingPanel;
	private ConfigurationsSettingPanel confSettingPanel;

	static class CheckBoxNodeRenderer implements TreeCellRenderer {
		public static Color selectionBorderColor, selectionForeground, selectionBackground,
		      textForeground, textBackground, textDisabled;

		static {
			selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
			selectionForeground = UIManager.getColor("Tree.selectionForeground");
			selectionBackground = UIManager.getColor("Tree.selectionBackground");
			textForeground = UIManager.getColor("Tree.textForeground");
			textBackground = UIManager.getColor("Tree.textBackground");
			textDisabled = Color.LIGHT_GRAY; // UIManager.getColor("CheckBox.disabledText")
		}

		private ItemListener listener;
		private HashMap<Object, NodeComponent> compCached = new HashMap<>();
		private class NodeComponent extends JPanel {
			private static final long serialVersionUID = -5852428171246328514L;
			JCheckBox check;
			JLabel label;
			private NodeComponent(ItemListener listener) {
				setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
				setBorder(new EmptyBorder(0,0,0,0));
				//setOpaque(false);

				check = new JCheckBox();
				check.setBorder(new EmptyBorder(2,0,0,0));
				check.setOpaque(false);
				if(listener != null) check.addItemListener(listener);

				Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
				check.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));

				label = new JLabel();
				label.setBorder(new EmptyBorder(0,0,0,0));
				label.setOpaque(false);

				Font fontValue = UIManager.getFont("Tree.font");
				if (fontValue != null) label.setFont(fontValue);

				add(check);
				add(label);
			}
		}

		public CheckBoxNodeRenderer() {
			//Log.v("CheckBoxNodeRenderer() Constructor");
		}

		public CheckBoxNodeRenderer(ItemListener listener) {
			//Log.v("CheckBoxNodeRenderer() Constructor, " + listener);
			this.listener = listener;
		}

		private NodeComponent makeNodeComponent(Object userObject, String text, Icon icon, boolean selected,
				boolean usedCheckbox, boolean enabledCheckBox, boolean selectedCheckBox) {
			NodeComponent nodeComp = null;
			if(compCached.containsKey(userObject)) {
				nodeComp = compCached.get(userObject);
			}
			if(nodeComp == null) {
				final Object source = userObject;
				nodeComp = new NodeComponent(listener == null ? null : new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent ie) {
						ie.setSource(source);
						listener.itemStateChanged(ie);
					}
				});
				compCached.put(source, nodeComp);
			}

			nodeComp.setForeground(selected ? selectionForeground : textForeground);
			nodeComp.setBackground(selected ? selectionBackground : textBackground);

			nodeComp.check.setVisible(usedCheckbox);
			nodeComp.check.setEnabled(enabledCheckBox);
			nodeComp.check.setSelected(selectedCheckBox);

			boolean isLabelEnabled = (!usedCheckbox || (selectedCheckBox && enabledCheckBox));
			if(!selected) nodeComp.label.setForeground(isLabelEnabled ? textForeground : textDisabled);
			nodeComp.label.setText(text);
			nodeComp.label.setIcon(icon);
			if(!isLabelEnabled) {
				nodeComp.label.setIcon(nodeComp.label.getDisabledIcon());
			}

			return nodeComp;
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
						nodeText = _RStr.TREE_NODE_NETWORK.get();
						nodeIcon = _RImg.TREE_NETWORK_SETTING.getImageIcon(16, 16);
						break;
					case TREE_NODE_CONFIGURATION_SETTING:
						nodeText = _RStr.TREE_NODE_CONFIGURATION.get();
						nodeIcon = _RImg.TREE_CONFIG_SETTING.getImageIcon(16, 16);
						break;
					case TREE_NODE_GLOBAL_SETTINGS:
						nodeText = _RStr.TREE_NODE_GLOBAL_SETTING.get();
						nodeIcon = _RImg.TREE_GLOBAL_SETTING.getImageIcon(16, 16);
						break;
					case TREE_NODE_TOP_PLUGINS:
						nodeText = _RStr.TREE_NODE_PLUGINS_TOP.get();
						nodeIcon = PlugInManager.getAppImage().getImageIcon(16, 16);
						break;
					case TREE_NODE_NO_PLUGINS:
						nodeText = _RStr.TREE_NODE_NO_PLUGINS.get();
						break;
					}
				} else if(userObject instanceof IPlugIn) {
					IPlugIn plugin = (IPlugIn) userObject;
					URL iconUrl = plugin.getIconURL();
					if(iconUrl != null) {
						nodeIcon = ImageScaler.getScaledImageIcon(new ImageIcon(iconUrl), 16, 16);
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
						nodeIcon = ImageScaler.getScaledImageIcon(new ImageIcon(iconUrl), 16, 16);
					}
					nodeText = pluginPackage.getLabel() + " / " + pluginPackage.getVersionName();
					visibledCheckBox = true;
					enabledCheckBox = tree.isEnabled();
					selectedCheckBox = pluginPackage.isEnabled();
				}
			}

			return makeNodeComponent(userObject, nodeText, nodeIcon, selected, visibledCheckBox, enabledCheckBox, selectedCheckBox);
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

		Border title = new TitledBorder(_RStr.LABEL_PLUGINS_SETTINGS.get());
		Border padding = new EmptyBorder(5,5,5,5);
		pluginTreePanel.setBorder(new CompoundBorder(title, padding));

		tree = new JTree(root = new DefaultMutableTreeNode(TREE_NODE_TOP_PLUGINS));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new CheckBoxNodeRenderer());
		tree.setCellEditor(new CheckBoxNodeEditor());
		tree.setEditable(true);
		tree.setRootVisible(true);

		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setAlignmentX(0);

		pluginTreePanel.add(scrollPane);


		CardLayout extraPanelLayout = new CardLayout();
		extraPanel = new JPanel(extraPanelLayout);

		JPanel pluginDescPanel = new JPanel();
		pluginDescPanel.setLayout(new BoxLayout(pluginDescPanel, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(_RStr.LABEL_PLUGINS_DESCRIPTION.get());
		label.setAlignmentX(0);

		description = new JTextArea();
		description.setEditable(false);

		scrollPane = new JScrollPane(description);
		scrollPane.setAlignmentX(0);

		pluginDescPanel.add(label);
		pluginDescPanel.add(scrollPane);

		JPanel netSettingPanel = new JPanel();
		netSettingPanel.setLayout(new BoxLayout(netSettingPanel, BoxLayout.X_AXIS));

		proxySettingPanel = new NetworkProxySettingPanel(null) {
			private static final long serialVersionUID = 3084256038830919672L;
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(200, super.getPreferredSize().height);
			}
		};
		proxySettingPanel.setAlignmentY(0);

		trustSettingPanel = new NetworkTruststoreSettingPanel((PlugInPackage)null, true){
			private static final long serialVersionUID = -6888382898932060788L;
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(200, super.getPreferredSize().height);
			}
		};
		trustSettingPanel.setAlignmentY(0);

		confSettingPanel = new ConfigurationsSettingPanel(null);

		netSettingPanel.add(proxySettingPanel);
		netSettingPanel.add(trustSettingPanel);
		//netSettingPanel.add(new NetworkTruststoreSettingPanel(null));

		extraPanel.add(pluginDescPanel, TREE_NODE_DESCRIPTION);
		extraPanel.add(new JScrollPane(netSettingPanel), TREE_NODE_NETWORK_SETTING);
		extraPanel.add(confSettingPanel, TREE_NODE_CONFIGURATION_SETTING);

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
			if(pack.isEnabled()) {
				tree.expandPath(new TreePath(packNode.getPath()));
			}
		}
		if(root.isLeaf()) {
			root.add(new DefaultMutableTreeNode(TREE_NODE_NO_PLUGINS));
		}
		DefaultMutableTreeNode commSetNode = new DefaultMutableTreeNode(TREE_NODE_GLOBAL_SETTINGS);
		commSetNode.add(new DefaultMutableTreeNode(TREE_NODE_NETWORK_SETTING));
		commSetNode.add(new DefaultMutableTreeNode(TREE_NODE_CONFIGURATION_SETTING));
		root.add(commSetNode);

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
				if(!(userObject instanceof PlugInPackage)) {
					if(userObject instanceof String) {
						userObject = null;
					} else {
						Log.w("Parent is not package : " + userObject);
						break;
					}
				}
				proxySettingPanel.setPluginPackage((PlugInPackage) userObject);
				trustSettingPanel.setPluginPackage((PlugInPackage) userObject);
				break;
			case TREE_NODE_CONFIGURATION_SETTING:
				layoutPage = TREE_NODE_CONFIGURATION_SETTING;
				userObject = getUserObject(e.getPath().getParentPath());
				if(!(userObject instanceof PlugInPackage)) {
					if(userObject instanceof String) {
						userObject = null;
					} else {
						Log.w("Parent is not package : " + userObject);
						break;
					}
				}
				confSettingPanel.setPluginPackage((PlugInPackage) userObject);
				break;
			case TREE_NODE_GLOBAL_SETTINGS:
				description.setText(_RStr.TREE_NODE_GLOBAL_SETTING_DESC.get());
				break;
			case TREE_NODE_TOP_PLUGINS:
				description.setText(_RStr.TREE_NODE_PLUGINS_TOP_DESC.get());
				break;
			case TREE_NODE_NO_PLUGINS:
				description.setText(_RStr.TREE_NODE_NO_PLUGINS_DESC.get());
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
