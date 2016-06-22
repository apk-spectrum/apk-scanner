package com.apkscanner.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

class SearchRenderer implements TableCellRenderer {
	  private static final Color BACKGROUND_SELECTION_COLOR = new Color(120, 140, 155);
	  private final transient Highlighter.HighlightPainter highlightPainter
	    = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	  private final JTextField field = new JTextField();
	  private String pattern = "";
	  private String prev;
	  private int selectedIndex;

	  public boolean setPattern(String str) {
	    if (str == null || str.equals(pattern)) {
	      return false;
	    } else {
	      prev = pattern;
	      pattern = str;
	      return true;
	    }
	  }
	  
	  public void findNext() {
		  
	  }
	  
	  public void findbefore() {
		  
	  }
	  
	  public SearchRenderer() {
	    super();
	    field.setOpaque(true);
	    field.setBorder(BorderFactory.createEmptyBorder());
	    field.setForeground(Color.BLACK);
	    field.setBackground(Color.WHITE);
	    field.setEditable(false);
	  }
	  @Override public Component getTableCellRendererComponent(
	    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    String txt = Objects.toString(value, "");
	    Highlighter highlighter = field.getHighlighter();
	    highlighter.removeAllHighlights();
	    field.setText(txt);
	    field.setBackground(isSelected ? BACKGROUND_SELECTION_COLOR : Color.WHITE);
	    
	    if (pattern != null && !pattern.isEmpty() && !pattern.equals(prev)) {
	      Matcher matcher = Pattern.compile(pattern).matcher(txt);
	      if (matcher.find()) {
	        int start = matcher.start();
	        int end   = matcher.end();
	        try {
	          highlighter.addHighlight(start, end, highlightPainter);
	          if(!isSelected)field.setBackground(Color.GRAY);
	        } catch (BadLocationException e) {
	          e.printStackTrace();
	        }
	      }
	    }
	    return field;
	  }
	}
