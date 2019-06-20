package com.apkscanner.resource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.apkscanner.util.Log;

public enum RComp implements ResValue<RComp>, PropertyChangeListener
{
	BTN_TOOLBAR_OPEN				(RStr.BTN_OPEN, RImg.TOOLBAR_OPEN.getImageIcon(40, 40) , RStr.BTN_OPEN_LAB),
	; // ENUM END

	private RStr text, toolTipText;
	private Icon icon;
	private List<JComponent> list;

	private RComp(RStr text) {
		this(text, null);
	}

	private RComp(RStr text, Icon icon) {
		this(text, icon, null);
	}

	private RComp(RStr text, Icon icon, RStr toolTipText) {
		this.text = text;
		this.icon = icon;
		this.toolTipText = toolTipText;
	}

	@Override
	public String getValue() {
		return text != null ? text.getValue() : null;
	}

	@Override
	public RComp get() {
		return this;
	}

	public String getText() {
		return text != null ? text.getString() : null;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getToolTipText() {
		return toolTipText != null ? toolTipText.getString() : null;
	}

	public void apply(JComponent c) {
		applyText(c);
		if(c instanceof AbstractButton) {
			AbstractButton comp = (AbstractButton) c;
			if(icon != null) {
				comp.setIcon(icon);
			}
		} else if(c instanceof JLabel) {
			JLabel comp = (JLabel) c;
			if(icon != null) {
				comp.setIcon(icon);
			}
		}
	}

	public void applyText(JComponent c) {
		if(c instanceof AbstractButton) {
			AbstractButton comp = (AbstractButton) c;
			if(text != null) {
				comp.setText(text.getString());
			}
			if(toolTipText != null) {
				comp.setToolTipText(toolTipText.getString());
			}
		} else if(c instanceof JLabel) {
			JLabel comp = (JLabel) c;
			if(text != null) {
				comp.setText(text.getString());
			}
			if(toolTipText != null) {
				comp.setToolTipText(toolTipText.getString());
			}
		}
	}

	public void autoReapply(JComponent c, boolean useAutoReapply) {
		if(c == null) return;
		if(useAutoReapply) {
			apply(c);
			registeReapply(c);
		} else {
			removeReapply(c);
		}
	}

	public void registeReapply(JComponent c) {
		if(list == null) {
			list = new ArrayList<>();
			RProp.LANGUAGE.addPropertyChangeListener(this);
		}
		if(!list.contains(c)) {
			list.add(c);
		}
	}

	public void removeReapply(JComponent c) {
		if(list != null && list.contains(c)) {
			list.remove(c);
			if(list.isEmpty()) {
				list = null;
				RProp.LANGUAGE.removePropertyChangeListener(this);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(list == null) return;
		for(JComponent c: list) {
			Log.e("c " + c);
			//if(c.isValidateRoot()) {
				applyText(c);
			//}
		}
	}
}