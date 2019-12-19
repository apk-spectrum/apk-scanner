package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.UiEventHandler;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;

public class ResourceToolBarPanel extends JPanel {
	private static final long serialVersionUID = 5458329057300067564L;

	public static final String TEXTVIEWER_TOOLBAR_OPEN = UiEventHandler.ACT_CMD_OPEN_RESOURCE_FILE;
	public static final String TEXTVIEWER_TOOLBAR_SAVE= UiEventHandler.ACT_CMD_SAVE_RESOURCE_FILE;
	public static final String TEXTVIEWER_TOOLBAR_FIND = "textviewer_toolbar_find";
	public static final String TEXTVIEWER_TOOLBAR_NEXT = "textviewer_toolbar_next";
	public static final String TEXTVIEWER_TOOLBAR_PREV = "textviewer_toolbar_prev";
	public static final String TEXTVIEWER_TOOLBAR_VIEW_TYPE = "textviewer_toolbar_view_type";
	public static final String TEXTVIEWER_TOOLBAR_MULTI_LINE = "textviewer_toolbar_multi_line";
	public static final String TEXTVIEWER_TOOLBAR_FIND_TEXTAREA = "textviewer_toolbar_findtextarea";

	private JToolBar textTools;
	private JToolBar xmllTools;
	private JTextField filePathtextField;
	private JTextField findtextField;
	private JComboBox<String> resTypeCombobox;
	private JToggleButton multiLinePrintButton;

	public ResourceToolBarPanel(final ActionListener listener) {
		super(new BorderLayout());

		JButton openBtn = new JButton("",RImg.RESOURCE_TEXTVIEWER_TOOLBAR_OPEN.getImageIcon(16, 16));
		JButton saveBtn = new JButton("",RImg.RESOURCE_TEXTVIEWER_TOOLBAR_SAVE.getImageIcon(16, 16));

		openBtn.setActionCommand(TEXTVIEWER_TOOLBAR_OPEN);
		saveBtn.setActionCommand(TEXTVIEWER_TOOLBAR_SAVE);

		openBtn.addActionListener(listener);
		saveBtn.addActionListener(listener);

		openBtn.setFocusPainted(false);
		saveBtn.setFocusPainted(false);

		JToolBar extrTools = new JToolBar("");
		extrTools.add(openBtn);
		extrTools.add(saveBtn);
		extrTools.add(getNewSeparator());
		extrTools.setFloatable(false);
		extrTools.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));

		filePathtextField = new JTextField("");
		filePathtextField.setEditable(false);
		filePathtextField.setBackground(Color.WHITE);


		JButton findBtn = new JButton("", RImg.RESOURCE_TEXTVIEWER_TOOLBAR_FIND.getImageIcon(16, 16));
		JButton nextBtn = new JButton("", RImg.RESOURCE_TEXTVIEWER_TOOLBAR_NEXT.getImageIcon(16, 16));
		JButton prevBtn = new JButton("", RImg.RESOURCE_TEXTVIEWER_TOOLBAR_PREV.getImageIcon(16, 16));

		findBtn.setActionCommand(TEXTVIEWER_TOOLBAR_FIND);
		nextBtn.setActionCommand(TEXTVIEWER_TOOLBAR_NEXT);
		prevBtn.setActionCommand(TEXTVIEWER_TOOLBAR_PREV);

		findBtn.setFocusPainted(false);
		nextBtn.setFocusPainted(false);
		prevBtn.setFocusPainted(false);

		findBtn.addActionListener(listener);
		nextBtn.addActionListener(listener);
		prevBtn.addActionListener(listener);

		findtextField = new JTextField();
		findtextField.setActionCommand(TEXTVIEWER_TOOLBAR_FIND_TEXTAREA);

		Dimension size = findtextField.getPreferredSize();
		findtextField.setPreferredSize(new Dimension(size.width+100, size.height));
		findtextField.addActionListener(listener);
		findtextField.setFocusable(true);
		findtextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent evt) {
				findtextField.setBackground(new Color(255,255,255));
			}

			@Override
			public void focusGained(FocusEvent evt) {
				findtextField.setBackground(new Color(178,235,244));
			}
		});

		xmllTools = new JToolBar();
		xmllTools.setFloatable(false);
		xmllTools.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		xmllTools.setBorder(new EmptyBorder(0,0,0,0));

		String[] petStrings = { RConst.AXML_VEIWER_TYPE_XML, RConst.AXML_VEIWER_TYPE_ARSC };
		resTypeCombobox = new JComboBox<String>(petStrings);
		resTypeCombobox.setActionCommand(TEXTVIEWER_TOOLBAR_VIEW_TYPE);
		resTypeCombobox.setSelectedItem(RProp.S.AXML_VIEWER_TYPE.get());
		resTypeCombobox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fileType = resTypeCombobox.getSelectedItem().toString();
				RProp.S.AXML_VIEWER_TYPE.set(fileType);
				if(RConst.AXML_VEIWER_TYPE_XML.equals(fileType)) {
					multiLinePrintButton.setEnabled(true);
				} else if(RConst.AXML_VEIWER_TYPE_ARSC.equals(fileType)) {
					multiLinePrintButton.setEnabled(false);
				}
				listener.actionPerformed(e);	
			}
		});

		multiLinePrintButton = new JToggleButton(RImg.RESOURCE_TEXTVIEWER_TOOLBAR_INDENT.getImageIcon());
		multiLinePrintButton.setActionCommand(TEXTVIEWER_TOOLBAR_MULTI_LINE);
		multiLinePrintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RProp.B.PRINT_MULTILINE_ATTR.set(multiLinePrintButton.isSelected());
				listener.actionPerformed(e);
			}
		});
		multiLinePrintButton.setFocusPainted(false);
		multiLinePrintButton.setSelected(RProp.B.PRINT_MULTILINE_ATTR.get());

		xmllTools.add(getNewSeparator());
		xmllTools.add(resTypeCombobox);
		xmllTools.add(multiLinePrintButton);

		textTools = new JToolBar();
		textTools.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		textTools.setFloatable(false);
		textTools.setVisible(false);
		//add(new JLabel(RStr.LABEL_SEARCH.get()));
		//add(getNewSeparator(JSeparator.VERTICAL, new Dimension(5,16)));
		textTools.add(findtextField);
		textTools.add(findBtn);
		textTools.add(prevBtn);
		textTools.add(nextBtn);
		textTools.add(xmllTools);

		add(filePathtextField, BorderLayout.CENTER);
		add(extrTools, BorderLayout.WEST);
		add(textTools, BorderLayout.SOUTH);

		KeyStrokeAction.registerKeyStrokeAction(findtextField, KeyStroke.getKeyStroke("F3"), listener);
		KeyStrokeAction.registerKeyStrokeAction(findtextField, KeyStroke.getKeyStroke("shift F3"), listener);
	}

	public void setToolbarPolicy(ResourceObject resObj) {
		if(resObj == null) {
			setResPath("");
			return;
		}
		setResPath(resObj.path);
		findtextField.setText("");

		switch(resObj.attr) {
		case ResourceObject.ATTR_AXML:
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
		case ResourceObject.ATTR_CERT:
			setVisibleTextTools(true);
			setVisibleAXmlTools(resObj.attr == ResourceObject.ATTR_AXML);
			break;
		case ResourceObject.ATTR_ETC:
			if("resources.arsc".equals(resObj.path)) {
				setVisibleTextTools(true);
				setVisibleAXmlTools(false);
				break;
			}
		case ResourceObject.ATTR_IMG:
		default:
			setVisibleTextTools(false);
			break;
		}
	}

	public void setResPath(String path) {
		filePathtextField.setText(path);
	}

	public void setVisibleTextTools(boolean visible) {
		textTools.setVisible(visible);
	}

	public void setVisibleAXmlTools(boolean visible) {
		xmllTools.setVisible(visible);
	}

	public String getFindText() {
		return findtextField.getText().trim();
	}

	public String getViewType() {
		return (String) resTypeCombobox.getSelectedItem();
	}

	public void setFocusFindTextField() {
		findtextField.requestFocusInWindow();
	}

	private JSeparator getNewSeparator() {
		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setPreferredSize(new Dimension(5,16));
		return separator;
	}
}
