package com.apkscanner.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class JXTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 7931638587933453293L;

	private ITabRenderer tabRenderer = new DefaultTabRenderer();

    public JXTabbedPane() {
        super();
    }

    public JXTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }

    public JXTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }

    public ITabRenderer getTabRenderer() {
        return tabRenderer;
    }

    public void setTabRenderer(ITabRenderer tabRenderer) {
        this.tabRenderer = tabRenderer;
    }

    @Override
    public void addTab(String title, Component component) {
        this.addTab(title, null, component, null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        this.addTab(title, icon, component, null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
        int tabIndex = getTabCount() - 1;
        Component tab = tabRenderer.getTabRendererComponent(this, title, icon, tabIndex);
        super.setTabComponentAt(tabIndex, tab);
    }
}

interface ITabRenderer {

    public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex);

}



class DefaultTabRenderer extends AbstractTabRenderer implements PropertyChangeListener {

    private Component prototypeComponent;

    public DefaultTabRenderer() {
        super();
        prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
        addPropertyChangeListener(this);
    }

    private Component generateRendererComponent(String text, Icon icon, int horizontalTabTextAlignmen) {
        JPanel rendererComponent = new JPanel(new GridBagLayout());
        rendererComponent.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        rendererComponent.add(new JLabel(icon), c);

        c.gridx = 1;
        c.weightx = 1;
        rendererComponent.add(new JLabel(text, horizontalTabTextAlignmen), c);

        return rendererComponent;
    }

    @Override
    public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex) {
        Component rendererComponent = generateRendererComponent(text, icon, getHorizontalTextAlignment());
        int prototypeWidth = prototypeComponent.getPreferredSize().width;
        int prototypeHeight = prototypeComponent.getPreferredSize().height;
        rendererComponent.setPreferredSize(new Dimension(prototypeWidth, prototypeHeight));
        return rendererComponent;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if ("prototypeText".equals(propertyName) || "prototypeIcon".equals(propertyName)) {
            this.prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
        }
    }
}
