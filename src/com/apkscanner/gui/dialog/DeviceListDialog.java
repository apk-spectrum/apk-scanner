package com.apkscanner.gui.dialog;

import javax.swing.*;

import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.AdbWrapper.DeviceStatus;
import com.apkscanner.gui.util.ButtonType;
import com.apkscanner.gui.util.StandardButton;
import com.apkscanner.gui.util.Theme;
import com.apkscanner.resource.Resource;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeviceListDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 662649457192939410L;

	private static DeviceListDialog dialog;
	private static int value = 0;
	private static JList<String> list;
	private static Boolean clicked = false;
	private static ArrayList<DeviceStatus> DeviceList;

	/**
	 * Set up and show the dialog. The first Component argument determines which
	 * frame the dialog depends on; it should be a component in the dialog's
	 * controlling frame. The second Component argument should be null if you
	 * want the dialog to come up with its left corner in the center of the
	 * screen; otherwise, it should be the component on top of which the dialog
	 * should appear.
	 */
	public static int showDialog()
	{
		return showDialog(null, null, Resource.STR_LABEL_DEVICE_LIST.getString(), Resource.STR_LABEL_SELECT_DEVICE.getString(), null, 0, "Cosmo  ");
	}
	
	public static int showDialog(Component frameComp,
			Component locationComp, String labelText, String title,
			String[] possibleValues, int initialValue, String longValue)
	{
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		do {
			dialog = new DeviceListDialog(frame, locationComp, labelText, title,
					possibleValues, initialValue, longValue);

			KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
			dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
			dialog.getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
				private static final long serialVersionUID = 3581700080322128746L;
				public void actionPerformed(ActionEvent e) {
					value = -1;
					dialog.dispose();
			    }
			});

	    	KeyStroke vk_f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
			dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(vk_f5, "VK_F5");
			dialog.getRootPane().getActionMap().put("VK_F5", new AbstractAction() {
				private static final long serialVersionUID = -5281980076592985530L;
				public void actionPerformed(ActionEvent e) {
					refreshData();
			    }
			});
			
			dialog.setVisible(true);
			dialog.dispose();

			
			if(clicked) {
				if(value == -1) {
				} else if(DeviceList.get(value).status.equals("device")) {
					return value;
				} else if(DeviceList.get(value).status.equals("unauthorized")) {
					JOptionPane.showOptionDialog(null, Resource.STR_MSG_DEVICE_UNAUTHORIZED.getString(), Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null,
				    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				} else {
					JOptionPane.showOptionDialog(null, Resource.STR_MSG_DEVICE_UNKNOWN.getString() + " " + DeviceList.get(value).status, Resource.STR_LABEL_ERROR.getString(), JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null,
				    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
				}
				clicked = false;
			}
			else return -1;
		} while (true);
	}
	
	public static DeviceStatus getSelectedData()
	{
		if(value == -1)
			return null;
		return DeviceList.get(value);
	}

	private void setValue(int newValue) {
		value = newValue;
		list.setSelectedIndex(value);
	}

	private DeviceListDialog(Frame frame, Component locationComp, String labelText,
			String title, Object[] data, int initialValue, String longValue) {
		super(frame, title, true);
		clicked = false;

        ImageIcon Appicon = Resource.IMG_APP_ICON.getImageIcon();
		
		//ImageIcon Appicon = new ImageIcon("/home/leejinhyeong/workspace/APKInfoDlgv2/AppIcon.png");
		
		this.setResizable( false );
		this.setIconImage(Appicon.getImage());
				
		// Create and initialize the buttons.
		StandardButton cancelButton = new StandardButton(Resource.STR_BTN_REFRESH.getString(),Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);		
		cancelButton.setActionCommand("Refresh");
		cancelButton.addActionListener(this);
		cancelButton.setFocusable(true);
		cancelButton.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent ke) {
        		if(ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP)
            		list.dispatchEvent(ke);
        	}
        });
		//
		final JButton setButton = new StandardButton(Resource.STR_BTN_OK.getString(),Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED);
		setButton.setActionCommand("Set");
		setButton.addActionListener(this);
		getRootPane().setDefaultButton(setButton);
		setButton.setFocusable(true);
		setButton.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent ke) {
        		if(ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP)
            		list.dispatchEvent(ke);
        	}
        });

		// main part of the dialog
		list = new JList<String>((String[])data) {
			private static final long serialVersionUID = -1937264530524245731L;

			// Subclass JList to workaround bug 4832765, which can cause the
			// scroll pane to not let the user easily scroll up to the beginning
			// of the list. An alternative would be to set the unitIncrement
			// of the JScrollBar to a fixed value. You wouldn't get the nice
			// aligned scrolling, but it should work.
			public int getScrollableUnitIncrement(Rectangle visibleRect,
					int orientation, int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0
						&& (row = getFirstVisibleIndex()) != -1) {
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0)) {
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);

						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect,
						orientation, direction);
			}
		};
		refreshData();

		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		if (longValue != null) {
			list.setPrototypeCellValue(longValue); // get extra space
		}
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setFocusable(true);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					setButton.doClick(); // emulate button click
				}
			}
		});
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);

		// Create a container so that we can add a title around
		// the scroll pane. Can't add a title directly to the
		// scroll pane because its background would be white.
		// Lay out the label and scroll pane from top to bottom.
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel(labelText);
		label.setLabelFor(list);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0, 5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(setButton);

		// Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
		forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		
		Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
		backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED));
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);

		// Initialize values.
		setValue(0);
		pack();
		setLocationRelativeTo(locationComp);
	}

	// Handle clicks on the Set and Cancel buttons.
	public void actionPerformed(ActionEvent e) {
		if ("Set".equals(e.getActionCommand())) {
			//Log.i("click set");
			DeviceListDialog.value = (int) (list.getSelectedIndex());
			DeviceListDialog.dialog.setVisible(false);
			clicked = true;
		} else if("Refresh".equals(e.getActionCommand())) {
			refreshData();			
		}
	}
	
	private static void refreshData()
	{
    	DeviceList = AdbWrapper.scanDevices();
		String[] names = new String[DeviceList.size()];

		int i = 0;
		for(DeviceStatus dev: DeviceList) {
			if(dev.status.equals("device")) {
				names[i++] = dev.name + "(" + dev.device + ")";
			} else {
				names[i++] = dev.name + "(Unknown) - " + dev.status; 
			}
		}
		list.setListData(names);
		if(names.length > 0) list.setSelectedIndex(0);
	}
}