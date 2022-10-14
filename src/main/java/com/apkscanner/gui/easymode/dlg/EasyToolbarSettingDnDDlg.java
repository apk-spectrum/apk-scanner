package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.RProp;

public class EasyToolbarSettingDnDDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 4294238572154793279L;
	final int HIDE_LIST_ITEM_HEIGHT = 50;
	final int SHOW_LIST_ITEM_HEIGHT = 35; 
	
	ListItemTransferHandler arrayListHandler;	
	JLabel description;
	JList<ToolEntry> hidetoollist;
	JList<ToolEntry> showtoollist;
	
	JButton btnapply;
	JButton btnCancel;
	
	boolean ischange;
	public EasyToolbarSettingDnDDlg(Frame frame, boolean modal) {
		super(frame, "Setting", modal);
		ischange = false;
		this.setSize(500, 500);
		//this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		arrayListHandler = new ListItemTransferHandler();
		// this.setResizable(false);

		
		final DefaultListModel<ToolEntry> showlistmodel = new DefaultListModel<>();
		for(ToolEntry tempentry : ToolEntryManager.getShowToolbarList()) {
			showlistmodel.addElement(tempentry);
		}
		
		final DefaultListModel<ToolEntry> hidelistmodel = new DefaultListModel<>();
	    for(ToolEntry tempentry : ToolEntryManager.getHideToolbarList()) {
	    	hidelistmodel.addElement(tempentry);
	    }
		
		hidetoollist = new JList<>(hidelistmodel);
		showtoollist = new JList<>(showlistmodel);
	
		JPanel parentpanel = new JPanel(new BorderLayout());
		parentpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		
		JLabel listLabel = new JLabel("Hide - Drag and Drop");
		listLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		JPanel buttonsetpanel = new JPanel();
		btnapply = new JButton("Apply");
		btnCancel = new JButton("Cancel");
		btnapply.addActionListener(this);
		btnCancel.addActionListener(this);
		
		buttonsetpanel.add(btnapply);
		buttonsetpanel.add(btnCancel);

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
							description.setText("*" + showtoollist.getSelectedValue().getTitle() + " : " + showtoollist.getSelectedValue().getDescription());
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
	
//	public static void main(final String[] args) {
//		new EasyToolbarSettingDnDDlg(null, true);
//		
//		
//	}

	class HideCellRenderer extends JLabel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = -1167455367997909583L;
		// private Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
		int height = 0;
		public HideCellRenderer(int height) {
			this.height = height;
			setOpaque(true);
			setIconTextGap(12);
		}
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {			
			ToolEntry entry = (ToolEntry) value;
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			setText(entry.getTitle() + " - " +entry.getDescription());
			setIcon(new ImageIcon(ImageUtils.getScaledImage(entry.getImage(), this.height, this.height)));
			//setIcon(entry.getImage());
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
	
	class ShowCellRenderer extends JLabel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = -8747865035780014531L;
		// private Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
		int height = 0;
		public ShowCellRenderer(int height) {
			setOpaque(true);
			this.height = height;
			//setIconTextGap(12);
		}
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource().equals(btnapply)) {			
			ListModel<ToolEntry> list = showtoollist.getModel();			
			String str = "";
			for(int i = 0; i < list.getSize(); i++){
				ToolEntry obj = (ToolEntry)list.getElementAt(i);
				str +=ToolEntryManager.findIndexFromAllEntry(obj) + ",";
				}
			RProp.S.EASY_GUI_TOOLBAR.set(str);
			ischange = true;			
			this.dispose();
		} else if(e.getSource().equals(btnCancel)) {
			ischange = false;
			this.dispose();
		}
	}
	public boolean ischange() {
		return ischange;
	}
}
