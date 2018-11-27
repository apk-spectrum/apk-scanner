package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import com.apkscanner.gui.easymode.contents.EasyGuiToolPanel;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyToolbarSettingDlg extends JDialog {
	
	final int LIST_ITEM_HEIGHT = 30; 
	
	private BookEntry books[] = {
			new BookEntry(Resource.STR_BTN_ABOUT.getString(), Resource.IMG_TOOLBAR_ABOUT.getImageIcon(LIST_ITEM_HEIGHT, LIST_ITEM_HEIGHT)),
			new BookEntry(Resource.STR_BTN_EXPLORER.getString(), Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(LIST_ITEM_HEIGHT, LIST_ITEM_HEIGHT)),
			new BookEntry(Resource.STR_BTN_INSTALL.getString(), Resource.IMG_TOOLBAR_INSTALL.getImageIcon(LIST_ITEM_HEIGHT, LIST_ITEM_HEIGHT)),
			new BookEntry(Resource.STR_BTN_LAUNCH.getString(), Resource.IMG_TOOLBAR_LAUNCH.getImageIcon(LIST_ITEM_HEIGHT, LIST_ITEM_HEIGHT)),
			new BookEntry(Resource.STR_BTN_OPEN_PACKAGE.getString(), Resource.IMG_TOOLBAR_PACKAGETREE.getImageIcon(LIST_ITEM_HEIGHT, LIST_ITEM_HEIGHT)),
			
			
	};

	JList booklist = new JList(books);

	public EasyToolbarSettingDlg(Frame frame, boolean modal) {
		super(frame, "Setting", modal);

		this.setSize(500, 300);
		this.setLocationRelativeTo(frame);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// this.setResizable(false);

		JPanel parentpanel = new JPanel(new BorderLayout());

		JLabel listLabel = new JLabel("Hide");
		JPanel buttonsetpanel = new JPanel();
		buttonsetpanel.add(new JButton("apply"));
		buttonsetpanel.add(new JButton("cancel"));

		booklist = new JList(books);
		booklist.setCellRenderer(new BookCellRenderer());
		booklist.setVisibleRowCount(2);
		JScrollPane pane = new JScrollPane(booklist);

		
		JPanel tempupperpanel = new JPanel(new BorderLayout());
		JPanel tempcenterpanel = new JPanel();
		
		JButton uplabel = new JButton("Up");
		JButton downlabel = new JButton("Down");
		tempcenterpanel.add(uplabel);
		tempcenterpanel.add(downlabel);		
		tempupperpanel.add(listLabel, BorderLayout.NORTH);
		tempupperpanel.add(pane, BorderLayout.CENTER);
		tempupperpanel.add(tempcenterpanel, BorderLayout.SOUTH);
		
		JPanel contentspanel = new JPanel(new BorderLayout());
		
		contentspanel.add(tempupperpanel, BorderLayout.CENTER);
		
		
		
		JPanel tempdownpanel = new JPanel(new BorderLayout());
		tempdownpanel.add(new JLabel("Show"), BorderLayout.NORTH);
		tempdownpanel.add(new EasyGuiToolPanel(50, 500), BorderLayout.CENTER);

		JPanel RLbuttonsetpanel = new JPanel();
		RLbuttonsetpanel.add(new JButton("left"));
		RLbuttonsetpanel.add(new JButton("right"));
		tempdownpanel.add(RLbuttonsetpanel, BorderLayout.EAST);
		
		contentspanel.add(tempdownpanel, BorderLayout.SOUTH);
		
		parentpanel.add(contentspanel, BorderLayout.CENTER);
		parentpanel.add(buttonsetpanel, BorderLayout.SOUTH	);
		
		this.add(parentpanel);
		// this.pack();
		this.setVisible(true);
	}

	public static void main(final String[] args) {
		new EasyToolbarSettingDlg(null, true);
		
		
	}

	class BookEntry {
		private final String title;
		private ImageIcon image;

		public BookEntry(String title, String imagePath) {
			this.title = title;
			image = new ImageIcon(imagePath);
		}

		public BookEntry(String title, ImageIcon image) {
			this.title = title;
			this.image = image;
		}

		public String getTitle() {
			return title;
		}

		public ImageIcon getImage() {
			return image;
		}

		// Override standard toString method to give a useful result
		public String toString() {
			return title;
		}
	}

	class BookCellRenderer extends JLabel implements ListCellRenderer {
		// private Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

		public BookCellRenderer() {
			setOpaque(true);
			setIconTextGap(12);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			
			BookEntry entry = (BookEntry) value;
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

}
