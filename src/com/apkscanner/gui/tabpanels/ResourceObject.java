package com.apkscanner.gui.tabpanels;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.gui.component.TreeNodeImageObserver;
import com.apkscanner.resource.RImg;
import com.apkscanner.util.SystemUtil;

public class ResourceObject {
	public static final int ATTR_AXML = 1;
	public static final int ATTR_XML = 2;
	public static final int ATTR_IMG = 3;
	public static final int ATTR_QMG = 4;
	public static final int ATTR_TXT = 5;
	public static final int ATTR_CERT = 6;
	public static final int ATTR_FS_IMG = 7;
	public static final int ATTR_ETC = 8;

	public final String label;
	public final boolean isFolder;
	public final String path;
	public final String config;
	public final ResourceType type;
	public final int attr;

	private boolean isLoading;
	private Icon icon;
	private DefaultMutableTreeNode node;

	public ResourceObject(File file) {
		label = file.getName();
		path = file.getAbsolutePath();
		isFolder = file.isDirectory();

		type = ResourceType.LOCAL;
		attr = getAttr(path);
		config = null;

		isLoading = false;
	}

	public ResourceObject(String path, boolean isFolder) {
		this.path = path;
		this.isFolder = isFolder;

		isLoading = false;
		type = ResourceType.getType(path);
		attr = getAttr(path);

		if (type.getInt() <= ResourceType.XML.getInt()
				&& path.startsWith("res/" + type.toString() + "-")) {
			config = path.replaceAll("res/" + type.toString() + "-([^/]*)/.*", "$1");
		} else {
			config = null;
		}

		if (isFolder) {
			label = Resources.getOnlyFoldername(path);
		} else {
			label = Resources.getOnlyFilename(path);
		}
	}

	void setNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	private int getAttr(String path) {
		int attr = 0;
		String extension = path.replaceAll(".*/", "").replaceAll(".*\\.", ".").toLowerCase();

		if (extension.endsWith(".xml")) {
			if (path.startsWith("res/") || path.equals("AndroidManifest.xml"))
				attr = ATTR_AXML;
			else
				attr = ATTR_XML;
		} else if (extension.endsWith(".png") || extension.endsWith(".jpg") || extension.endsWith(".gif")
				|| extension.endsWith(".bmp") || extension.endsWith(".webp")) {
			attr = ATTR_IMG;
		} else if (extension.endsWith(".qmg")) {
			attr = ATTR_QMG;
		} else if (extension.endsWith(".txt") || extension.endsWith(".mk") || extension.endsWith(".html")
				|| extension.endsWith(".js") || extension.endsWith(".css") || extension.endsWith(".json")
				|| extension.endsWith(".props") || extension.endsWith(".properties") || extension.endsWith(".policy")
				|| extension.endsWith(".mf") || extension.endsWith(".sf") || extension.endsWith(".rc")
				|| extension.endsWith(".version") || extension.endsWith(".default")) {
			attr = ATTR_TXT;
		} else if(extension.endsWith(".rsa") || extension.endsWith(".dsa") || extension.endsWith(".ec")) {
			attr = ATTR_CERT;
		} else if(extension.endsWith(".img")) {
			attr = ATTR_FS_IMG;
		} else {
			attr = ATTR_ETC;
		}
		return attr;
	}

	@Override
	public String toString() {
		String str = null;
		int childCount = 0;
		if(node != null /* && !isFolder */)
			childCount = node.getChildCount();
		if (childCount > 0) {
			str = label + " (" + childCount + ")";
		} else if (config != null && !config.isEmpty()) {
			str = label + " (" + config + ")";
		} else {
			str = label;
		}
		return str;
	}

	public Icon getIcon() {
		if(icon != null) return icon;

		if(getLoadingState()) {
			icon = RImg.RESOURCE_TREE_OPEN_JD.getImageIcon();
		} else if(isFolder) {
			icon = SystemUtil.getExtensionIcon(SystemUtil.FOLDER_ICON);
		}
		if(icon != null) return icon;

		switch (attr) {
		case ATTR_IMG:
			String apkFilePath = null;
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) node.getRoot();
			if(root.getUserObject() instanceof ResourceObject) {
				apkFilePath = ((ResourceObject) root.getUserObject()).path;
			} else {
				apkFilePath = root.getUserObject().toString();
			}
			if (apkFilePath != null && new File(apkFilePath).exists()) {
				String urlFilePath = apkFilePath.replaceAll("#", "%23");
				String jarPath = "jar:file:" + urlFilePath + "!/";
				try {
					Image tempImage = null;
					if(path.toLowerCase().endsWith(".webp")) {
						tempImage = ImageScaler.getScaledImage(new ImageIcon(ImageIO.read(new URL(jarPath + path))), 32, 32);
					} else {
						tempImage = ImageScaler.getScaledImage(new ImageIcon(new URL(jarPath + path)), 32, 32);
					}
					icon = new ImageIcon(tempImage);
					tempImage.flush();
				} catch (IOException|NullPointerException e1) {
					//e1.printStackTrace();
				}
			}
			break;
		case ATTR_AXML:
		case ATTR_XML:
		case ATTR_QMG:
		case ATTR_TXT:
		case ATTR_CERT:
		case ATTR_ETC:
		case ATTR_FS_IMG:
		default:
		}

		return (icon != null) ? icon : SystemUtil.getExtensionIcon(getExtension(path));
	}

	public Icon getIconWithObserver(JTree tree) {
		Icon icon = getIcon();
		if(icon instanceof ImageIcon) {
			ImageIcon image = (ImageIcon) icon;
			image.setImageObserver(new TreeNodeImageObserver(tree, node, image) {
				public void stop() {
					stopFlag = true;
					if(node.getUserObject() instanceof ResourceObject) {
						((ResourceObject)node.getUserObject()).setLoadingState(false);
					}
				}

				protected boolean isStop() {
					if(node.getUserObject() instanceof ResourceObject) {
						stopFlag = !((ResourceObject)node.getUserObject()).getLoadingState();
					}
					return stopFlag;
				}
			});
		}
		return icon;
	}

	public void setLoadingState(boolean state) {
		if(isLoading == state) return;
		isLoading = state;
		icon = null;
	}

	public boolean getLoadingState() {
		return isLoading;
	}

	public static String getExtension(String path) {
		String suffix = path.replaceAll(".*/", "");
		if (suffix.contains(".")) {
			suffix = suffix.replaceAll(".*\\.", ".");
		} else {
			suffix = "";
		}
		return suffix.toLowerCase();
	}
}