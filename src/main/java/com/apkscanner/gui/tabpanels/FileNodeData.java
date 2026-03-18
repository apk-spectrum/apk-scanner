package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.net.URI;

public class FileNodeData extends DefaultNodeData {
    private File file;

    public FileNodeData(File file) {
        super(file.getName(), file.getPath(), file.isDirectory());
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public URI getURI() {
        return getFile().toURI();
    }

    @Override
    public String getPath() {
        return getFile() != null ? getFile().getPath() : super.getPath();
    }
}
