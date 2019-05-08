package com.apkscanner.gui.easymode.dlg;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.apkscanner.gui.easymode.test.Board;
import com.apkscanner.gui.easymode.util.ImageSliderPanel;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;

public class EasyStartupDlg
{
	public static boolean needreStart;
	
	static public void showAboutDialog(Component component)
	{
		// html content
		StringBuilder body = new StringBuilder();
		body.append("<div id=\"about\">");
		body.append("  <H1>" + "Easy GUI" + "</H1>");
		body.append("  <H3>Feature</H3>");
		body.append("   - Simple, Light, Fast<br/>");
		body.append("   - Easy switching to origin<br/>");
		body.append("   - Custom Toolbar<br/>");		
		body.append("  <hr/>");
		body.append("  Would you like to use Easy mode? (Can be changed in settings)");
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
		JHtmlEditorPane hep = new JHtmlEditorPane("", style.toString(), body.toString());
		ImageSliderPanel imagePanel = new ImageSliderPanel(Resource.IMG_PREVIEW_EASY.getImageIcon());
		imagePanel.setAlignmentY(0.0f);
		//imagePanel.setBorder(new LineBorder(Color.black));
		imagePanel.add(Resource.IMG_PREVIEW_EASY1.getImageIcon());
		imagePanel.add(Resource.IMG_PREVIEW_EASY2.getImageIcon());
		imagePanel.add(Resource.IMG_PREVIEW_EASY3.getImageIcon());
		//imagePanel.add(Resource.IMG_PREVIEW_EASY4.getImageIcon());
		
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		aboutPanel.add(imagePanel);
		aboutPanel.add(hep);

		JCheckBox check = new JCheckBox("Don't show this again");
		Object[] options = {check, "Yes", "No"};

		imagePanel.start();
		int x = JOptionPane.showOptionDialog(component, aboutPanel, "New Update", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				
		if(check.isSelected()) {
			Resource.PROP_SKIP_STARTUP_EASY_UI_DLG.setData(true);
		} else {
			Resource.PROP_SKIP_STARTUP_EASY_UI_DLG.setData(false);
		}
		
		if(x != -1) {
			boolean setgui;
			if(x == 1) {
				setgui = true;
			} else {				
				setgui = false;
			}
			
			if((boolean)Resource.PROP_USE_EASY_UI.getData() != setgui) {
				Resource.PROP_USE_EASY_UI.setData(setgui);
				needreStart = true;
			}
		}
		//"So many options using Object[]", "Don't forget to Tick it!",
		//JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

		//MessageBoxPane.showMessageDialog(component, aboutPanel, "Update", MessageBoxPane.CLOSED_OPTION, null);
	}

	public static void main(final String[] args) {
		EasyStartupDlg dlg = new EasyStartupDlg();
		JFrame frame = new JFrame();
		
		
		//Board boa = new Board();
		//frame.add(boa);
		//frame.setVisible(true);
		dlg.showAboutDialog(frame);
		System.exit(0);
	}
}
