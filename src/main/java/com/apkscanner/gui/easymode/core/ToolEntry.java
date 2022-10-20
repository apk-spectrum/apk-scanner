package com.apkscanner.gui.easymode.core;

import java.io.Serializable;

import javax.swing.ImageIcon;

import com.apkscanner.resource.RComp;

public class ToolEntry implements Serializable
{
	private static final long serialVersionUID = 8819359209402028535L;

	private String title;
	private String description;
	private ImageIcon image = null;
	private String actionCommand;

	public ToolEntry(String title, String imagePath) {
		this.title = title;
		image = new ImageIcon(imagePath);
		this.description = "";
	}

	public ToolEntry(String title, ImageIcon image) {
		this(title, null, image, null);
	}

	public ToolEntry(String title, String description, ImageIcon image) {
		this(title, description, image, null);
	}

	public ToolEntry(String title, String description, ImageIcon image, String actionCommand) {
		this.title = title;
		this.image = image;
		this.description = description;
		this.actionCommand = actionCommand;
	}

	public ToolEntry(RComp res, String actionCommand) {
		this(res.getText(), res.getToolTipText(), (ImageIcon)res.getIcon(), actionCommand);
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

	public String getActionCommand() {
		return actionCommand != null ? actionCommand : title;
	}

	// Override standard toString method to give a useful result
	public String toString() {
		return title;
	}
}
