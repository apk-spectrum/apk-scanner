package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.gui.easymode.test.DragAndDropTest;
import com.apkscanner.gui.easymode.test.ReportingListTransferHandler;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyToolbarSettingDnDDlg extends JDialog {
	
	final int HIDE_LIST_ITEM_HEIGHT = 40;
	final int SHOW_LIST_ITEM_HEIGHT = 35; 
	
	private ToolEntry items[] = {
			new ToolEntry(Resource.STR_BTN_ABOUT.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_EXPLORER.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_INSTALL.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_LAUNCH.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon()),
	};
	
	private ToolEntry showitems[] = {
			new ToolEntry(Resource.STR_BTN_ABOUT.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_EXPLORER.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_INSTALL.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_LAUNCH.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon()),
			new ToolEntry(Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon()),
	};
	
	ListItemTransferHandler arrayListHandler;	
	JLabel description;
	JList<ToolEntry> hidetoollist;
	JList<ToolEntry> showtoollist;
	
	public EasyToolbarSettingDnDDlg(Frame frame, boolean modal) {
		super(frame, "Setting", modal);
		
		this.setSize(500, 500);
		//this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		arrayListHandler = new ListItemTransferHandler();
		// this.setResizable(false);

		final DefaultListModel<ToolEntry> hidelistmodel = new DefaultListModel<>();
	    for(ToolEntry tempentry : items) {
	    	hidelistmodel.addElement(tempentry);
	    }
		
		final DefaultListModel<ToolEntry> showlistmodel = new DefaultListModel<>();
		for(ToolEntry tempentry : showitems) {
			showlistmodel.addElement(tempentry);
		}
		
		hidetoollist = new JList<>(hidelistmodel);
		showtoollist = new JList<>(showlistmodel);
	
		JPanel parentpanel = new JPanel(new BorderLayout());
		parentpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		
		JLabel listLabel = new JLabel("Hide - Drag and Drop");
		listLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		JPanel buttonsetpanel = new JPanel();
		buttonsetpanel.add(new JButton("Apply"));
		buttonsetpanel.add(new JButton("Cancel"));

		hidetoollist.setCellRenderer(new HideCellRenderer(HIDE_LIST_ITEM_HEIGHT));
		hidetoollist.setTransferHandler(arrayListHandler);
		hidetoollist.setDragEnabled(true);
		hidetoollist.setDropMode(DropMode.INSERT);
		hidetoollist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane hidetoollistpane = new JScrollPane(hidetoollist);
		
		showtoollist.setTransferHandler(arrayListHandler);
		showtoollist.addMouseListener(new DragMouseAdapter());
		showtoollist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		showtoollist.setDropMode(DropMode.INSERT);
		showtoollist.setDragEnabled(true);
		showtoollist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		showtoollist.setCellRenderer(new ShowCellRenderer(SHOW_LIST_ITEM_HEIGHT));
		showtoollist.setPreferredSize(new Dimension(0, SHOW_LIST_ITEM_HEIGHT + 4));
		showtoollist.setVisibleRowCount(0);
		showtoollist.addListSelectionListener(
                 new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						// TODO Auto-generated method stub
						//ListSelectionModel lsm = (ListSelectionModel)e.getSource();
						if(showtoollist.getSelectedValue() != null){
							description.setText("*" + showtoollist.getSelectedValue().title);
						}
					}                	 
                 });
		
		
		JScrollPane showtoollistpane = new JScrollPane(showtoollist, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		JPanel tempupperpanel = new JPanel(new BorderLayout());
		
		tempupperpanel.add(listLabel, BorderLayout.NORTH);
		tempupperpanel.add(hidetoollistpane, BorderLayout.CENTER);
		
		
		JPanel contentspanel = new JPanel(new BorderLayout());
		
		contentspanel.add(tempupperpanel, BorderLayout.CENTER);
				
		
		JPanel tempdownpanel = new JPanel(new BorderLayout());
		JLabel showtoollabel = new JLabel("Visible");
		tempdownpanel.add(showtoollabel, BorderLayout.NORTH);
		
		showtoollabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 2));
		
		
		//tempdownpanel.add(new EasyGuiToolPanel(50, 500), BorderLayout.CENTER);
		tempdownpanel.add(showtoollistpane, BorderLayout.CENTER);
		description = new JLabel("Description : ");
		description.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		tempdownpanel.add(description, BorderLayout.SOUTH);
		
		contentspanel.add(tempdownpanel, BorderLayout.SOUTH);
		
		parentpanel.add(contentspanel, BorderLayout.CENTER);
		parentpanel.add(buttonsetpanel, BorderLayout.SOUTH	);
		
		this.add(parentpanel);
		//this.pack();

		this.setVisible(true);
	}

    private class DragMouseAdapter extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            JComponent c = (JComponent)e.getSource();
            TransferHandler handler = c.getTransferHandler();
            handler.exportAsDrag(c, e, TransferHandler.COPY);
//          handler.exportAsDrag(c, e, TransferHandler.MOVE);
        }
    }
	
	public static void main(final String[] args) {
		new EasyToolbarSettingDnDDlg(null, true);
		
		
	}

	class HideCellRenderer extends JLabel implements ListCellRenderer {
		// private Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
		int height = 0;
		public HideCellRenderer(int height) {
			height = height;
			setOpaque(true);
			setIconTextGap(12);
		}
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {			
			ToolEntry entry = (ToolEntry) value;
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			setText(entry.getTitle());
			setIcon(entry.getImage());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(Color.white);
				setForeground(Color.black);
			}
			return this;
		}
	}
	
	class ShowCellRenderer extends JLabel implements ListCellRenderer {
		// private Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
		int height = 0;
		public ShowCellRenderer(int height) {
			setOpaque(true);
			this.height = height;
			//setIconTextGap(12);
		}
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {			
			ToolEntry entry = (ToolEntry) value;
			//Log.d("list.getHeight() : " + list.getPreferredScrollableViewportSize().getHeight());
 			//image = entry.getImage();
			setIcon(new ImageIcon(ImageUtils.getScaledImage(entry.getImage(), this.height, this.height)));			
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));			
			//setIcon(entry.image);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(Color.white);
				setForeground(Color.black);
			}
			return this;
		}
        @Override
        public void paintComponent(final Graphics g) {
            //setRenderingHints here? Probably for ANTIALIAS...
            //((Graphics2D)g).scale(0.8, 0.8); //Let's scale everything that is painted afterwards:
        	//g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
        	
        	//image = new ImageIcon(ImageUtils.getScaledImage(entry.getImage(),30,30));
            super.paintComponent(g); //Let's paint the (scaled) JLabel!
        }
	}
}
