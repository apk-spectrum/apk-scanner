package com.apkscanner.gui.easymode.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EasyRoundLabelCount extends RoundPanel implements MouseListener{
	private static final long serialVersionUID = 963938688570939089L;
	EasyTextField textlabel = null;
	boolean entered = false;
	private Color backgroundcolor;
	boolean mouseover = false;
	boolean clipboard = false;
	JPanel countpanel;

	public EasyRoundLabelCount(String str, Color backgroundColor, Color foregroundColor) {
		textlabel = new EasyTextField(str);
		//setBackground(backgroundColor);
		this.backgroundcolor = backgroundColor;
		setRoundrectColor(backgroundColor);
		setLayout(new BorderLayout());
		//setOpaque(false);
		textlabel.setForeground(foregroundColor);
		setEasyTextField(textlabel);


		countpanel = new JPanel(new BorderLayout());
		countpanel.setOpaque(false);
		countpanel.add(textlabel);
		add(countpanel, BorderLayout.CENTER);
	}
	public void addCountpanel(JComponent com) {
		//countpanel.add(com);
		countpanel.add(com, BorderLayout.WEST);
	}

	public void setMouseHoverEffect(boolean flag) {
		mouseover = flag;
		if(flag) {
			textlabel.addMouseListener(this);
		}
		else {
			textlabel.removeMouseListener(this);
		}
	}

	public void setclipboard(boolean flag) {
		this.clipboard = flag;
	}

	private void setEasyTextField(JTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		textfield.setEditable(false);
		textfield.setOpaque(false);
		//textfield.setBackground(Color.BLACK);
		textfield.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
	}
	public void setText(String str) {
		textlabel.setText(str);
	}

	public void setTextFont(Font font) {
		textlabel.setFont(font);
	}

	public String getText() {
		return textlabel.getText();
	}

	public void setHorizontalAlignment(int jtextfield) {
		textlabel.setHorizontalAlignment(jtextfield);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(clipboard) {
			StringSelection stringSelection = new StringSelection(textlabel.getText());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
			AndroidLikeToast.ShowToast("Copying to the clipboard!",this);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
    	entered = true;
    	setRoundrectColor(backgroundcolor.darker().darker());
    	super.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		entered = false;
		setRoundrectColor(backgroundcolor);
    	super.repaint();
	}


	public void setMouseListener(MouseListener listener) {
		textlabel.addMouseListener(listener);
	}

}
