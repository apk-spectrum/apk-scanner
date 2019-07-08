package com.apkscanner.gui.easymode.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class DragTest3 {
	public JComponent makeUI() {
		DragPanel p1 = new DragPanel();
		p1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		p1.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
		p1.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
		p1.add(new JLabel("Label1"));
		p1.add(new JLabel("Label2"));
		MouseListener handler = new Handler();
		p1.addMouseListener(handler);
		LabelTransferHandler th = new LabelTransferHandler();
		p1.setTransferHandler(th);
		JPanel p = new JPanel(new GridLayout(1, 2));
		p.add(p1);

		DragPanel p2 = new DragPanel();
		p2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		p2.addMouseListener(handler);
		p2.setTransferHandler(th);
		p.add(p2);

		JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.add(p);
		panel.add(new JScrollPane(new JTextArea()));
		return panel;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public static void createAndShowGUI() {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.getContentPane().add(new DragTest3().makeUI());
		f.setSize(320, 240);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}

@SuppressWarnings("serial")
class DragPanel extends JPanel {
	public DragPanel() {
		super();
	}

	public JLabel draggingLabel;
}

class Handler extends MouseAdapter {
	@Override
	public void mousePressed(MouseEvent e) {
		DragPanel p = (DragPanel) e.getSource();
		Component c = SwingUtilities.getDeepestComponentAt(p, e.getX(), e.getY());
		if (c != null && c instanceof JLabel) {
			p.draggingLabel = (JLabel) c;
			p.getTransferHandler().exportAsDrag(p, e, TransferHandler.MOVE);
		}
	}
}

@SuppressWarnings("serial")
class LabelTransferHandler extends TransferHandler {
	private final DataFlavor localObjectFlavor;
	private final JLabel label = new JLabel() {
		@Override
		public boolean contains(int x, int y) {
			return false;
		}
	};
	private final JWindow window = new JWindow();

	public LabelTransferHandler() {
		System.out.println("LabelTransferHandler");
		localObjectFlavor = new ActivationDataFlavor(DragPanel.class, DataFlavor.javaJVMLocalObjectMimeType, "JLabel");
		window.add(label);
		window.setAlwaysOnTop(true);
		window.setBackground(new Color(0, true));
		DragSource.getDefaultDragSource().addDragSourceMotionListener(new DragSourceMotionListener() {
			@Override
			public void dragMouseMoved(DragSourceDragEvent dsde) {
				Point pt = dsde.getLocation();
				pt.translate(5, 5); // offset
				window.setLocation(pt);
			}
		});
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		System.out.println("createTransferable");
		DragPanel p = (DragPanel) c;
		JLabel l = p.draggingLabel;
		String text = l.getText();
		// TEST
		// if(text==null) {
		// text = l.getIcon().toString();
		// }
		// return new StringSelection(text+"\n");
		final DataHandler dh = new DataHandler(c, localObjectFlavor.getMimeType());
		if (text == null)
			return dh;
		final StringSelection ss = new StringSelection(text + "\n");
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				ArrayList<DataFlavor> list = new ArrayList<>();
				for (DataFlavor f : ss.getTransferDataFlavors()) {
					list.add(f);
				}
				for (DataFlavor f : dh.getTransferDataFlavors()) {
					list.add(f);
				}
				return list.toArray(dh.getTransferDataFlavors());
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				for (DataFlavor f : getTransferDataFlavors()) {
					if (flavor.equals(f)) {
						return true;
					}
				}
				return false;
			}

			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (flavor.equals(localObjectFlavor)) {
					return dh.getTransferData(flavor);
				} else {
					return ss.getTransferData(flavor);
				}
			}
		};
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		System.out.println("getSourceActions");
		DragPanel p = (DragPanel) c;
		label.setIcon(p.draggingLabel.getIcon());
		label.setText(p.draggingLabel.getText());
		window.pack();
		Point pt = p.draggingLabel.getLocation();
		SwingUtilities.convertPointToScreen(pt, p);
		window.setLocation(pt);
		window.setVisible(true);
		return MOVE;
	}

	@Override
	public boolean importData(TransferSupport support) {
		System.out.println("importData");
		if (!canImport(support))
			return false;
		DragPanel target = (DragPanel) support.getComponent();
		try {
			DragPanel src = (DragPanel) support.getTransferable().getTransferData(localObjectFlavor);
			JLabel l = new JLabel();
			l.setIcon(src.draggingLabel.getIcon());
			l.setText(src.draggingLabel.getText());
			target.add(l);
			target.revalidate();
			return true;
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		System.out.println("exportDone");
		DragPanel src = (DragPanel) c;
		if (action == TransferHandler.MOVE) {
			src.remove(src.draggingLabel);
			src.revalidate();
			src.repaint();
		}
		src.draggingLabel = null;
		window.setVisible(false);
	}
}