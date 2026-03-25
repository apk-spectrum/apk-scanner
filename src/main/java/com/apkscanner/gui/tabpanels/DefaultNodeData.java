package com.apkscanner.gui.tabpanels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.apkspectrum.core.signer.SignatureReport;
import com.apkspectrum.resource.DefaultResImage;
import com.apkspectrum.swing.ImageScaler;
import com.apkspectrum.swing.TreeNodeImageObserver;
import com.apkspectrum.util.FileUtil;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultNodeData implements TreeNodeData, Cloneable {
    private String label;
    private String path;
    private boolean isFolder;
    private int dataType;

    private transient boolean isLoading;
    private transient Icon icon;
    private transient DefaultMutableTreeNode node;

    protected static final ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

    private static final Queue<DefaultNodeData> loadingQueue =
            new ConcurrentLinkedQueue<>();
    private static transient boolean isIconLoading;
    private transient Icon loadedIcon;

    public DefaultNodeData(@NonNull String label) {
        this(label, null, false);
    }

    public DefaultNodeData(@NonNull String label, boolean isFolder) {
        this(label, null, isFolder);
    }

    public DefaultNodeData(@NonNull String label, String path) {
        this(label, path, path != null && path.endsWith("/"));
    }

    public DefaultNodeData(@NonNull String label, String path, boolean isFolder) {
        this.label = label;
        this.path = path;
        this.isFolder = isFolder;
        this.dataType = checkDataType();
    }

    protected int checkDataType() {
        switch (getExtension()) {
            case ".png":
            case ".jpg":
            case ".gif":
            case ".bmp":
            case ".webp":
                return DATA_TYPE_IMAGE;
            case ".rsa":
            case ".dsa":
            case ".ec":
            case ".der":
            case ".pem":
                return DATA_TYPE_CERTIFICATION;
            case ".xml":
            case ".txt":
            case ".mk":
            case ".html":
            case ".htm":
            case ".js":
            case ".css":
            case ".json":
            case ".props":
            case ".properties":
            case ".policy":
            case ".rc":
            case ".mf":
            case ".sf":
            case ".version":
            case ".default":
            case ".sql":
            case ".list":
            case ".ini":
            case ".inf":
            case ".pro":
            case ".dtd":
            case ".xsd":
            case ".svg":
            case ".csv":
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
    public URI getURI() {
        try {
            return getPath() != null ? URI.create(getPath()) : null;
        } catch (IllegalArgumentException e) {
            log.error("{}: class {}", e.getMessage(), this.getClass().getSimpleName());
        }
        return null;
    }

    public URL getURL() {
        URI uri = getURI();
        if (uri != null && uri.isAbsolute()) {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                log.error("{}: class {}", e.getMessage(), this.getClass().getSimpleName());
            }
        }
        return null;
    }

    @Override
    public Icon getIcon() {
        if (icon != null) return icon;

        if (dataType == DATA_TYPE_IMAGE && getURL() != null) {
            setLoadingState(true);
            setIcon(icons.getIcon(ResourceTreeIcons.LOADING_ICON));
            loadImage();
        } else if (getLoadingState()) {
            setIcon(icons.getIcon(ResourceTreeIcons.PROCESSING_ICON));
        } else if (isFolder()) {
            setIcon(icons.getIcon(ResourceTreeIcons.FOLDER_ICON));
        }
        if (icon == null) {
            setIcon(icons.getIcon(getExtension()));
        }
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    private void loadImage() {
        synchronized (loadingQueue) {
            loadingQueue.add(this);
            if (isIconLoading) return;
            isIconLoading = true;
        }
        new SwingWorker<Void, DefaultNodeData>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (true) {
                    DefaultNodeData nodeData = null;
                    synchronized (loadingQueue) {
                        nodeData = loadingQueue.poll();
                        if (nodeData == null) {
                            if (!loadingQueue.isEmpty()) {
                                log.warn("Won't happen because the queue wasn't add null object.");
                                continue;
                            }
                            isIconLoading = false;
                            break;
                        }
                    }

                    try {
                        URL url = nodeData.getURL();
                        if (url == null) continue;

                        ImageIcon icon = null;
                        if (".webp".equals(nodeData.getExtension())) {
                            icon = new ImageIcon(ImageIO.read(url), url.toExternalForm());
                        } else {
                            icon = new ImageIcon(url);
                        }
                        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                            nodeData.loadedIcon = icons.getIcon(nodeData.getExtension());
                        } else {
                            nodeData.loadedIcon = ImageScaler.getScaledImageIcon(icon, 32, 32);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        publish(nodeData);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<DefaultNodeData> chunks) {
                if (chunks != null) {
                    for (DefaultNodeData nodeData: chunks) {
                        if (nodeData != null) nodeData.setLoadingState(false);
                    }
                }
            }
        }.execute();
    }

    public Icon getIcon(final JTree tree) {
        Icon icon = getIcon();
        if (tree != null && getLoadingState() && icon instanceof ImageIcon) {
            ImageIcon image = (ImageIcon) icon;
            if (!(image.getImageObserver() instanceof TreeNodeImageObserver)) {
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

    public void setNode(DefaultMutableTreeNode node) {
        this.node = node;
    }

    public TreeNode getNode() {
        return node;
    }

    public void setLoadingState(boolean isLoading) {
        if (this.isLoading == isLoading) return;
        if (icon instanceof ImageIcon) {
            ImageIcon image = (ImageIcon) icon;
            if (image.getImageObserver() != null) {
                image.setImageObserver(null);
            }
        }
        if (!isLoading && loadedIcon != null) {
            setIcon(loadedIcon);
            loadedIcon = null;
        } else {
            setIcon(null);
        }
        this.isLoading = isLoading;
    }

    public boolean getLoadingState() {
        return isLoading;
    }

    public String getExtension() {
        String path = getPath();
        if (path == null) return "";
        return FileUtil.getSuffix(path).toLowerCase();
    }

    public String getFileName() {
        String path = getPath();
        if (path == null) return null;
        String separator = path.contains(File.separator) ? separator = File.separator : "/";
        return path.substring(path.lastIndexOf(separator) + 1);
    }

    public String getFolderName() {
        String path = getPath();
        if (path == null) return null;
        String separator = path.contains(File.separator) ? separator = File.separator : "/";
        if (!path.contains(separator)) return "";
        return path.substring(0, path.lastIndexOf(separator));
    }

    @Override
    public Object getData() {
        switch (dataType) {
            case DATA_TYPE_IMAGE:
                if (".webp".equals(getExtension())) {
                    try {
                        return ImageIO.read(getURL());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    return null;
                }
                return DefaultResImage.getImage(getURL());
            case DATA_TYPE_CERTIFICATION:
                try {
                    return new SignatureReport(getURL().openStream()).toString();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            case DATA_TYPE_TEXT:
            case DATA_TYPE_UNKNOWN:
            default:
                byte[] buffer = getBytes();
                return (dataType == DATA_TYPE_TEXT && buffer != null) ? new String(buffer) : buffer;
        }
    }

    private byte[] getBytes() {
        URL url = getURL();
        if (url == null) return null;

        byte[] buffer = null;
        try (InputStream is = url.openStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                os.write(data, 0, nRead);
            }
            os.flush();
            buffer = os.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return buffer;
    }

    @Override
    protected DefaultNodeData clone() {
        DefaultNodeData resObj;
        try {
            resObj = (DefaultNodeData) super.clone();
            resObj.node = null;
            resObj.icon = null;
            resObj.isLoading = false;
            resObj.loadedIcon = null;
        } catch (CloneNotSupportedException e) {
            // Won't happen because we implement Cloneable
            throw new Error(e.toString());
        }
        return resObj;
    }
}
