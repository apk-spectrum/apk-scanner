package com.apkscanner.gui.tabpanels;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class FileNodeDataTest {

    @Test
    void testConstructorWithFile() {
        File file = new File("testFile.txt");
        FileNodeData fileNodeData = new FileNodeData(file);

        assertEquals(file.getName(), fileNodeData.getLabel());
        assertEquals(file.getPath(), fileNodeData.getPath());
        assertEquals(file.isDirectory(), fileNodeData.isFolder());
        assertEquals(file, fileNodeData.getFile());
    }

    @Test
    void testGetURI() {
        File file = new File("testFile.txt");
        FileNodeData fileNodeData = new FileNodeData(file);

        assertEquals(file.toURI(), fileNodeData.getURI());
    }

    @Test
    void testGetPath() {
        File file = new File("testFile.txt");
        FileNodeData fileNodeData = new FileNodeData(file);

        assertEquals(file.getPath(), fileNodeData.getPath());
    }
}