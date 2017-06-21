package com.apkscanner.gui.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public abstract class AbstractTabRenderer implements ITabRenderer {

    private String prototypeText = "";
    private Icon prototypeIcon = UIManager.getIcon("OptionPane.informationIcon");
    private int horizontalTextAlignment = SwingConstants.CENTER;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public AbstractTabRenderer() {
        super();
    }

    public void setPrototypeText(String text) {
        String oldText = this.prototypeText;
        this.prototypeText = text;
        firePropertyChange("prototypeText", oldText, text);
    }

    public String getPrototypeText() {
        return prototypeText;
    }

    public Icon getPrototypeIcon() {
        return prototypeIcon;
    }

    public void setPrototypeIcon(Icon icon) {
        Icon oldIcon = this.prototypeIcon;
        this.prototypeIcon = icon;
        firePropertyChange("prototypeIcon", oldIcon, icon);
    }

    public int getHorizontalTextAlignment() {
        return horizontalTextAlignment;
    }

    public void setHorizontalTextAlignment(int horizontalTextAlignment) {
        this.horizontalTextAlignment = horizontalTextAlignment;
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
        }
    }
}
