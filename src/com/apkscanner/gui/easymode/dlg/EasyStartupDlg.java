package com.apkscanner.gui.easymode.dlg;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.apkscanner.gui.component.HtmlEditorPane;
import com.apkscanner.gui.easymode.util.ImageSliderPanel;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;

public class EasyStartupDlg
{
	
	
	static public boolean showAboutDialog(Component component)
	{
		boolean needreStart = false;
		// html content
		StringBuilder body = new StringBuilder();
		body.append("<div id=\"about\">");
		body.append("  <H1>" + "Easy GUI" + "</H1>");
		body.append("  <H3>" + RStr.FEATURE_LAB.get() + "</H3>");
		body.append("   - "+ RStr.EASY_GUI_FEATURE1.get() + "<br/>");
		body.append("   - "+ RStr.EASY_GUI_FEATURE2.get() + "<br/>");
		body.append("   - "+ RStr.EASY_GUI_FEATURE3.get() + "<br/>");
		body.append("  <hr/>");
		body.append("  " + RStr.EASY_GUI_UES_QUESTION.get());
		body.append("</div>");

		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuilder style = new StringBuilder("#about {");
		style.append("width:350px;margin:0px;padding:0px;");
		style.append("background-color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");
		style.append("H1 {margin-top: 0px; margin-bottom: 0px;}");
		style.append("H3 {margin-top: 5px; margin-bottom: 0px;}");

		// html content
		HtmlEditorPane hep = new HtmlEditorPane("", style.toString(), body.toString());
		ImageSliderPanel imagePanel = new ImageSliderPanel(RImg.PREVIEW_EASY1.getImageIcon());
		//imagePanel.setAlignmentY(0.0f);
		//imagePanel.setBorder(new LineBorder(Color.black));
		imagePanel.add(RImg.PREVIEW_EASY2.getImageIcon());
		imagePanel.add(RImg.PREVIEW_EASY3.getImageIcon());
		//imagePanel.add(RImg.PREVIEW_EASY3.getImageIcon());
		//imagePanel.add(RImg.PREVIEW_EASY4.getImageIcon());
		
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		aboutPanel.add(imagePanel);
		aboutPanel.add(hep);

		JCheckBox check = new JCheckBox(RStr.LABEL_DO_NOT_LOOK_AGAIN.get());
		check.setSelected(true);
		Object[] options = {check, RStr.BTN_YES.get(), RStr.BTN_NO.get()};

		imagePanel.start();
		int x = JOptionPane.showOptionDialog(component, aboutPanel, 
				RStr.BTN_CHECK_UPDATE.get(), JOptionPane.DEFAULT_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
		imagePanel.clean();	
		
		if(check.isSelected()) {
			RProp.B.SKIP_STARTUP_EASY_UI_DLG.set(true);
		} else {
			RProp.B.SKIP_STARTUP_EASY_UI_DLG.set(false);
		}
		
		if(x != -1) {
			boolean setgui;
			if(x == 1) {
				setgui = true;
			} else {				
				setgui = false;
			}
			
			if(RProp.B.USE_EASY_UI.get() != setgui) {
				RProp.B.USE_EASY_UI.set(setgui);
				needreStart = true;
			}
		}
		//"So many options using Object[]", "Don't forget to Tick it!",
		//JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		return needreStart;

		//MessageBoxPane.showMessageDialog(component, aboutPanel, "Update", MessageBoxPane.CLOSED_OPTION, null);
	}

	public static void main(final String[] args) {
		//EasyStartupDlg dlg = new EasyStartupDlg();
		JFrame frame = new JFrame();
		
		
		//Board boa = new Board();
		//frame.add(boa);
		//frame.setVisible(true);
		EasyStartupDlg.showAboutDialog(frame);
		
		System.exit(0);
	}
}
