package com.apkscanner.gui.tabpanels;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserNodeDataTest {
    @Test
    void testConstructorWithThreeParameters() {
        String label = "testLabel";
        String path = "testPath";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, data);

        assertEquals(label, userNodeData.getLabel());
        assertEquals(path, userNodeData.getPath());
        assertNull(userNodeData.config);
        assertEquals(data, userNodeData.getData());
        assertEquals(".xml", userNodeData.getExtension());
        assertEquals(DefaultNodeData.DATA_TYPE_TEXT, userNodeData.getDataType());
    }

    @Test
    void testConstructorWithFourParameters() {
        String label = "testLabel";
        String path = "testPath";
        String config = "testConfig";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, config, data);

        assertEquals(label, userNodeData.getLabel());
        assertEquals(path, userNodeData.getPath());
        assertEquals(config, userNodeData.config);
        assertEquals(data, userNodeData.getData());
        assertEquals(".xml", userNodeData.getExtension());
        assertEquals(DefaultNodeData.DATA_TYPE_TEXT, userNodeData.getDataType());
    }

    @Test
    void testSetData() {
        String label = "testLabel";
        String path = "testPath";
        Object initialData = new Object();
        Object newData = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, initialData);

        assertEquals(initialData, userNodeData.getData());

        userNodeData.setData(newData);

        assertEquals(newData, userNodeData.getData());
    }

    @Test
    void testGetExtension() {
        String label = "testLabel";
        String path = "testPath";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, data);

        assertEquals(".xml", userNodeData.getExtension());
    }

    @Test
    void testGetDataType() {
        String label = "testLabel";
        String path = "testPath";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, data);

        assertEquals(DefaultNodeData.DATA_TYPE_TEXT, userNodeData.getDataType());
    }

    @Test
    void testToStringWithoutConfig() {
        String label = "testLabel";
        String path = "testPath";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, data);

        assertEquals(label, userNodeData.toString());
    }

    @Test
    void testToStringWithConfig() {
        String label = "testLabel";
        String path = "testPath";
        String config = "testConfig";
        Object data = new Object();

        UserNodeData userNodeData = new UserNodeData(label, path, config, data);

        assertEquals(label + " (" + config + ")", userNodeData.toString());
    }
}