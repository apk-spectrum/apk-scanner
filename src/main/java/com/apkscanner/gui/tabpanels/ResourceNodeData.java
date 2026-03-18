package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.apkspectrum.tool.ImgExtractorWrapper;
import com.apkspectrum.tool.aapt.AaptNativeWrapper;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.JarURI;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.ZipFileUtil;

public class ResourceNodeData extends DefaultNodeData {
    protected String config;
    protected ResourceType type;
    protected String symbolic;

    private JarURI jarURI;

    public ResourceNodeData(JarURI jarURI) {
        super(jarURI.getEntryName(), jarURI.getEntryPath(), jarURI.isDirectory());
        this.jarURI = jarURI;
        initValue();
    }

    public ResourceNodeData(String label, String config, JarURI jarURI) {
        super(label, jarURI.getEntryPath(), jarURI.isDirectory());
        this.jarURI = jarURI;
        initValue();
        this.config = config;
    }

    private void initValue() {
        String path = getPath();
        type = ResourceType.getType(path);

        if (type.getInt() <= ResourceType.XML.getInt()
                && path.startsWith("res/" + type.toString() + "-")) {
            config = path.replaceAll("res/" + type.toString() + "-([^/]*)/.*", "$1");
        } else {
            config = null;
        }
    }

    public ResourceType getResourceType() {
        return type;
    }

    @Override
    public URI getURI() {
        return jarURI.toURI();
    }

    @Override
    public URL getURL() {
        try {
            return jarURI.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        String str = null;
        int childCount = 0;
        if (getNode() != null && getNode().getParent() != null /* && !isFolder */
                && !".img".equals(getExtension()))
            childCount = getNode().getChildCount();

        if (childCount > 0) {
            str = getLabel() + " (" + childCount + ")";
        } else if (config != null && !config.isEmpty()) {
            str = getLabel() + " (" + config + ")";
        } else {
            str = getLabel();
        }
        return str;
    }

    public Object getData() {
        String path = getPath();
        if ((path.startsWith("res/") && !path.startsWith("res/raw/")
                && path.endsWith(".xml")) || path.equals("AndroidManifest.xml")
                || path.endsWith(".img")) {
            String apkPath = jarURI.getJarAbsolutePath();

            switch (getExtension()) {
                case ".xml":
                    return AaptNativeWrapper.Dump.getXmltree(apkPath, new String[] {path});
                case ".img":
                    String tempWorkPath = FileUtil
                            .makeTempPath(apkPath.substring(apkPath.lastIndexOf(File.separator)));
                    String imgPath =
                            tempWorkPath + File.separator + path.replace("/", File.separator);
                    if (!new File(imgPath).exists()) {
                        ZipFileUtil.unZip(apkPath, path, imgPath);
                    }
                    String data = null;
                    if (SystemUtil.isWindows()) {
                        data = ImgExtractorWrapper.getSuperblockInfo(imgPath);
                        data += ImgExtractorWrapper.getLsInfo(imgPath);
                    } else {
                        data = "Not Supported in " + SystemUtil.OS;
                    }
                    return data;
            }
        }

        return super.getData();
    }
}
