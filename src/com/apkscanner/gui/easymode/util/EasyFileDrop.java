package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Component;

import com.apkscanner.gui.util.FileDrop;

public class EasyFileDrop extends FileDrop{

	public EasyFileDrop(Component c, Listener listener) {
		//super(c, listener);
		// TODO Auto-generated constructor stub
		
		super(null, // Logging stream
				c, // Drop target
				javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, Color.RED), // Drag
																								// border
				true, // Recursive
				listener);
	}

}
