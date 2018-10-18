package com.apkscanner.gui.easymode.test;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {

	private JFrame compoundFrame;
	private JPanel backgroundPanel;

	Dimension gUISize = new Dimension(400, 400);

	public GUI() {
		buildResizeableFrame();
	}

	public void activate() {
		compoundFrame.setVisible(true);
	}

	private void buildResizeableFrame() {
		compoundFrame = new JFrame();
		FrameComponent frame = new FrameComponent(new Insets(5, 5, 5, 5));
		backgroundPanel = new JPanel();
		compoundFrame.setLayout(null);
		compoundFrame.add(frame);
		compoundFrame.add(backgroundPanel);
		setFrameSizeController(frame, backgroundPanel);
		setFrameController(frame);
		setBackgroundPanelController(backgroundPanel);
		Dimension dimPant = Toolkit.getDefaultToolkit().getScreenSize();
		compoundFrame.setBounds(dimPant.width / 4, dimPant.height / 4, dimPant.width / 2, dimPant.height / 2);
		compoundFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		compoundFrame.setUndecorated(true);
	}

	private void setFrameSizeController(final FrameComponent frame, final JPanel panel) {
		compoundFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension sizeIn = ((JFrame) e.getComponent()).getContentPane().getSize();
				frame.setSize(sizeIn);
				panel.setSize(sizeIn);
			}
		});
	}

	private void setFrameController(FrameComponent frame) {
		ComponentBorderDragger controller = new ComponentBorderDragger(compoundFrame, new Insets(5, 5, 5, 5),
				new Dimension(10, 10));
		frame.addMouseMotionListener(controller);
	}

	private void setBackgroundPanelController(JPanel panel) {
		panel.addMouseMotionListener(new BackgroundComponentDragger(compoundFrame));
	}

	public static void main(String[] args) {
		new GUI().activate();
	}
}