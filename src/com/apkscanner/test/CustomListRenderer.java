package com.apkscanner.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;

/**
 * @author Mikle Garin
 * @see http://stackoverflow.com/a/18589264/909085
 */

public class CustomListRenderer extends DefaultListCellRenderer
{
	private static final long serialVersionUID = -1867006118377826954L;

	//private static final ImageIcon crossIcon = new ImageIcon(Resource.class.getResource("/icons/logo/base.png"));
    private static ImageIcon tipIcon = RImg.getImageIcon("/icons/logo/nougat.png");
    private static ImageIcon tipIcon1 = RImg.getImageIcon("/icons/logo/marshmallow.png");
    private static ImageIcon tipIcon2 = RImg.getImageIcon("/icons/logo/jelly_bean.png");
    
    
    //private static int LIST_HEIGHT = 60;
    
    /**
     * Sample frame with list.
     *
     * @param args arguments
     */
    public static void main ( String[] args )
    {
        JFrame frame = new JFrame ( "Custom list renderer" );

        DefaultListModel<CustomData> model = new DefaultListModel<CustomData> ();
        model.addElement ( new CustomData ( new Color ( 209, 52, 23 ), 0, "SC-02J" ) );
        model.addElement ( new CustomData ( new Color ( 135, 163, 14 ), 1, "SC-04J" ) );
        model.addElement ( new CustomData ( new Color ( 204, 204, 204 ), 2, "SC-05J" ) );
        model.addElement ( new CustomData ( new Color ( 90, 90, 90 ), 3, "SCH-44566" ) );

        Image tipIconimg = tipIcon.getImage();  //ImageIcon을 Image로 변환.
        Image result = tipIconimg.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
        tipIcon = new ImageIcon(result); //Image로 ImageIcon 생성

        tipIconimg = tipIcon1.getImage();  //ImageIcon을 Image로 변환.
        result = tipIconimg.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
        tipIcon1 = new ImageIcon(result); //Image로 ImageIcon 생성

        tipIconimg = tipIcon2.getImage();  //ImageIcon을 Image로 변환.
        result = tipIconimg.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
        tipIcon2 = new ImageIcon(result); //Image로 ImageIcon 생성
        
        
        JList<?> list = new JList<CustomData> ( model );
        list.setCellRenderer ( new CustomListRenderer ( list ) );
        list.setBorder ( BorderFactory.createEmptyBorder ( 5, 5, 5, 5 ) );
        frame.add ( list );

        frame.pack ();
        frame.setLocationRelativeTo ( null );
        frame.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        frame.setVisible ( true );
    }

    private CustomLabel renderer;
    public CustomListRenderer ( final JList<?> list )
    {
        super ();
        renderer = new CustomLabel ();
    }

    @Override
    public Component getListCellRendererComponent ( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus )
    {
        renderer.setSelected ( isSelected );
        renderer.setData ( ( CustomData ) value );
        return renderer;
    }

    /**
     * Label that has some custom decorations.
     */
    private static class CustomLabel extends JLabel
    {
		private static final long serialVersionUID = -458675320336838995L;

		private static final Color selectionColor = new Color ( 82, 158, 202 );

        private boolean selected;
        private CustomData data;

        public CustomLabel ()
        {
            super ();
            setOpaque ( false );
            setBorder ( BorderFactory.createEmptyBorder ( 0, 70, 0, 40 ) );
            setFont(new Font(getFont().getName(), Font.BOLD, 20));
        }

        private void setSelected ( boolean selected )
        {
            this.selected = selected;
            setForeground ( selected ? Color.WHITE : Color.BLACK );
        }

        private void setData ( CustomData data )
        {
            this.data = data;
            setText ( data.getName () );
        }

        @Override
        protected void paintComponent ( Graphics g )
        {
            Graphics2D g2d = ( Graphics2D ) g;
            g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

            if ( selected )
            {
                Area area = new Area ( new Ellipse2D.Double ( 0, 0, 72, 72 ) );
                area.add ( new Area ( new RoundRectangle2D.Double ( 36, 6, getWidth () - 36, 58, 12, 12 ) ) );
                g2d.setPaint ( selectionColor );
                g2d.fill ( area );

                g2d.setPaint ( Color.WHITE );
                g2d.fill ( new Ellipse2D.Double ( 4, 4, 64, 64 ) );
            }

            g2d.setPaint ( data.getCircleColor () );
            g2d.fill ( new Ellipse2D.Double ( 10, 10, 52, 52 ) );
            //g2d.drawImage ( tipIcon.getImage (), 5 + 13 - tipIcon.getIconWidth () / 2, 5 + 13 - tipIcon.getIconHeight () / 2, null );
            g2d.drawImage ( tipIcon.getImage (), 10 + 26 - tipIcon.getIconWidth () / 2, 10 + 26 - tipIcon.getIconHeight () / 2, null );
            
            if ( selected )
            {
//                g2d.drawImage ( crossIcon.getImage (), getWidth () - 9 - 5 - crossIcon.getIconWidth () / 2,
//                        getHeight () / 2 - crossIcon.getIconHeight () / 2, null );
            }

            switch(data.getNewMessages ()) {
        	case 0:
        		g2d.setPaint ( Color.LIGHT_GRAY );
        		g2d.drawImage ( tipIcon.getImage (), 10 + 26 - tipIcon.getIconWidth () / 2, 10 + 26 - tipIcon.getIconHeight () / 2, null );
        		break;
        	case 1:
        		g2d.setPaint ( Color.GREEN );
        		g2d.drawImage ( tipIcon2.getImage (), 10 + 26 - tipIcon2.getIconWidth () / 2, 10 + 26 - tipIcon2.getIconHeight () / 2, null );
        		break;
        	case 2:
        		g2d.setPaint ( Color.LIGHT_GRAY );
        		g2d.drawImage ( tipIcon1.getImage (), 10 + 26 - tipIcon1.getIconWidth () / 2, 10 + 26 - tipIcon1.getIconHeight () / 2, null );
        		break;
        	case 3:
        		g2d.setPaint ( Color.GREEN );
        		g2d.drawImage ( tipIcon2.getImage (), 10 + 26 - tipIcon2.getIconWidth () / 2, 10 + 26 - tipIcon2.getIconHeight () / 2, null );
        		break;
        	}
        	
            
            g2d.fill ( new Ellipse2D.Double ( getWidth () - 18 - 10, getHeight () / 2 - 18, 18, 18 ) );
//
//                final String text = "" + data.getNewMessages ();
//                final Font oldFont = g2d.getFont ();
//                g2d.setFont ( oldFont.deriveFont ( oldFont.getSize () - 1f ) );
//                final FontMetrics fm = g2d.getFontMetrics ();
//                g2d.setPaint ( Color.WHITE );
//                g2d.drawString ( text, getWidth () - 9 - 5 - fm.stringWidth ( text ) / 2,
//                        getHeight () / 2 + ( fm.getAscent () - fm.getLeading () - fm.getDescent () ) / 2 );
//                g2d.setFont ( oldFont );

            super.paintComponent ( g );
        }

        @Override
        public Dimension getPreferredSize ()
        {
            final Dimension ps = super.getPreferredSize ();
            ps.height = 72;
            return ps;
        }
    }

    /**
     * Custom data for our list.
     */
    private static class CustomData
    {
        private Color circleColor;
        private int newMessages;
        private String name;

        public CustomData ( Color circleColor, int newMessages, String name )
        {
            super ();
            this.circleColor = circleColor;
            this.newMessages = newMessages;
            this.name = name;
        }

        private Color getCircleColor ()
        {
            return circleColor;
        }

        private int getNewMessages ()
        {
            return newMessages;
        }

        private String getName ()
        {
            return name;
        }
    }
}