package com.apkscanner.gui.tabpanels;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.apkscanner.resource.RImg;
import com.apkspectrum.core.signer.SignatureReport;
import com.apkspectrum.swing.ImageScaler;
import com.apkspectrum.swing.TreeNodeImageObserver;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.URITool;

public class DefaultNodeData implements TreeNodeData, Cloneable
{
	protected String label;
	protected URI uri;
	protected String path;
	protected boolean isFolder;
	protected int dataType;

	transient protected boolean isLoading;
	transient protected Icon icon;
	transient protected DefaultMutableTreeNode node;

	public DefaultNodeData(String label) {
		this(label, (URI) null);
	}

	public DefaultNodeData(URI uri) {
		this(null, uri);
	}

	public DefaultNodeData(String label, URI uri) {
		this(label, uri, uri != null && uri.toString().endsWith("/"));
	}

	public DefaultNodeData(String label, boolean isFolder) {
		this(label, (URI) null, isFolder);
	}

	public DefaultNodeData(URI uri, boolean isFolder) {
		this(null, uri, isFolder);
	}

	public DefaultNodeData(String label, URI uri, boolean isFolder) {
		this.uri = uri;
		String path = null;
		if(uri != null) {
			path = URITool.getJarEntryPath(uri);
			if(path == null) path = uri.getPath();
		}
		if(path != null && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		this.path = path;
		this.label = label != null ? label : getFileName();
		this.isFolder = isFolder;
		this.dataType = checkDataType();
	}

	public DefaultNodeData(File file) {
		this.uri = file.toURI();
		this.path = file.getAbsolutePath();
		this.label = file.getName();
		this.isFolder = file.isDirectory();
		this.dataType = checkDataType();
	}

	private int checkDataType() {
		String extension = getExtension();
		switch(extension.toLowerCase()) {
		case ".png": case ".jpg": case ".gif": case ".bmp": case ".webp":
			return DATA_TYPE_IMAGE;
		case ".rsa": case ".dsa": case ".ec": case ".der":
			return DATA_TYPE_CERTIFICATION;
		case ".xml": case ".txt": case ".mk": case ".html": case ".htm": case ".js": case ".css":  case ".json":
		case ".props": case ".properties":  case ".policy": case ".rc": case ".mf": case ".sf":
		case ".version": case ".default": case ".sql": case ".list": case ".ini": case ".inf":
		case ".pro": case ".dtd": case ".xsd": case ".svg": case ".pem": case ".csv":
			return DATA_TYPE_TEXT;
		default:
			return DATA_TYPE_UNKNOWN;
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	public URL getURL() {
		if(uri == null) return null;
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isFolder() {
		return isFolder;
	}

	@Override
	public int getDataType() {
		return dataType;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public Icon getIcon() {
		if(icon != null) return icon;

		if(getLoadingState()) {
			icon = RImg.RESOURCE_TREE_OPEN_JD.getImageIcon();
		} else if(isFolder) {
			icon = SystemUtil.getExtensionIcon(SystemUtil.FOLDER_ICON);
		}
		if(dataType == DATA_TYPE_IMAGE) {
			try {
				URL url = getURL();
				if(url != null) {
					Image tempImage = null;
					if(path.toLowerCase().endsWith(".webp")) {
						tempImage = ImageScaler.getScaledImage(new ImageIcon(ImageIO.read(url)), 32, 32);
					} else {
						tempImage = ImageScaler.getScaledImage(new ImageIcon(url), 32, 32);
					}
					icon = new ImageIcon(tempImage);
					tempImage.flush();
				}
			} catch (IOException|NullPointerException e1) {
				//e1.printStackTrace();
			}
		}
		if(icon == null) {
			icon = SystemUtil.getExtensionIcon(getExtension());
		}

		return icon;
	}

	public Icon getIcon(final JTree tree) {
		Icon icon = getIcon();
		if(tree != null && getLoadingState() && icon instanceof ImageIcon) {
			ImageIcon image = (ImageIcon) icon;
			if(!(image.getImageObserver() instanceof TreeNodeImageObserver)) {
				image.setImageObserver(new TreeNodeImageObserver(tree, node, image) {
					@Override
					protected boolean isStop() {
						stopFlag = !getLoadingState();
						return super.isStop();
					}
				});
			}
		}
		return icon;
	}

	void setNode(DefaultMutableTreeNode node) {
		this.node = node;
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

	public String getExtension() {
		if(path == null) return "";
		return FileUtil.getSuffix(path);
	}

	public String getFileName() {
		if(path == null) return null;
		String separator = path.contains(File.separator) ? separator = File.separator : "/";
		return path.substring(path.lastIndexOf(separator) + 1, path.length());
	}

	public String getFolderName() {
		if(path == null) return null;
		String separator = path.contains(File.separator) ? separator = File.separator : "/";
		if(!path.contains(separator)) return path;
		return path.substring(0, path.lastIndexOf(separator));
	}

	public byte[] getBytes() {
		if(uri == null || uri.getScheme() == null) return null;

		byte[] buffer = null;
		try(InputStream is = uri.toURL().openStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				os.write(data, 0, nRead);
		    }
			os.flush();
			buffer = os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	@Override
	public Object getData() {
		switch(dataType) {
		case DATA_TYPE_IMAGE:
			if(getPath().toLowerCase().endsWith(".webp")) {
				try {
					return ImageIO.read(getURL());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			} else {
				return RImg.getImage(getURL());
			}
		case DATA_TYPE_CERTIFICATION:
			try {
				return new SignatureReport(getURL().openStream()).toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		case DATA_TYPE_TEXT:
		case DATA_TYPE_UNKNOWN:
		default:
			byte[] buffer = getBytes();
			return (dataType == DATA_TYPE_TEXT && buffer != null)
					? new String(buffer) : buffer;
		}
	}

	@Override
	protected DefaultNodeData clone() {
		DefaultNodeData resObj;
		try {
			resObj = (DefaultNodeData) super.clone();
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