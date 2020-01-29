package com.apkscanner.gui.installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ImageObserver;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import com.android.ddmlib.IDevice;
import com.apkscanner.resource.RImg;
import com.apkspectrum.core.installer.OptionsBundle;
import com.apkspectrum.util.Log;

public class DeviceCustomList extends JList<DeviceListData> {
	private static final long serialVersionUID = 4647130365982201484L;

	private ActionListener actionlistener;

	public DeviceCustomList(EventListener listener) {
		setLayout(new BorderLayout());

		if(listener != null && listener instanceof ActionListener) {
			actionlistener = (ActionListener) listener;
		}

		setPreferredSize(new Dimension(200, 0));
		//setBorder(BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ));
		setBorder(new EtchedBorder(EtchedBorder.RAISED));

		setModel(new DefaultListModel<DeviceListData>());
		setCellRenderer(new DeviceDataRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		CellButtonsMouseListener cbml = new CellButtonsMouseListener(this);
		addMouseListener(cbml);
		addMouseMotionListener(cbml);

		if(listener != null && listener instanceof ListSelectionListener) {
			addListSelectionListener((ListSelectionListener) listener);
		}
	}

	class DeviceDataRenderer extends JPanel implements ListCellRenderer<DeviceListData> {
		private static final long serialVersionUID = 927417951602577901L;

		public int pressedIndex  = -1;
		public int rolloverIndex = -1;

		ToggleButtonBar tagPanel;
		CustomLabel customLabel = new CustomLabel();
		JButton button;

		JLabel isinstallIcon;
		ImageIcon ii;

		protected DeviceDataRenderer() {
			super(new BorderLayout());

			ii = new ImageIcon(RImg.INSTALL_LOADING.getImage());

			setBorder ( BorderFactory.createEmptyBorder ( 5, 5 , 5, 5 ) );

			setOpaque(true);
			tagPanel = new ToggleButtonBar(ApkInstallWizard.STATUS_INSTALLING, actionlistener);

			JPanel Iconpanel = new JPanel(new BorderLayout());
			isinstallIcon = new JLabel(RImg.INSTALL_BLOCK.getImageIcon());

			Iconpanel.setBackground(Color.WHITE);

			Iconpanel.add(tagPanel, BorderLayout.CENTER);
			Iconpanel.add(isinstallIcon, BorderLayout.WEST);

			//add(textArea);
			add(customLabel, BorderLayout.CENTER);
			add(Iconpanel, BorderLayout.SOUTH);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.width = 0; // VerticalScrollBar as needed
			return d;
		}

		@Override
		protected void paintComponent ( Graphics g ) {
			Graphics2D g2d = ( Graphics2D ) g;
			g2d.setPaint ( Color.LIGHT_GRAY );
			g.drawLine(2, getHeight()-2, getWidth()-2, getHeight()-2 );
		}

		public void setStatus(int status) {
			tagPanel.setStatus(status);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DeviceListData> list, DeviceListData value,
				int index, boolean isSelected, boolean cellHasFocus) {
			//textArea.setText(Objects.toString(value, ""));

			if(value != null) {
				customLabel.setData(value);
				tagPanel.setData(value);
				resetButtonStatus(value);

				//Log.d("aa" + ((DeviceListData) value).selectedinstalloption);

				if(value.getState() == DeviceListData.STATUS_INSTALLING) {
					ii.setImageObserver(new AnimatedObserver(list, index));

					isinstallIcon.setIcon(ii);
				} else {
					IDevice device = value.getDevice();
					OptionsBundle bundle = value.getOptionsBundle();

					if(!device.isOnline() || bundle.isNotInstallOptions() || bundle.isImpossibleInstallOptions()
							|| value.getState() == DeviceListData.STATUS_FAILED) {
						isinstallIcon.setIcon(RImg.INSTALL_BLOCK.getImageIcon());
					} else if(bundle.isInstallOptions() || bundle.isPushOptions()) {
						isinstallIcon.setIcon(RImg.INSTALL_CHECK.getImageIcon());
					}
				}
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				customLabel.setSelected(isSelected);
			} else {
				setBackground(list.getBackground());
				customLabel.setSelected(isSelected);
			}

			if (button != null) {
				if (index == pressedIndex) {
					button.getModel().setSelected(true);
					button.getModel().setArmed(true);
					button.getModel().setPressed(true);
				} else if (index == rolloverIndex) {
					button.getModel().setRollover(true);
				}
			}

			return this;
		}

		private void resetButtonStatus(DeviceListData value) {
			for( Component b: tagPanel.getComponents() ) {

				if(b instanceof JButton || b instanceof JLabel) {
					if(!value.getDevice().isOnline()) {
						b.setEnabled(true);
						continue;
					} else {
						b.setEnabled(true);
					}

					ButtonModel m = ((JButton)b).getModel();
					m.setRollover(false);
					m.setArmed(false);
					m.setPressed(false);
					m.setSelected(false);
				}
			}
		}

	}

	private class CustomLabel extends JLabel
	{
		private static final long serialVersionUID = -9172448518025388689L;

		private final Color selectionColor = new Color ( 82, 158, 202 );

		private boolean selected;
		private DeviceListData data;

		public CustomLabel ()
		{
			super ();
			setOpaque ( false );
			setBorder ( BorderFactory.createEmptyBorder ( 0, 60, 0, 40 ) );
			setFont(new Font(getFont().getName(), Font.BOLD, 9));
		}

		private void setSelected ( boolean selected )
		{
			this.selected = selected;
			setForeground ( selected ? Color.WHITE : Color.BLACK );
		}

		private void setData ( DeviceListData data )
		{
			this.data = data;
			setText ( data.getDeviceName() );
		}

		private void centerString(Graphics g, Rectangle r, String s,
				Font font) {
			FontRenderContext frc =
					new FontRenderContext(null, true, true);

			Rectangle2D r2D = font.getStringBounds(s, frc);
			int rWidth = (int) Math.round(r2D.getWidth());
			int rHeight = (int) Math.round(r2D.getHeight());
			int rX = (int) Math.round(r2D.getX());
			int rY = (int) Math.round(r2D.getY());

			int a = (r.width / 2) - (rWidth / 2) - rX;
			int b = (r.height / 2) - (rHeight / 2) - rY;

			g.setFont(font);
			g.drawString(s, r.x + a, r.y + b);
		}

		@Override
		protected void paintComponent ( Graphics g )
		{
			Graphics2D g2d = ( Graphics2D ) g;
			g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

			if ( selected )
			{
				Area area = new Area ( new Ellipse2D.Double ( 0, 0, 52, 52 ) );
				area.add ( new Area ( new RoundRectangle2D.Double ( 36, 6, getWidth () - 36, 40, 12, 12 ) ) );
				g2d.setPaint ( selectionColor );
				g2d.fill ( area );

				g2d.setPaint ( Color.WHITE );
				g2d.fill ( new Ellipse2D.Double ( 4, 4, 44, 44 ) );
			}

			boolean isOnline = data.getDevice().isOnline();

			g2d.setPaint ( isOnline ? data.getCircleColor() : Color.GRAY );
			g2d.fill ( new Ellipse2D.Double ( 6, 6, 40, 40 ) );

			g2d.setPaint ( Color.WHITE );
			//g2d.drawString("N", 22, 22);

			String osVersion = data.getOsVersion();
			if(osVersion != null) {
				centerString(g2d,new Rectangle(4, 4, 44, 44), osVersion, new Font(getFont().getName(), Font.BOLD, (osVersion.length() < 4) ? 20 : 15));
			}
			if(isOnline) {
				g2d.setPaint ( new Color(116, 211, 109) ); // online color
			} else if(data.getDevice().isOffline()) {
				g2d.setPaint(Color.GRAY);
			} else {
				g2d.setPaint(Color.ORANGE);
			}
			g2d.fill ( new Ellipse2D.Double ( getWidth () - 18 - 10, getHeight () / 2 - 9, 18, 18 ) );

			g.setFont(new Font(getFont().getName(), Font.BOLD, 15));
			super.paintComponent ( g );
		}

		@Override
		public Dimension getPreferredSize ()
		{
			final Dimension ps = super.getPreferredSize ();
			ps.height = 54;
			ps.width = 200;
			return ps;
		}
	}

	class CellButtonsMouseListener extends MouseAdapter {
		private int prevIndex = -1;
		private JButton prevButton;
		private final JList<DeviceListData> list;
		private DefaultListModel<DeviceListData> listModel;

		protected CellButtonsMouseListener(JList<DeviceListData> list) {
			super();
			this.list = list;
			listModel = (DefaultListModel<DeviceListData>) list.getModel();
		}

		@Override public void mouseMoved(MouseEvent e) {
			//JList list = (JList) e.getComponent();
			Point pt = e.getPoint();
			int index = list.locationToIndex(pt);
			if(index < 0) return;
			if (!list.getCellBounds(index, index).contains(pt)) {
				if (prevIndex >= 0) {
					listRepaint(list, list.getCellBounds(prevIndex, prevIndex));
				}
				index = -1;
				prevButton = null;
				return;
			}
			if (index >= 0) {
				JButton button = getButton(list, pt, index);
				DeviceDataRenderer renderer = (DeviceDataRenderer) list.getCellRenderer();
				renderer.button = button;
				if (button != null && button.isEnabled()) {
					button.getModel().setRollover(true);
					renderer.rolloverIndex = index;
					if (!button.equals(prevButton)) {
						listRepaint(list, list.getCellBounds(prevIndex, index));
					}
				} else {
					renderer.rolloverIndex = -1;
					Rectangle r = null;
					if (prevIndex == index) {
						if (prevIndex >= 0 && prevButton != null) {
							r = list.getCellBounds(prevIndex, prevIndex);
						}
					} else {
						r = list.getCellBounds(index, index);
					}
					listRepaint(list, r);
					prevIndex = -1;
				}
				prevButton = button;
			}
			prevIndex = index;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			Point pt = e.getPoint();
			int index = list.locationToIndex(pt);
			if (index >= 0) {
				JButton button = getButton(list, pt, index);
				if (button != null && button.isEnabled()) {
					DeviceDataRenderer renderer = (DeviceDataRenderer) list.getCellRenderer();
					renderer.pressedIndex = -1;
					renderer.button = null;
					//button.doClick();
					listRepaint(list, list.getCellBounds(index, index));

					DeviceListData temp = listModel.get(list.getSelectedIndex());

					if(temp.getState() != DeviceListData.STATUS_SETTING) return;

					if(!temp.getOptionsBundle().isInstalled()) {
						//temp.showstate = DeviceListData.SHOW_INSTALL_OPTION;
						Log.e("!getOptionsBundle().isInstalled()");
						return;
					}
					//list.repaint();
					listRepaint(list, list.getCellBounds(index, index));
					actionlistener.actionPerformed(new ActionEvent(temp, 0, button.getActionCommand()));
				}
			}
		}

		private void listRepaint(JList<DeviceListData> list, Rectangle rect) {
			if (rect != null) {
				list.repaint(rect);
			}
		}

		private JButton getButton(JList<DeviceListData> list, Point pt, int index) {
			DeviceDataRenderer renderer = (DeviceDataRenderer) list.getCellRenderer();
			Component c = renderer.getListCellRendererComponent(list, null, index, false, false);
			Rectangle r = list.getCellBounds(index, index);
			c.setBounds(r);
			//c.doLayout(); //may be needed for mone LayoutManager
			pt.translate(-r.x, -r.y);
			Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
			if (b instanceof JButton) {
				return (JButton) b;
			} else {
				return null;
			}
		}
	}

	class AnimatedObserver implements ImageObserver
	{
		JList<?> list;
		int index;

		public AnimatedObserver(JList<?> list2, int index) {
			this.list = list2;
			this.index = index;
		}

		public boolean imageUpdate (Image img, int infoflags, int x, int y, int width, int height) {
			if ((infoflags & (FRAMEBITS|ALLBITS)) != 0) {
				Rectangle rect = list.getCellBounds(index, index);
				list.repaint(rect);
			}

			return (infoflags & (ALLBITS|ABORT)) == 0;
		}
	}
}
