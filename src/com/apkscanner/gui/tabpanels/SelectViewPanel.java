package com.apkscanner.gui.tabpanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;

public class SelectViewPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -5260902185163996992L;

	public final static int SELECT_VIEW_NONE = 0;
	public final static int SELECT_VIEW_ICON_OPEN = 0x01;
	public final static int SELECT_VIEW_ICON_JD_OPEN = 0x02;
	public final static int SELECT_VIEW_ICON_SCANNER_OPEN = 0x04;
	public final static int SELECT_VIEW_ICON_EXPLORER = 0x08;
	public final static int SELECT_VIEW_ICON_TEXTVIEWER = 0x10;
	public final static int SELECT_VIEW_ICON_CHOOSE_APPLICATION = 0x20;

	public static final String ACT_CMD_OPEN_WITH_TEXTVIEWER	= "ACT_CMD_OPEN_WITH_TEXTVIEWER";

	private HashMap<ButtonSet, JButton> buttonMap;
	private JLabel openWithLabel;

	private ResourceObject resObj;
	private ActionListener listener;

	public enum ButtonSet {
		OS_SETTING			(SELECT_VIEW_ICON_OPEN, RComp.BTN_OPEN_WITH_SYSTEM_SET, UiEventHandler.ACT_CMD_OPEN_RESOURCE_FILE_SYSTEM_SET),
		JD_GUI				(SELECT_VIEW_ICON_JD_OPEN, RComp.BTN_OPEN_WITH_JD_GUI, RComp.BTN_OPEN_WITH_LOADING, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_JDGUI),
		JADX_GUI			(SELECT_VIEW_ICON_JD_OPEN, RComp.BTN_OPEN_WITH_JADX_GUI, RComp.BTN_OPEN_WITH_LOADING, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_JADXGUI),
		BYTECODE_VIEWER		(SELECT_VIEW_ICON_JD_OPEN, RComp.BTN_OPEN_WITH_BYTECODE_VIEWER, RComp.BTN_OPEN_WITH_LOADING, UiEventHandler.ACT_CMD_OPEN_DECOMPILER_BYTECODE),
		APK_SCANNER			(SELECT_VIEW_ICON_SCANNER_OPEN, RComp.BTN_OPEN_WITH_APK_SCANNER, UiEventHandler.ACT_CMD_OPEN_RESOURCE_FILE_APK_SCANNER),
		EXPLORER			(SELECT_VIEW_ICON_EXPLORER, RComp.BTN_OPEN_WITH_EXPLORER, UiEventHandler.ACT_CMD_OPEN_RESOURCE_FILE_ARCHIVE),
		OPEN_TO_TEXTVIEWER	(SELECT_VIEW_ICON_TEXTVIEWER, RComp.BTN_OPEN_WITH_TEXTVIEWER, ACT_CMD_OPEN_WITH_TEXTVIEWER),
		CHOOSE_APPLICATION	(SELECT_VIEW_ICON_CHOOSE_APPLICATION, RComp.BTN_OPEN_WITH_CHOOSER, ""),
		; // ENUM END

		static private final int IconSize = 80;

		private RComp res;
		private RComp disabledRes;
		private String actionCommand = null;
		private int id = -1;

		ButtonSet(int id, RComp res, String actCommand)
		{
			this(id, res, actCommand, false);
		}

		ButtonSet(int id, RComp res, String actCommand, boolean extension)
		{
			this(id, res, null, actCommand, extension);
		}

		ButtonSet(int id, RComp res, RComp disabledRes, String actCommand)
		{
			this(id,  res, disabledRes, actCommand, false);
		}

		ButtonSet(int id, RComp res, RComp disabledRes, String actCommand, boolean extension)
		{
			this.id = id;
			this.res = res;
			this.disabledRes = disabledRes;
			this.actionCommand = actCommand != null ? actCommand : getClass().getName()+"."+this.toString();
			res.setImageSize(new Dimension(IconSize, IconSize));
		}

		public boolean matchActionEvent(ActionEvent e)
		{
			return actionCommand.equals(e.getActionCommand());
		}

		public int getButtonId() {
			return id;
		}

		private JButton getButton(ActionListener listener)
		{
			final JButton button = new JButton() {
				private static final long serialVersionUID = 4077420312263705720L;
				@Override
				public void setEnabled(boolean enabled) {
					super.setEnabled(enabled);
					if(enabled && res != null) {
						res.set(this);
					} else if(!enabled && disabledRes != null) {
						disabledRes.set(this);
					}
				}
			};
			res.set(button);
			button.setOpaque(false);
			button.setBorderPainted(false);
			button.addActionListener(listener);
			button.setVerticalTextPosition(JLabel.BOTTOM);
			button.setHorizontalTextPosition(JLabel.CENTER);
			//button.setFocusable(false);
			button.setPreferredSize(new Dimension(120,120));
			button.setContentAreaFilled(false);
			button.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent evt) {
					button.setContentAreaFilled(true);
				}
				public void mouseExited(MouseEvent evt) {
					button.setContentAreaFilled(false);
				}
			});
			button.setActionCommand(actionCommand);

			return button;
		}

		static private HashMap<ButtonSet, JButton> getButtonMap(ActionListener listener)
		{
			LinkedHashMap<ButtonSet, JButton> buttonMap = new LinkedHashMap<>();
			for(ButtonSet bs: values()) {
				buttonMap.put(bs, bs.getButton(listener));
			}
			return buttonMap;
		}
	}

	public SelectViewPanel(ActionListener listener) {
		this.listener = listener;

		JLabel warringLabel = new JLabel(RImg.WARNING2.getImageIcon(80,80));

		JTextArea textArea = new JTextArea(RStr.MSG_UNSUPPORTED_PREVIEW.get());
		textArea.setEditable(false);

		openWithLabel = new JLabel(RStr.LABEL_OPEN_WITH.get());
		openWithLabel.setBorder(new EmptyBorder(20, 10, 0, 0));

		JPanel MessagePanel = new JPanel(new FlowLayout());
		MessagePanel.add(warringLabel);
		MessagePanel.add(textArea);
		MessagePanel.setBorder(new EmptyBorder(40, 5, 0, 0));
		MessagePanel.setBackground(Color.WHITE);
		MessagePanel.setMaximumSize(MessagePanel.getPreferredSize());
		MessagePanel.setAlignmentX(LEFT_ALIGNMENT);

		buttonMap = ButtonSet.getButtonMap(this);

		JPanel IconPanel = new JPanel(new GridBagLayout());
		for(JButton btn: buttonMap.values()) {
			IconPanel.add(btn);
		}
		IconPanel.setBackground(Color.WHITE);
		IconPanel.setMaximumSize(MessagePanel.getPreferredSize());
		IconPanel.setAlignmentX(LEFT_ALIGNMENT);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.WHITE);

		this.add(MessagePanel);
		this.add(openWithLabel);
		this.add(IconPanel);
	}

	public void setMenu(int Flag) {
		openWithLabel.setVisible(Flag != 0);
		for (ButtonSet key : buttonMap.keySet()) {
			buttonMap.get(key).setVisible((key.getButtonId() & Flag) != 0);
		}
		buttonMap.get(ButtonSet.CHOOSE_APPLICATION).setVisible(false);
	}

	public void setMenu(ResourceObject resObj) {
		this.resObj = resObj;
		if(resObj.path.endsWith(".dex")) {
			setMenu(  SelectViewPanel.SELECT_VIEW_ICON_JD_OPEN
					| SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION );
		} else if(resObj.path.endsWith(".apk")) {
			setMenu(  SelectViewPanel.SELECT_VIEW_ICON_SCANNER_OPEN
					| SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION
					| SelectViewPanel.SELECT_VIEW_ICON_EXPLORER );
		} else if(resObj.path.endsWith(".qmg")) {
			setMenu(  SelectViewPanel.SELECT_VIEW_NONE );
		} else {
			setMenu(  SelectViewPanel.SELECT_VIEW_ICON_OPEN
					| SelectViewPanel.SELECT_VIEW_ICON_TEXTVIEWER
					| SelectViewPanel.SELECT_VIEW_ICON_CHOOSE_APPLICATION );
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent comp = (JComponent) e.getSource();
		comp.putClientProperty(ResourceObject.class, resObj);
		listener.actionPerformed(e);
	}
}
