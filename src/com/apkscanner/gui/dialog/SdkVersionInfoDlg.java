package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;

import com.apkscanner.gui.component.ImagePanel;
import com.apkscanner.gui.component.WindowSizeMemorizer;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class SdkVersionInfoDlg extends JDialog {
	private static final long serialVersionUID = -2587104139198073626L;

	private XmlPath sdkXmlPath;

	private ImagePanel sdkLogoImg;
	JComboBox<Integer> sdkVersions;
	JTextArea sdkInfoArea;

	public SdkVersionInfoDlg(Window owner) {
		this(owner, null);
	}

	public SdkVersionInfoDlg(Window owner, String xmlPath) {
		this(owner, xmlPath, -1);
	}

	public SdkVersionInfoDlg(Window owner, String xmlPath, int ver) {
		super(owner);
		initialize(owner);
		setSdkXml(xmlPath);
		setSdkVersion(ver);
	}

	private void initialize(Window window)
	{
		setTitle("SDK Info");
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(window);
		setModal(true);
		setLayout(new GridBagLayout());

		Dimension minSize = new Dimension(550, 270);
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(this, minSize);
		} else {
			setSize(minSize);
		}
		//setMinimumSize(minSize);
		WindowSizeMemorizer.registeComponent(this);

		sdkLogoImg = new ImagePanel();
		sdkInfoArea = new JTextArea();
		sdkInfoArea.setEditable(false);
		sdkVersions = new JComboBox<Integer>(new Integer[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28});
		sdkVersions.setRenderer(new ListCellRenderer<Integer>() {
			JLabel label;
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
					boolean isSelected, boolean cellHasFocus) {
				if(label == null) {
					label = new JLabel();
				}
				if(value > 0) {
					label.setText("SDK API Level " + value);
				} else {
					label.setText("SDK API Levels");
				}
				return label;
			}
		});
		sdkVersions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int value = (int)arg0.getItem();
				setSdkVersion(value);
			}
		});

		JButton btnExit = new JButton(Resource.STR_BTN_OK.getString());
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) 
		GridBagConstraints gridConst = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(10,10,0,10),0,0);

		gridConst.fill = GridBagConstraints.HORIZONTAL;
		this.add(sdkVersions, gridConst);

		gridConst.fill = GridBagConstraints.NONE;
		gridConst.anchor = GridBagConstraints.NORTH;
		gridConst.gridy++;
		this.add(sdkLogoImg, gridConst);

		gridConst.anchor = GridBagConstraints.CENTER;
		gridConst.gridy++;
		gridConst.gridwidth = 2;
		this.add(btnExit, gridConst);
		gridConst.gridy++;
		this.add(new JLabel(), gridConst);

		gridConst.gridx = 1;
		gridConst.gridy = 0;
		gridConst.gridwidth = 1;
		gridConst.gridheight = 2;
		gridConst.weightx = 1;
		gridConst.weighty = 1;
		gridConst.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(sdkInfoArea), gridConst);

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = -8988954049940512230L;
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	public void setSdkXml(String xmlPath) {
		if(xmlPath == null) {
			return;
		}
		try(InputStream xml = Resource.class.getResourceAsStream(xmlPath)) {
			if(xml != null) sdkXmlPath = new XmlPath(xml);
		} catch(IOException e) { }
		if(sdkXmlPath == null) {
			Log.w("Can not create XmlPath, xmlPath : " + xmlPath);
			return;
		}
		int maxSdk = sdkXmlPath.getCount("/resources/sdk-info");
		int preDefMaxSdk = sdkVersions.getItemCount() - 1;
		for(int ver = preDefMaxSdk + 1; ver <= maxSdk; ver++) {
			sdkVersions.addItem(ver);
		}
	}

	public void setSdkVersion(int sdkVer) {
		ImageIcon logoIcon = null;
		StringBuilder info = new StringBuilder();

		if(sdkVer > 0) {
			XmlPath sdkInfo = sdkXmlPath.getNode("/resources/sdk-info[@apiLevel='" + sdkVer + "']");

			if(sdkInfo != null) {
				info.append(sdkInfo.getAttribute("platformVersion"));
				info.append(" - " + sdkInfo.getAttribute("codeName"));
				info.append("\n\nAPI Level " + sdkVer);
				info.append("\nBuild.VERSION_CODES." + sdkInfo.getAttribute("versionCode"));

				logoIcon = new ImageIcon(Resource.class.getResource(sdkInfo.getAttribute("icon")));
			} else {
				info.append("API Level " + sdkVer);
				info.append("\nUnknown verion.\n\nYou can look at the sdk info in the Android developer site\n");
				info.append("http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels");

				logoIcon = new ImageIcon(Resource.class.getResource("/icons/logo/base.png"));
			}
		} else {
			XmlPath list = sdkXmlPath.getNodeList("/resources/sdk-info");
			for(int i=0; i < list.getCount(); i++) {
				info.append("API ");
				info.append(list.getAttribute(i, "apiLevel"));
				info.append(" : ");
				info.append(list.getAttribute(i, "platformVersion"));
				info.append(" - ");
				info.append(list.getAttribute(i, "codeName"));
				info.append("\n");
			}
			info.append("\nhttp://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels");

			logoIcon = new ImageIcon(Resource.class.getResource("/icons/logo/base.png"));
		}

		sdkInfoArea.setText(info.toString());
		sdkInfoArea.setCaretPosition(0);
		sdkLogoImg.setImage(logoIcon.getImage());
		if((int)sdkVersions.getSelectedItem() != sdkVer) {
			sdkVersions.setSelectedItem(sdkVer);
		}
	}
}
