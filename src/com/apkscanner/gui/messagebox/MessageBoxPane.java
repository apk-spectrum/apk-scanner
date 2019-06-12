package com.apkscanner.gui.messagebox;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.apkscanner.gui.component.WindowSizeMemorizer;
import com.apkscanner.resource.Resource;

public class MessageBoxPane extends JOptionPane
{
	private static final long serialVersionUID = 4947402878882910721L;

	public MessageBoxPane() {
		this("MessageBoxPane message");
	}

	public MessageBoxPane(Object message) {
		this(message, PLAIN_MESSAGE);
	}

	public MessageBoxPane(Object message, int messageType) {
		this(message, messageType, DEFAULT_OPTION);
	}

	public MessageBoxPane(Object message, int messageType, int optionType) {
		this(message, messageType, optionType, null);
	}

	public MessageBoxPane(Object message, int messageType, int optionType,
			Icon icon) {
		this(message, messageType, optionType, icon, null);
	}

	public MessageBoxPane(Object message, int messageType, int optionType,
			Icon icon, Object[] options) {
		this(message, messageType, optionType, icon, options, null);
	}

	public MessageBoxPane(Object message, int messageType, int optionType, Icon icon, Object[] options, Object initialValue)
	{
		super(message, messageType, optionType, icon, options, initialValue);

		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

		Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
	}

	public JDialog createDialog(Component parentComponent, String title)
			throws HeadlessException {
		JDialog dialog = super.createDialog(parentComponent, title);
		dialog.setResizable(true);
		dialog.setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(dialog, title);
		} else {
			dialog.pack();
		}
		WindowSizeMemorizer.registeComponent(dialog, title);
		return dialog;
	}

	public JDialog createDialog(String title) throws HeadlessException {
		return createDialog(null, title);
	}

	public static String showInputDialog(Object message)
			throws HeadlessException {
		return showInputDialog(null, message);
	}

	public static String showInputDialog(Object message, Object initialSelectionValue) {
		return showInputDialog(null, message, initialSelectionValue);
	}

	public static String showInputDialog(Component parentComponent,
			Object message) throws HeadlessException {
		return showInputDialog(parentComponent, message, UIManager.getString(
				"OptionPane.inputDialogTitle"), QUESTION_MESSAGE);
	}

	public static String showInputDialog(Component parentComponent, Object message,
			Object initialSelectionValue) {
		return (String)showInputDialog(parentComponent, message,
				UIManager.getString("OptionPane.inputDialogTitle"), QUESTION_MESSAGE, null, null,
				initialSelectionValue);
	}

	public static String showInputDialog(Component parentComponent,
			Object message, String title, int messageType)
					throws HeadlessException {
		return (String)showInputDialog(parentComponent, message, title,
				messageType, null, null, null);
	}

	public static Object showInputDialog(Component parentComponent,
			Object message, String title, int messageType, Icon icon,
			Object[] selectionValues, Object initialSelectionValue)
					throws HeadlessException {
		MessageBoxPane pane = new MessageBoxPane(message, messageType,
				OK_CANCEL_OPTION, icon,
				null, null);

		pane.setWantsInput(true);
		pane.setSelectionValues(selectionValues);
		pane.setInitialSelectionValue(initialSelectionValue);
		pane.setComponentOrientation(((parentComponent == null) ?
				getRootFrame() : parentComponent).getComponentOrientation());

		JDialog dialog = pane.createDialog(parentComponent, title);

		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();

		Object value = pane.getInputValue();

		if (value == UNINITIALIZED_VALUE) {
			return null;
		}
		return value;
	}

	public static void showMessageDialog(Component parentComponent,
			Object message) throws HeadlessException {
		showMessageDialog(parentComponent, message, UIManager.getString("OptionPane.messageDialogTitle"),
				INFORMATION_MESSAGE);
	}

	public static void showMessageDialog(Component parentComponent,
			Object message, String title, int messageType)
					throws HeadlessException {
		showMessageDialog(parentComponent, message, title, messageType, null);
	}

	public static void showMessageDialog(Component parentComponent,
			Object message, String title, int messageType, Icon icon)
					throws HeadlessException {
		showOptionDialog(parentComponent, message, title, DEFAULT_OPTION,
				messageType, icon, null, null);
	}

	public static int showConfirmDialog(Component parentComponent,
			Object message) throws HeadlessException {
		return showConfirmDialog(parentComponent, message,
				UIManager.getString("OptionPane.titleText"),
				YES_NO_CANCEL_OPTION);
	}

	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType)
					throws HeadlessException {
		return showConfirmDialog(parentComponent, message, title, optionType,
				QUESTION_MESSAGE);
	}

	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType, int messageType)
					throws HeadlessException {
		return showConfirmDialog(parentComponent, message, title, optionType,
				messageType, null);
	}

	public static int showConfirmDialog(Component parentComponent,
			Object message, String title, int optionType,
			int messageType, Icon icon) throws HeadlessException {
		return showOptionDialog(parentComponent, message, title, optionType,
				messageType, icon, null, null);
	}

	public static int showOptionDialog(Component parentComponent,
			Object message, String title, int optionType, int messageType,
			Icon icon, Object[] options, Object initialValue)
					throws HeadlessException {
		MessageBoxPane pane = new MessageBoxPane(message, messageType,
				optionType, icon,
				options, initialValue);

		pane.setInitialValue(initialValue);
		pane.setComponentOrientation(((parentComponent == null) ?
				getRootFrame() : parentComponent).getComponentOrientation());

		final JDialog dialog = pane.createDialog(parentComponent, title);

		if(message instanceof Component) {
			((Component)message).addComponentListener(new ComponentAdapter() {
				private int prePreferedHeight = 0;
				@Override
				public void componentResized(ComponentEvent arg0) {
					Dimension size = dialog.getSize();
					Dimension preferSize = dialog.getPreferredSize();
					if(prePreferedHeight < preferSize.height 
							&& size.height < preferSize.height) {
						size.height = preferSize.height;
						dialog.setSize(size);
					}
					prePreferedHeight = preferSize.height;
				}
			});
		}

		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();

		Object selectedValue = pane.getValue();

		if(selectedValue == null)
			return CLOSED_OPTION;
		if(options == null) {
			if(selectedValue instanceof Integer)
				return ((Integer)selectedValue).intValue();
			return CLOSED_OPTION;
		}
		for(int counter = 0, maxCounter = options.length;
				counter < maxCounter; counter++) {
			if(options[counter].equals(selectedValue))
				return counter;
		}
		return CLOSED_OPTION;
	}

	public static void showTextAreaDialog(Component parentComponent, String text, String title, int messageType, Icon icon, Dimension size) {
		showTextAreaDialog(parentComponent, null, text, title, messageType, icon, size);
	}

	public static void showTextAreaDialog(Component parentComponent, String subject, String text, String title, int messageType, Icon icon, Dimension size) {
		JTextPane messagePane = new JTextPane();
		if(subject != null) {
			messagePane.setText(subject);
			messagePane.setOpaque(false);
			messagePane.setEditable(false);
			messagePane.setFocusable(false);
		} else {
			messagePane.setVisible(false);
		}

		JTextArea taskOutput = new JTextArea();
		taskOutput.setText(text);
		taskOutput.setEditable(false);
		taskOutput.setCaretPosition(0);

		final JScrollPane scrollPane = new JScrollPane(taskOutput);
		scrollPane.setPreferredSize(size);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setOpaque(false);
		panel.add(messagePane,BorderLayout.NORTH);
		panel.add(scrollPane,BorderLayout.CENTER);

		showOptionDialog(parentComponent, panel, title, DEFAULT_OPTION,
				messageType, icon, null, null);
	}
	
	public static void showInfomation(Component parentComponent, Object message) {
		showMessageDialog(parentComponent, message, Resource.STR_LABEL_INFO.getString(), INFORMATION_MESSAGE, null);
	}

	public static void showWarring(Component parentComponent, Object message) {
		showMessageDialog(parentComponent, message, Resource.STR_LABEL_WARNING.getString(), WARNING_MESSAGE, null);
	}

	public static void showError(Component parentComponent, Object message) {
		showMessageDialog(parentComponent, message, Resource.STR_LABEL_ERROR.getString(), ERROR_MESSAGE, null);
	}

	public static int showQuestion(Component parentComponent, Object message, int optionType) {
		return showConfirmDialog(parentComponent, message, Resource.STR_LABEL_QUESTION.getString(), optionType, QUESTION_MESSAGE, null);
	}

	public static void showPlain(Component parentComponent, Object message) {
		showMessageDialog(parentComponent, message, Resource.STR_APP_NAME.getString(), PLAIN_MESSAGE, null);
	}
}