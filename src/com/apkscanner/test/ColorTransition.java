package com.apkscanner.test;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class ColorTransition extends JPanel {

   private static final int TRANSITION_DELAY = 30;
   private static final int PREF_W = 800;
   private static final int PREF_H = 600;
   private RgbSliderPanel rgbSliderPanel1 = new RgbSliderPanel("Color 1");
   private RgbSliderPanel rgbSliderPanel2 = new RgbSliderPanel("Color 2");
   private Color background1;
   private Color background2;
   private JButton button = new JButton(new ButtonAction("Push Me"));

   public ColorTransition() {
      setBackground(Color.black);

      add(rgbSliderPanel1.getMainPanel());
      add(rgbSliderPanel2.getMainPanel());

      add(button);

      rgbSliderPanel1.addPropertyChangeListener(new PropertyChangeListener() {

         @Override
         public void propertyChange(PropertyChangeEvent evt) {
            if (RgbSliderPanel.COLOR.equals(evt.getPropertyName())) {
               setBackground(rgbSliderPanel1.calculateColor());
            }
         }
      });
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }

   @Override
   public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      button.setEnabled(enabled);
      rgbSliderPanel1.setEnabled(enabled);
      rgbSliderPanel2.setEnabled(enabled);
   }

   private class ButtonAction extends AbstractAction {

      public ButtonAction(String name) {
         super(name);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         ColorTransition.this.setEnabled(false);
         background1 = rgbSliderPanel1.calculateColor();
         background2 = rgbSliderPanel2.calculateColor();

         setBackground(background1);

         Timer timer = new Timer(TRANSITION_DELAY, new TransitionListener());
         timer.start();
      }

      private class TransitionListener implements ActionListener {
         private int index = 0;

         @Override
         public void actionPerformed(ActionEvent e) {
            if (index > 100) {
               ((Timer) e.getSource()).stop();
               ColorTransition.this.setEnabled(true);
            } else {
               int r = (int) (background2.getRed() * index / 100.0 + background1
                     .getRed() * (100 - index) / 100.0);
               int g = (int) (background2.getGreen() * index / 100.0 + background1
                     .getGreen() * (100 - index) / 100.0);
               int b = (int) (background2.getBlue() * index / 100.0 + background1
                     .getBlue() * (100 - index) / 100.0);
               setBackground(new Color(r, g, b));
            }
            index++;
         }
      }
   }

   private static void createAndShowGui() {
      JFrame frame = new JFrame("ColorTransition");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(new ColorTransition());
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            createAndShowGui();
         }
      });
   }
}

enum Rgb {
   RED("Red"), GREEN("Green"), BLUE("Blue");
   private String name;

   private Rgb(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }
}

class RgbSliderPanel {
   public static final String COLOR = "color";
   private JPanel mainPanel = new JPanel();
   private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(
         this);
   private Map<Rgb, JSlider> colorSliderMap = new EnumMap<>(Rgb.class);
   private String name;
   protected Color color;

   public RgbSliderPanel(String name) {
      this.name = name;
      mainPanel.setBorder(BorderFactory.createTitledBorder(name));
      //mainPanel.setOpaque(false);
      mainPanel.setLayout(new GridLayout(0, 1));
      for (Rgb rgb : Rgb.values()) {
         JSlider colorSlider = new JSlider(0, 255, 0);
         colorSliderMap.put(rgb, colorSlider);
         mainPanel.add(colorSlider);
         colorSlider.setBorder(BorderFactory.createTitledBorder(rgb.getName()));
         colorSlider.setPaintTicks(true);
         colorSlider.setPaintTrack(true);
         colorSlider.setMajorTickSpacing(50);
         colorSlider.setMinorTickSpacing(10);
         colorSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
               Color oldValue = color;
               Color newValue = calculateColor();
               color = newValue;
               propertyChangeSupport.firePropertyChange(COLOR, oldValue,
                     newValue);
            }
         });

      }
   }

   public JComponent getMainPanel() {
      return mainPanel;
   }

   public void setEnabled(boolean enabled) {
      for (JSlider slider : colorSliderMap.values()) {
         slider.setEnabled(enabled);
      }
   }

   public Color calculateColor() {
      int r = colorSliderMap.get(Rgb.RED).getValue();
      int g = colorSliderMap.get(Rgb.GREEN).getValue();
      int b = colorSliderMap.get(Rgb.BLUE).getValue();
      return new Color(r, g, b);
   }

   public String getName() {
      return name;
   }

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      propertyChangeSupport.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      propertyChangeSupport.removePropertyChangeListener(listener);
   }
}
