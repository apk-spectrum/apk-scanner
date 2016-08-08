package com.apkscanner.gui.dialog.install;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceManager;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;

public class DeviceListPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 662649457192939410L;
	
	private static int value = 0;
	private static JList<String> list;
	//private static Boolean clicked = false;
	private static ArrayList<DeviceStatus> DeviceList;
	
	/**
	 * Set up and show the dialog. The first Component argument determines which
	 * frame the dialog depends on; it should be a component in the dialog's
	 * controlling frame. The second Component argument should be null if you
	 * want the dialog to come up with its left corner in the center of the
	 * screen; otherwise, it should be the component on top of which the dialog
	 * should appear.
	 */
	public DeviceListPanel(ActionListener ButtonListener) {
		//showDialog();
		
		InitUI(null, null, Resource.STR_LABEL_DEVICE_LIST.getString(), Resource.STR_LABEL_SELECT_DEVICE.getString(), null, 0, "Cosmo  ",ButtonListener);
	}
	
	public DeviceStatus getSelectedData()	{	
		if(value == -1)
			return null;
		return DeviceList.get(list.getSelectedIndex());
	}
	
	public int getSelectedIndex() {
		return list.getSelectedIndex();
	}

	private void setValue(int newValue) {
		value = newValue;
		list.setSelectedIndex(value);
	}

	private void InitUI(Frame frame, Component locationComp, String labelText,
			String title, Object[] data, int initialValue, String longValue, ActionListener ButtonListener) {
		//clicked = false;
        		
		// Create and initialize the buttons.
		JButton cancelButton = new JButton(Resource.STR_BTN_REFRESH.getString());
		cancelButton.setPreferredSize(new Dimension(100,30));
		
		cancelButton.setActionCommand("Refresh");
		cancelButton.addActionListener(ButtonListener);
		cancelButton.setFocusable(true);
		cancelButton.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent ke) {
        		if(ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP)
            		list.dispatchEvent(ke);
        	}
        });
		//
		final JButton setButton = new JButton(Resource.STR_BTN_OK.getString());
		setButton.setPreferredSize(new Dimension(100,30));
		setButton.setActionCommand("Set");
		setButton.addActionListener(ButtonListener);
		//this.getRootPane().setDefaultButton(setButton);
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
		JLabel description = new JLabel("디바이스를 선택 하세요??");
		this.setBorder(new EmptyBorder(50, 10, 10, 10));
		this.add(description,BorderLayout.NORTH);
		this.add(listPane, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);

		// Initialize values.
		setValue(0);		
	}

	// Handle clicks on the Set and Cancel buttons.
	public void actionPerformed(ActionEvent e) {
		if ("Set".equals(e.getActionCommand())) {
			//Log.i("click set");
			DeviceListPanel.value = (int) (list.getSelectedIndex());			
			//clicked = true;
		} else if("Refresh".equals(e.getActionCommand())) {
			refreshData();
		}
	}
	
	public void refreshData()
	{
    	DeviceList = AdbDeviceManager.scanDevices();
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