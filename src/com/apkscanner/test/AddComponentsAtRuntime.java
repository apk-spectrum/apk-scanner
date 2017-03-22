package com.apkscanner.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class AddComponentsAtRuntime {

    private JFrame f;
    private JPanel panel;
    private JCheckBox checkValidate, checkReValidate, checkRepaint, checkPack;

    public AddComponentsAtRuntime() {
        JButton b = new JButton();
        b.setBackground(Color.red);
        b.setBorder(new LineBorder(Color.black, 2));
        b.setPreferredSize(new Dimension(600, 10));
        panel = new JPanel(new GridLayout(0, 1));
        panel.add(b);
        f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(panel, "Center");
        f.add(getCheckBoxPanel(), "South");
        f.setLocation(200, 200);
        f.pack();
        
        
        
        f.setVisible(true);
    }

    private JPanel getCheckBoxPanel() {
        checkValidate = new JCheckBox("validate");
        checkValidate.setSelected(false);
        checkReValidate = new JCheckBox("revalidate");
        checkReValidate.setSelected(false);
        checkRepaint = new JCheckBox("repaint");
        checkRepaint.setSelected(false);
        checkPack = new JCheckBox("pack");
        checkPack.setSelected(false);
        JButton addComp = new JButton("Add New One");
        addComp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JButton b = new JButton();
                b.setBackground(Color.red);
                b.setBorder(new LineBorder(Color.black, 2));
                b.setPreferredSize(new Dimension(600, 10));
                panel.add(b);
                makeChange();
                System.out.println(" Components Count after Adds :" + panel.getComponentCount());
            }
        });
        JButton removeComp = new JButton("Remove One");
        removeComp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int count = panel.getComponentCount();
                if (count > 0) {
                    panel.remove(0);
                }
                makeChange();
                System.out.println(" Components Count after Removes :" + panel.getComponentCount());
            }
        });
        JPanel panel2 = new JPanel();
        panel2.add(checkValidate);
        panel2.add(checkReValidate);
        panel2.add(checkRepaint);
        panel2.add(checkPack);
        panel2.add(addComp);
        panel2.add(removeComp);
        return panel2;
    }

    private void makeChange() {
        if (checkValidate.isSelected()) {
            panel.validate();
        }
        if (checkReValidate.isSelected()) {
            panel.revalidate();
        }
        if (checkRepaint.isSelected()) {
            panel.repaint();
        }
        if (checkPack.isSelected()) {
            f.pack();
        }
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
		AddComponentsAtRuntime makingChanges = new AddComponentsAtRuntime();
    }
}