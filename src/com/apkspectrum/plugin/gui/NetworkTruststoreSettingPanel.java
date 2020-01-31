package com.apkspectrum.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.apkspectrum.core.signer.SignatureReport;
import com.apkspectrum.plugin.NetworkSetting;
import com.apkspectrum.plugin.PlugInConfig;
import com.apkspectrum.plugin.PlugInPackage;
import com.apkspectrum.resource._RFile;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.swing.ApkFileChooser;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.util.Log;

public class NetworkTruststoreSettingPanel extends JPanel implements ActionListener, ListSelectionListener
{
	private static final long serialVersionUID = 7297978275508969293L;

	private static final String ACT_CMD_EXPLORER = "ACT_CMD_EXPLORER";
	private static final String ACT_CMD_IMPORT = "ACT_CMD_IMPORT";
	private static final String ACT_CMD_REMOVE = "ACT_CMD_REMOVE";
	private static final String ACT_CMD_DETAIL = "ACT_CMD_DETAIL";

	private static final String TRUSTSTORE_TYPE_APKSCANNER = "TRUSTSTORE_TYPE_APKSCANNER";
	private static final String TRUSTSTORE_TYPE_JVM = "TRUSTSTORE_TYPE_JVM";
	private static final String TRUSTSTORE_TYPE_MANUAL = "TRUSTSTORE_TYPE_MANUAL";
	private static final String TRUSTSTORE_TYPE_IGNORE = "TRUSTSTORE_TYPE_IGNORE";

	private JComboBox<String> trustStore;
	private JTextField trustPath;
	private JTable certList;
	private DefaultTableModel certListModel;
	private JTextArea certDescription;
	private JPanel mgmtPanel;
	private JButton explorerBtn;

	private HashMap<String, String> trustStoreTypeMap;

	private PlugInConfig pluginConfig;

	private KeyStore activeKeystore;
	private String activeKeystorePath;

	public NetworkTruststoreSettingPanel(final PlugInPackage pluginPackage) {
		this(pluginPackage, false);
	}

	public NetworkTruststoreSettingPanel(final PlugInPackage pluginPackage, boolean simple) {
		this(new PlugInConfig(pluginPackage), simple);
	}

	public NetworkTruststoreSettingPanel(final PlugInConfig pluginConfig) {
		this(pluginConfig, false);
	}

	public NetworkTruststoreSettingPanel(final PlugInConfig pluginConfig, boolean simple) {
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		Border title = new TitledBorder(_RStr.LABEL_TRUSTSTORE_SETTING.get());
		Border padding = new EmptyBorder(5,5,5,5);
		setBorder(new CompoundBorder(title, padding));

		String[] trustStoreList = new String[] {
			_RStr.TRUSTSTORE_APKSCANNER.get(),
			_RStr.TRUSTSTORE_JVM.get(),
			_RStr.TRUSTSTORE_MANUAL.get(),
			_RStr.TRUSTSTORE_IGNORE.get()
		};
		trustStoreTypeMap = new HashMap<>();
		trustStoreTypeMap.put(trustStoreList[0], TRUSTSTORE_TYPE_APKSCANNER);
		trustStoreTypeMap.put(trustStoreList[1], TRUSTSTORE_TYPE_JVM);
		trustStoreTypeMap.put(trustStoreList[2], TRUSTSTORE_TYPE_MANUAL);
		trustStoreTypeMap.put(trustStoreList[3], TRUSTSTORE_TYPE_IGNORE);

		trustStore = new JComboBox<String>(trustStoreList) {
			private static final long serialVersionUID = -595970244604500977L;
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };

        trustStore.setOpaque(false);
        trustStore.setAlignmentX(0.0f);
		trustStore.addItemListener(new ItemListener() {
			private Object preItem = null;
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String type = trustStoreTypeMap.get(e.getItem());
					if(type != null) {
						certList.clearSelection();

						String path = "";
						boolean visible = true;
						boolean needExplorer = false;
						boolean ignore = false;
						String desciprtion = "";
						String store = "";

						switch(type) {
						case TRUSTSTORE_TYPE_APKSCANNER:
							path = _RFile.SSL_TRUSTSTORE_PATH.get();
							store = NetworkSetting.APK_SCANNER_SSL_TRUSTSTORE;
							break;
						case TRUSTSTORE_TYPE_JVM:
							path = System.getProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
							store = NetworkSetting.JVM_SSL_TRUSTSTORE;
							break;
						case TRUSTSTORE_TYPE_MANUAL:
							path = pluginConfig.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE);
							if(!new File(path).canRead()) {
								//Log.e(path);
								File file = getKeyStoreFile(NetworkTruststoreSettingPanel.this, _RFile.SSL_TRUSTSTORE_PATH.get());
								if(file != null) {
									path = file.getAbsolutePath();
								}
							}
							store = path;
							needExplorer = true;
							break;
						case TRUSTSTORE_TYPE_IGNORE:
							visible = false;
							store = NetworkSetting.IGNORE_TRUSTSTORE;
							ignore = true;
							desciprtion = _RStr.MSG_WARN_SSL_IGNORE.get();
							break;
						}

						if(visible && new File(path).canRead()) {
							loadKeyStore(path);
							trustPath.setText(path);
							explorerBtn.setVisible(needExplorer);
						} else if(!ignore) {
							trustStore.setSelectedItem(preItem);
						}
						pluginConfig.setConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE, store);
						certDescription.setText(desciprtion);
						certDescription.setCaretPosition(0);
						mgmtPanel.setVisible(visible);
					}
				} else if(e.getStateChange() == ItemEvent.DESELECTED) {
					preItem = e.getItem();
				}
			}
		});

		add(trustStore);
		add(Box.createRigidArea(new Dimension(0,5)));

		mgmtPanel = new JPanel();
		mgmtPanel.setLayout(new BoxLayout(mgmtPanel, BoxLayout.Y_AXIS));
		trustPath = new JTextField();
		trustPath.setEditable(false);

		explorerBtn = new JButton("...");
		explorerBtn.setActionCommand(ACT_CMD_EXPLORER);
		explorerBtn.addActionListener(this);

		JPanel pathPanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = -762851494233546777L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
		};
		pathPanel.setAlignmentX(0.0f);
		pathPanel.add(trustPath, BorderLayout.CENTER);
		pathPanel.add(explorerBtn, BorderLayout.EAST);
		mgmtPanel.add(pathPanel);
		if(!simple) mgmtPanel.add(Box.createRigidArea(new Dimension(0,5)));

		certListModel = new DefaultTableModel(new String[] {
				_RStr.COLUMN_ISSUE_TO.get(),
				_RStr.COLUMN_ISSUE_BY.get(),
				_RStr.COLUMN_EXPIRES_ON.get(),
				_RStr.COLUMN_ALIAS.get()
			}, 0);
		certList = new JTable(certListModel);
		certList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		certList.getSelectionModel().addListSelectionListener(this);

		JScrollPane certListPanel = new JScrollPane(certList);
		certListPanel.setPreferredSize(new Dimension(150,100));
		certListPanel.setAlignmentX(0.0f);
		if(!simple) mgmtPanel.add(certListPanel);


		JButton importBtn = new JButton(_RStr.BTN_IMPORT.get());
		importBtn.setActionCommand(ACT_CMD_IMPORT);
		importBtn.addActionListener(this);

		JButton removeBtn = new JButton(_RStr.BTN_REMOVE.get());
		removeBtn.setActionCommand(ACT_CMD_REMOVE);
		removeBtn.addActionListener(this);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
			private static final long serialVersionUID = -8630147413462906817L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = 150;
                return max;
            }
		};
		buttonPanel.setAlignmentX(0.0f);
		buttonPanel.add(removeBtn);
		buttonPanel.add(importBtn);

		if(!simple) mgmtPanel.add(buttonPanel);

		add(mgmtPanel);
		if(!simple) add(Box.createRigidArea(new Dimension(0,5)));

		certDescription = new JTextArea();
		certDescription.setEditable(false);
		JScrollPane certDescPanel = new JScrollPane(certDescription) {
			private static final long serialVersionUID = -2429461232502187314L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = 150;
                return max;
            }
		};
		certDescPanel.setPreferredSize(new Dimension(150,100));
		certDescPanel.setAlignmentX(0.0f);
		//certDescPanel

		if(!simple) add(certDescPanel);
		if(!simple) add(Box.createRigidArea(new Dimension(0,5)));

		if(simple) {
			JButton detailTruststore = new JButton(_RStr.BTN_MANAGE_CERT.get());
			detailTruststore.setActionCommand(ACT_CMD_DETAIL);
			detailTruststore.addActionListener(this);
			add(detailTruststore);
		}

		setPluginPackage(pluginConfig);
		certListModel.fireTableStructureChanged();
	}

	public void setPluginPackage(PlugInPackage pluginPackage) {
		setPluginPackage(new PlugInConfig(pluginPackage));
	}

	public void setPluginPackage(PlugInConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
		String trustStoreType = null;

		String storePath = pluginConfig.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE, NetworkSetting.APK_SCANNER_SSL_TRUSTSTORE);
		if(NetworkSetting.APK_SCANNER_SSL_TRUSTSTORE.equals(storePath)) {
			trustStoreType = TRUSTSTORE_TYPE_APKSCANNER;
		} else if(NetworkSetting.JVM_SSL_TRUSTSTORE.equals(storePath)) {
			trustStoreType = TRUSTSTORE_TYPE_JVM;
		} else if(NetworkSetting.IGNORE_TRUSTSTORE.equals(storePath)) {
			trustStoreType = TRUSTSTORE_TYPE_IGNORE;
		} else {
			trustStoreType = TRUSTSTORE_TYPE_MANUAL;
		}

		trustStore.setSelectedItem(null);
		for(Entry<String, String> entry: trustStoreTypeMap.entrySet()) {
			if(trustStoreType.equals(entry.getValue())) {
				trustStore.setSelectedItem(entry.getKey());
				break;
			}
		}
	}

	public String confirmCertificateAlias(String alias, X509Certificate cert) {
		JPanel confirmForm = new JPanel();

		JTextField aliasField = new JTextField();
		aliasField.setText(alias);
		aliasField.setAlignmentX(0.0f);

		JTextArea descArea = new JTextArea();
		descArea.setEditable(false);
		descArea.setText(new SignatureReport(cert).toString());
		descArea.setCaretPosition(0);

		JScrollPane descScroll = new JScrollPane(descArea);
		descScroll.setAlignmentX(0.0f);
		descScroll.setPreferredSize(new Dimension(400, 150));

		confirmForm.setLayout(new BoxLayout(confirmForm, BoxLayout.Y_AXIS));
		confirmForm.add(new JLabel(_RStr.LABEL_ALIAS.get()));
		confirmForm.add(aliasField);
		confirmForm.add(new JLabel(_RStr.LABEL_DESCRIPTION.get()));
		confirmForm.add(descScroll);

		do {
			int confirm = MessageBoxPane.showConfirmDialog(this, confirmForm, _RStr.LABEL_QUESTION.get(),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			if(confirm == JOptionPane.OK_OPTION) {
				alias = aliasField.getText().trim();
				try {
					if(activeKeystore.containsAlias(alias)) {
						MessageBoxPane.showWarring(this, "Already existed alias name");
						continue;
					}
				} catch (KeyStoreException e) {
					e.printStackTrace();
					continue;
				}
				if(alias.isEmpty()) {
					MessageBoxPane.showWarring(this, "Empty alilas name.");
					continue;
				}
				return alias;
			}
			break;
		} while(true);

		return null;
	}

	public File getKeyStoreFile(Component component, String defaultFilePath)
	{
		JFileChooser jfc = ApkFileChooser.getFileChooser("", JFileChooser.OPEN_DIALOG, new File(defaultFilePath));

		if(jfc.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;

		File dir = jfc.getSelectedFile();
		//if(dir != null) {
			//Resource.PROP_LAST_FILE_SAVE_PATH.setData(dir.getParentFile().getAbsolutePath());
		//}
		return dir;
	}

	public File getCertificateFile(Component component, String defaultFilePath)
	{
		JFileChooser jfc = ApkFileChooser.getFileChooser("", JFileChooser.OPEN_DIALOG, new File(defaultFilePath));
		jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("X.509 Certificate(*.cer,*.crt)","cer","crt"));

		if(jfc.showOpenDialog(component) != JFileChooser.APPROVE_OPTION)
			return null;

		File dir = jfc.getSelectedFile();
		//if(dir != null) {
			//Resource.PROP_LAST_FILE_SAVE_PATH.setData(dir.getParentFile().getAbsolutePath());
		//}
		return dir;
	}

	private X509Certificate loadCertificate(File certFile) {
		Certificate cert = null;
		try(FileInputStream is = new FileInputStream(certFile))  {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = cf.generateCertificate(is);
		} catch (IOException | CertificateException e1) {
			e1.printStackTrace();
		}
		return (X509Certificate) cert;
	}

	private void importCertificate(String alias, X509Certificate certificate) {
		if(certificate == null || alias == null || alias.trim().isEmpty() || activeKeystore == null) return;
		try {
			activeKeystore.setCertificateEntry(alias, certificate);
			storeKeyStore();
			Object[] data = makeRowObject(alias, certificate);
			if(data != null) {
				certListModel.addRow(data);
			}
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		}
	}

	private void removeCertificate(String alias) {
		if(activeKeystore == null) return;
		try {
			activeKeystore.deleteEntry(alias);
			storeKeyStore();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}

	public Object[] makeRowObject(String alias, X509Certificate cert) {
		if(cert == null) return null;

		String subCn = null;
		String issCn = null;
		LdapName ldapDN = null;
		try {
			ldapDN = new LdapName(cert.getSubjectX500Principal().getName());
			for(Rdn rdn: ldapDN.getRdns()) {
				if (rdn.getType().toUpperCase().equals("CN")) {
					subCn = (String) rdn.getValue();
					break;
				}
			}

			ldapDN = new LdapName(cert.getIssuerX500Principal().getName());
			for(Rdn rdn: ldapDN.getRdns()) {
				if (rdn.getType().toUpperCase().equals("CN")) {
					issCn = (String) rdn.getValue();
					break;
				}
			}
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
		if(subCn == null) subCn = "null";
		if(issCn == null) issCn = "null";

		return new Object[] {subCn, issCn, new SimpleDateFormat("YYYY-MM-dd").format(cert.getNotAfter()), alias};
	}

	private void loadKeyStore(String path) {
		certListModel.getDataVector().clear();

	    activeKeystore = null;
	    activeKeystorePath = null;
		try(FileInputStream is = new FileInputStream(path)) {
			activeKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
			char[] pass = pluginConfig.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE_PWD, NetworkSetting.DEFAULT_TRUSTSTORE_PASSWORD).toCharArray();
		    activeKeystore.load(is, pass);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			e.printStackTrace();
			activeKeystore = null;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			activeKeystore = null;
			return;
		}
		activeKeystorePath = path;

		try {
			Enumeration<String> itor = activeKeystore.aliases();
			while(itor.hasMoreElements()) {
				String alias = itor.nextElement();
				String format = activeKeystore.getCertificate(alias).getPublicKey().getFormat();
				if("X.509".equals(format)) {
					X509Certificate cert = (X509Certificate) activeKeystore.getCertificate(alias);
					Object[] data = makeRowObject(alias, cert);
					if(data != null) {
						certListModel.addRow(data);
					}
				}
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}

	private void storeKeyStore() {
		if(activeKeystore == null || activeKeystorePath == null) return;
		try(FileOutputStream os = new FileOutputStream(activeKeystorePath)) {
			char[] pass = pluginConfig.getConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE_PWD, NetworkSetting.DEFAULT_TRUSTSTORE_PASSWORD).toCharArray();
			activeKeystore.store(os, pass);
		} catch(KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		switch(arg0.getActionCommand()) {
		case ACT_CMD_EXPLORER:
			File file = getKeyStoreFile(NetworkTruststoreSettingPanel.this, trustPath.getText());
			if(file != null) {
				String path = file.getAbsolutePath();
				trustPath.setText(path);
				pluginConfig.setConfiguration(PlugInConfig.CONFIG_SSL_TRUSTSTORE, path);
			}
			break;
		case ACT_CMD_IMPORT:
			File certFile = getCertificateFile(this, "");
			if(certFile == null || !certFile.canRead()) break;

			X509Certificate cert = loadCertificate(certFile);
			String name = certFile.getName().replaceAll("\\.([cC][rR][tT]|[cC][eE][rR])$", "");
			name = confirmCertificateAlias(name, cert);
			if(name != null) {
				importCertificate(name, cert);
			}
			break;
		case ACT_CMD_REMOVE:
			int row = certList.getSelectedRow();
			if(row > -1) {
				//need confirm popup
				int ret = MessageBoxPane.showConfirmDialog(this, "Warning. Other applications and systems may be affected."
						+ " continue deleting? ", _RStr.LABEL_QUESTION.get(),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
				Log.e(" " + ret + " , " + MessageBoxPane.OK_OPTION + ", " + MessageBoxPane.NO_OPTION);
				if(ret == JOptionPane.OK_OPTION) {
					String alias = (String) certListModel.getValueAt(row, 3);
					certListModel.removeRow(row);
					removeCertificate(alias);
				}
			} else {
				Log.v("No selected");
				MessageBoxPane.showWarring(this, "No selected");
			}
			break;
		case ACT_CMD_DETAIL:
			JPanel trustPanel = new NetworkTruststoreSettingPanel(pluginConfig);
			trustPanel.setPreferredSize(new Dimension(500, trustPanel.getPreferredSize().height));
			MessageBoxPane.showMessageDialog(null, trustPanel, _RStr.LABEL_TRUSTSTORE_SETTING.get(), JOptionPane.DEFAULT_OPTION);
			break;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if(evt.getValueIsAdjusting() || activeKeystore == null) return;

		int row = certList.getSelectedRow();
		if(row == -1) return;
		String alias = (String) certListModel.getValueAt(row, 3);

		try {
			Certificate cert = activeKeystore.getCertificate(alias);
			if("X.509".equals(cert.getPublicKey().getFormat())) {
				SignatureReport report = new SignatureReport((X509Certificate) cert);
				certDescription.setText(report.toString());
				certDescription.setCaretPosition(0);
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}
}
