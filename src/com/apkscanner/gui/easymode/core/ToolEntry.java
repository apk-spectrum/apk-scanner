package com.apkscanner.gui.easymode.core;

import java.io.Serializable;
import javax.swing.ImageIcon;


public class ToolEntry implements Serializable{
	private static final long serialVersionUID = 8819359209402028535L;
	String title  = "";
	String description = "";	
	ImageIcon image = null;
	
	public ToolEntry(String title, String imagePath) {
		this.title = title;
		image = new ImageIcon(imagePath);
		this.description = "";
	}

	public ToolEntry(String title, ImageIcon image) {
		this.title = title;
		this.image = image;		
	}

	public ToolEntry(String title, String description, ImageIcon image) {
		this(title, image);
		this.description = description;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}

	public ImageIcon getImage() {
		return image;
	}

	// Override standard toString method to give a useful result
	public String toString() {
		return title;
	}
}
