package com.apkscanner.gui.easymode.dlg;

import java.io.Serializable;

import javax.swing.ImageIcon;

class ToolEntry implements Serializable{
	public final String title;
	public final ImageIcon image;
	
	
	public ToolEntry(String title, String imagePath) {
		this.title = title;
		image = new ImageIcon(imagePath);
	}

	public ToolEntry(String title, ImageIcon image) {
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
