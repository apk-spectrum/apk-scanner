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

public class ResourceObject implements Cloneable
{
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

	transient private boolean isLoading;
	transient private Icon icon;
	transient private DefaultMutableTreeNode node;

	public ResourceObject(String path) {
		this(path, false);
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
			label = getFolderName();
		} else {
			label = getFileName();
		}
	}

	public ResourceObject(File file) {
		label = file.getName();
		path = file.getAbsolutePath();
		isFolder = file.isDirectory();

		type = ResourceType.LOCAL;
		attr = getAttr(path);
		config = null;

		isLoading = false;
	}

	public ResourceObject(ResourceType type) {
		this(type.toString(), true);
	}

	void setNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	private int getAttr(String path) {
		String extension = path.replaceAll(".*/", "").replaceAll(".*\\.", ".");
		switch(extension.toLowerCase()) {
		case ".xml":
			if (path.startsWith("res/") || path.equals("AndroidManifest.xml"))
				return ATTR_AXML;
			else
				return ATTR_XML;
		case ".png": case ".jpg": case ".gif": case ".bmp": case ".webp":
			return ATTR_IMG;
		case ".rsa": case ".dsa": case ".ec": case ".der":
			return ATTR_CERT;
		case ".img":
			return ATTR_FS_IMG;
		case ".txt": case ".mk": case ".html": case ".htm": case ".js": case ".css":  case ".json":
		case ".props": case ".properties":  case ".policy": case ".rc": case ".mf": case ".sf":
		case ".version": case ".default": case ".sql": case ".list": case ".ini": case ".inf":
		case ".pro": case ".dtd": case ".xsd": case ".svg": case ".pem": case ".csv":
			return ATTR_TXT;
		default:
			return ATTR_ETC;
		}
	}

	@Override
	public String toString() {
		String str = null;
		int childCount = 0;
		if(node != null && !node.isRoot() /* && !isFolder */
				&& attr != ATTR_FS_IMG)
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
			break;
		}

		return (icon != null) ? icon : SystemUtil.getExtensionIcon(getExtension(path));
	}

	public Icon getIconWithObserver(final JTree tree) {
		Icon icon = getIcon();
		if(icon instanceof ImageIcon) {
			ImageIcon image = (ImageIcon) icon;
			if(!(image.getImageObserver() instanceof TreeNodeImageObserver)) {
				image.setImageObserver(new TreeNodeImageObserver(tree, node, image) {
					@Override
					public void stop() {
						super.stop();
						if(node != null && node.getUserObject() instanceof ResourceObject) {
							((ResourceObject)node.getUserObject()).setLoadingState(false);
						}
					}

					@Override
					protected boolean isStop() {
						if(node != null && node.getUserObject() instanceof ResourceObject) {
							stopFlag = !((ResourceObject)node.getUserObject()).getLoadingState();
						}
						return super.isStop();
					}
				});
			}
		}
		return icon;
	}

	public boolean isRootRes() {
		return (path != null && !path.contains("/"));
	}

	public void setLoadingState(boolean state) {
		if(isLoading == state) return;
		isLoading = state;
		if(icon instanceof ImageIcon) {
			ImageIcon image = (ImageIcon) icon;
			if(image.getImageObserver() != null) {
				image.setImageObserver(null);
				icon = null;
			}
		}
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

	public String getFileName() {
		String separator = path.contains(File.separator) ? separator = File.separator : "/";
		return path.substring(path.lastIndexOf(separator) + 1, path.length());
	}

	public String getFolderName() {
		String separator = path.contains(File.separator) ? separator = File.separator : "/";
		if(!path.contains(separator)) return path;
		return path.substring(0, path.lastIndexOf(separator));
	}

	@Override
	protected Object clone() {
		ResourceObject resObj;
		try {
			resObj = (ResourceObject)super.clone();
			resObj.node = null;
			resObj.icon = null;
			resObj.isLoading = false;
		} catch (CloneNotSupportedException e) {
            // Won't happen because we implement Cloneable
            throw new Error(e.toString());
		}
		return resObj;
	}
}