package com.apkspectrum.plugin.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.apkspectrum.plugin.PlugInConfig;
import com.apkspectrum.plugin.PlugInPackage;
import com.apkspectrum.resource._RStr;

public class NetworkProxySettingPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 7135761307346885928L;

	private static final String CARD_NO_PROXY = "DESC_NO_PROXY";
	private static final String CARD_SCANNER_PROXY = "CARD_SCANNER_PROXY";
	private static final String CARD_SYSTEM_PROXY = "CARD_SYSTEM_PROXY";
	private static final String CARD_PAC_SCRIPT_PROXY = "CARD_PAC_SCRIPT_PROXY";
	private static final String CARD_MANUAL_PROXY = "DESC_MANUAL_PROXY";

	private static final String ACT_CMD_APPLY = "ACT_CMD_APPLY";

	private JPanel descPanel;
	private JComboBox<String> methods;
	private JTextField pacUrl, proxySet[] = new JTextField[8];

	private HashMap<String, String> methodCardMap;
	private PlugInConfig pluginConfig;

	public NetworkProxySettingPanel(PlugInPackage pluginPackage) {
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Border title = new TitledBorder(_RStr.LABEL_PROXY_SETTING.get());
		Border padding = new EmptyBorder(5,5,5,5);
		setBorder(new CompoundBorder(title, padding));

		String[] proxyMethods = new String[] {
			_RStr.PROXY_MENU_NO_PROXY.get(),
			_RStr.PROXY_MENU_GLOBAL.get(),
			_RStr.PROXY_MENU_SYSTEM.get(),
			_RStr.PROXY_MENU_PAC_SCRIPT.get(),
			_RStr.PROXY_MENU_MANUAL.get()
		};
		methodCardMap = new HashMap<>(proxyMethods.length);
		methodCardMap.put(proxyMethods[0], CARD_NO_PROXY);
		methodCardMap.put(proxyMethods[1], CARD_SCANNER_PROXY);
		methodCardMap.put(proxyMethods[2], CARD_SYSTEM_PROXY);
		methodCardMap.put(proxyMethods[3], CARD_PAC_SCRIPT_PROXY);
		methodCardMap.put(proxyMethods[4], CARD_MANUAL_PROXY);

		methods = new JComboBox<String>(proxyMethods) {
			private static final long serialVersionUID = -595970244604500977L;
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };

        final CardLayout descLayout = new CardLayout();

		methods.setOpaque(false);
		methods.setAlignmentX(1.0f);
		methods.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String card = methodCardMap.get(e.getItem());
					descLayout.show(descPanel, card);
				}
			}
		});

		add(methods);
		add(Box.createRigidArea(new Dimension(0,5)));

		descPanel = new JPanel(descLayout){
			private static final long serialVersionUID = 5641545914222359307L;
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                for (Component comp : getComponents()) {
                    if (comp.isVisible()) {
                        size.height = comp.getPreferredSize().height;
                        break;
                    }
                }
                return size;
            }
		};
		descPanel.setAlignmentX(1.0f);
		descPanel.add(new JLabel(proxyMethods[0]), CARD_NO_PROXY);
		descPanel.add(new JLabel(proxyMethods[1]), CARD_SCANNER_PROXY);
		descPanel.add(new JLabel(proxyMethods[2]), CARD_SYSTEM_PROXY);
		descPanel.add(makePacFileds(), CARD_PAC_SCRIPT_PROXY);
		descPanel.add(makeProxyFileds(), CARD_MANUAL_PROXY);
		descLayout.show(descPanel, CARD_NO_PROXY);

		add(descPanel, CARD_MANUAL_PROXY);

		add(Box.createRigidArea(new Dimension(0,5)));

		JButton applyBtn = new JButton(_RStr.BTN_APPLY.get());
		applyBtn.setAlignmentX(1.0f);
		applyBtn.setActionCommand(ACT_CMD_APPLY);
		applyBtn.addActionListener(this);
		add(applyBtn);
		//add(Box.createVerticalGlue());
		//setSize(500, 400);

		setPluginPackage(pluginPackage);
	}

	@Override
    public Dimension getMaximumSize() {
		Dimension max = super.getMaximumSize();
        max.height = getPreferredSize().height;
        this.repaint();
        return max;
    }

	private JPanel makeProxyFileds() {
		JPanel proxyFilds = new JPanel();
		proxyFilds.setLayout(new GridBagLayout());
		proxyFilds.setAlignmentX(1.0f);

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady)
		GridBagConstraints rowHeadConst = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(10,10,0,10),0,0);
		GridBagConstraints proxyIpConst = new GridBagConstraints(1,0,1,1,0.7,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL ,new Insets(10,0,0,0),0,0);
		GridBagConstraints proxyPortConst = new GridBagConstraints(2,0,1,1,0.3,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(10,10,0,0),0,0);

		proxyFilds.add(new JLabel("HTTP Proxy"), rowHeadConst);
		proxyFilds.add(proxySet[0] = new JTextField(), proxyIpConst);
		proxyFilds.add(proxySet[1] = new JTextField(), proxyPortConst);

		rowHeadConst.gridy++;
		proxyIpConst.gridy++;
		proxyPortConst.gridy++;

		proxyFilds.add(new JLabel("HTTPS Proxy"), rowHeadConst);
		proxyFilds.add(proxySet[2] = new JTextField(), proxyIpConst);
		proxyFilds.add(proxySet[3] = new JTextField(), proxyPortConst);
		/*
		rowHeadConst.gridy++;
		proxyIpConst.gridy++;
		proxyPortConst.gridy++;

		proxyFilds.add(new JLabel("FTP Proxy"), rowHeadConst);
		proxyFilds.add(proxySet[4] = new JTextField(), proxyIpConst);
		proxyFilds.add(proxySet[5] = new JTextField(), proxyPortConst);

		rowHeadConst.gridy++;
		proxyIpConst.gridy++;
		proxyPortConst.gridy++;

		proxyFilds.add(new JLabel("Socks Host"), rowHeadConst);
		proxyFilds.add(proxySet[6] = new JTextField(), proxyIpConst);
		proxyFilds.add(proxySet[7] = new JTextField(), proxyPortConst);
		 */
		return proxyFilds;
	}

	private JPanel makePacFileds() {
		JPanel pacFilds = new JPanel();
		pacFilds.setLayout(new BoxLayout(pacFilds, BoxLayout.Y_AXIS));
		pacFilds.add(Box.createRigidArea(new Dimension(0,10)));
		pacFilds.add(new JLabel(_RStr.LABEL_PAC_SCRIPT_URL.get()));
		pacFilds.add(Box.createRigidArea(new Dimension(0,5)));
		pacFilds.add(pacUrl = new JTextField());
		return pacFilds;
	}

	public void setPluginPackage(PlugInPackage pluginPackage) {
		pluginConfig = new PlugInConfig(pluginPackage);
		int scannerIdx = getCardIdxFromCombobox(CARD_SCANNER_PROXY);
		if(pluginPackage == null && scannerIdx > -1) {
			methods.removeItemAt(scannerIdx);
		} else if(pluginPackage != null && scannerIdx == -1) {
			methods.insertItemAt(_RStr.PROXY_MENU_GLOBAL.get(), 1);
		}

		boolean useGlobalConfig = "true".equals(pluginConfig.getConfiguration(PlugInConfig.CONFIG_USE_GLOBAL_PROXIES, "true"));

		boolean useSystemProxy = "true".equals(pluginConfig.getConfiguration(PlugInConfig.CONFIG_USE_SYSTEM_PROXIES,
												useGlobalConfig ? "true" : "false"));
		boolean usePacProxy = "true".equals(pluginConfig.getConfiguration(PlugInConfig.CONFIG_USE_PAC_PROXIES, "false"));

		boolean noProxy = "true".equals(pluginConfig.getConfiguration(PlugInConfig.CONFIG_NO_PROXIES, "false"));

		int selIdx = -1;
		if(useGlobalConfig) {
			selIdx = getCardIdxFromCombobox(CARD_SCANNER_PROXY);
		} else if(useSystemProxy) {
			selIdx = getCardIdxFromCombobox(CARD_SYSTEM_PROXY);
		} else if(usePacProxy) {
			selIdx = getCardIdxFromCombobox(CARD_PAC_SCRIPT_PROXY);
		} else if(noProxy) {
			selIdx = getCardIdxFromCombobox(CARD_NO_PROXY);
		} else { // manual
			selIdx = getCardIdxFromCombobox(CARD_MANUAL_PROXY);
		}
		methods.setSelectedIndex(selIdx > -1 ? selIdx : getCardIdxFromCombobox(CARD_SYSTEM_PROXY));

		pacUrl.setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_PAC_URL, ""));
		proxySet[0].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
		proxySet[1].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_PORT, ""));
		proxySet[2].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTPS_PROXY_HOST, ""));
		proxySet[3].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTPS_PROXY_PORT, ""));
		/*
		proxySet[4].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
		proxySet[5].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
		proxySet[6].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
		proxySet[7].setText(pluginConfig.getConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, ""));
		*/
	}

	private int getCardIdxFromCombobox(String card) {
		if(card == null) return -1;
		int size = methods.getItemCount();
		for(int i = 0; i < size; i++) {
			if(card.equals(methodCardMap.get(methods.getItemAt(i)))) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(ACT_CMD_APPLY.equals(e.getActionCommand())) {
			pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_GLOBAL_PROXIES, "false");
			pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_SYSTEM_PROXIES, "false");
			pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_PAC_PROXIES, "false");
			pluginConfig.setConfiguration(PlugInConfig.CONFIG_NO_PROXIES, "false");

			switch(methodCardMap.get(methods.getSelectedItem())) {
			case CARD_SCANNER_PROXY:
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_GLOBAL_PROXIES, "true");
				break;
			case CARD_SYSTEM_PROXY:
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_SYSTEM_PROXIES, "true");
				break;
			case CARD_PAC_SCRIPT_PROXY:
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_USE_PAC_PROXIES, "true");
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_PAC_URL, pacUrl.getText());
				break;
			case CARD_NO_PROXY:
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_NO_PROXIES, "true");
				break;
			case CARD_MANUAL_PROXY:
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_HOST, proxySet[0].getText());
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_HTTP_PROXY_PORT, proxySet[1].getText());
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_HTTPS_PROXY_HOST, proxySet[2].getText());
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_HTTPS_PROXY_PORT, proxySet[3].getText());
				break;
			default:
				break;
			}
		}
	}
}
