package com.apkscanner.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.apkscanner.resource.RImg;
import com.apkspectrum.util.Log;

/**
 * @version 1.0 06/20/99
 */
public class AnimatedIconTreeExample extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -7811249051552624726L;

    public AnimatedIconTreeExample() {
        super("AnimatedIconTreeExample");
        String[] strs = {"CARNIVORA", // 0
                "Felidae", // 1
                "Acinonyx jutatus  (cheetah)", // 2
                "Panthera leo  (lion)", // 3
                "Canidae", // 4
                "Canis lupus  (wolf)", // 5
                "Lycaon pictus  (lycaon)", // 6
                "Vulpes Vulpes  (fox)"}; // 7

        IconNode[] nodes = new IconNode[strs.length];
        for (int i = 0; i < strs.length; i++) {
            nodes[i] = new IconNode(strs[i]);
        }
        nodes[0].add(nodes[1]);
        nodes[0].add(nodes[4]);
        nodes[1].add(nodes[2]);
        nodes[1].add(nodes[3]);
        nodes[4].add(nodes[5]);
        nodes[4].add(nodes[6]);
        nodes[4].add(nodes[7]);

        nodes[2].setIcon(RImg.LOADING.getImageIcon());
        nodes[3].setIcon(RImg.LOADING.getImageIcon());
        nodes[5].setIcon(RImg.LOADING.getImageIcon());
        nodes[6].setIcon(RImg.LOADING.getImageIcon());
        nodes[7].setIcon(RImg.LOADING.getImageIcon());

        JTree tree = new JTree(nodes[0]);
        tree.setCellRenderer(new IconNodeRenderer());
        setImageObserver(tree, nodes);

        JScrollPane sp = new JScrollPane(tree);
        getContentPane().add(sp, BorderLayout.CENTER);
    }

    private void setImageObserver(JTree tree, IconNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ImageIcon icon = (ImageIcon) nodes[i].getIcon();
            if (icon != null) {
                icon.setImageObserver(new NodeImageObserver(tree, nodes[i]));
            }
        }
    }

    class NodeImageObserver implements ImageObserver {
        JTree tree;

        DefaultTreeModel model;

        TreeNode node;

        NodeImageObserver(JTree tree, TreeNode node) {
            this.tree = tree;
            this.model = (DefaultTreeModel) tree.getModel();
            this.node = node;
        }

        public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
            if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
                TreePath path = new TreePath(model.getPathToRoot(node));
                Rectangle rect = tree.getPathBounds(path);
                if (rect != null) {
                    tree.repaint(rect);
                }
            }
            return (flags & (ALLBITS | ABORT)) == 0;
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception evt) {
        }

        AnimatedIconTreeExample frame = new AnimatedIconTreeExample();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(280, 200);
        frame.setVisible(true);
    }
}


class IconNodeRenderer extends DefaultTreeCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 2567229320934693663L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Icon icon = ((IconNode) value).getIcon();
        Log.d("Aaaaaaaaaaaaaa");
        if (icon == null) {
            @SuppressWarnings("rawtypes")
            Hashtable icons = (Hashtable) tree.getClientProperty("JTree.icons");
            String name = ((IconNode) value).getIconName();
            if ((icons != null) && (name != null)) {
                icon = (Icon) icons.get(name);
                if (icon != null) {
                    setIcon(icon);
                }
            }
        } else {
            setIcon(icon);
        }

        return this;
    }
}


class IconNode extends DefaultMutableTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -3534956028497946635L;

    protected Icon icon;

    protected String iconName;

    public IconNode() {
        this(null);
    }

    public IconNode(Object userObject) {
        this(userObject, true, null);
    }

    public IconNode(Object userObject, boolean allowsChildren, Icon icon) {
        super(userObject, allowsChildren);
        this.icon = icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getIconName() {
        if (iconName != null) {
            return iconName;
        } else {
            String str = userObject.toString();
            int index = str.lastIndexOf(".");
            if (index != -1) {
                return str.substring(++index);
            } else {
                return null;
            }
        }
    }
}
