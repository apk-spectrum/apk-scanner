package com.apkscanner.gui.tabpanels;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Image;
import java.io.File;
import java.time.Duration;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultNodeDataTest {

    @Test
    void testConstructorWithLabel() {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label);

        assertEquals(label, nodeData.getLabel());
        assertNull(nodeData.getPath());
        assertFalse(nodeData.isFolder());
        assertEquals(TreeNodeData.DATA_TYPE_UNKNOWN, nodeData.getDataType());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testConstructorWithLabelAndIsFolder(boolean isFolder) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, isFolder);

        assertEquals(label, nodeData.getLabel());
        assertNull(nodeData.getPath());
        assertEquals(isFolder, nodeData.isFolder());
        assertEquals(TreeNodeData.DATA_TYPE_UNKNOWN, nodeData.getDataType());
    }

    static Stream<Arguments> provideConstructorWithLabelAndPathArguments() {
        return Stream.of(
            Arguments.of("testPath", false),
            Arguments.of("testPath/", true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideConstructorWithLabelAndPathArguments")
    void testConstructorWithLabelAndPath(String path, boolean expectedIsFolder) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(label, nodeData.getLabel());
        assertEquals(path, nodeData.getPath());
        assertEquals(expectedIsFolder, nodeData.isFolder());
        assertEquals(TreeNodeData.DATA_TYPE_UNKNOWN, nodeData.getDataType());
    }

    static Stream<Arguments> provideConstructorWithLabelPathAndIsFolderArguments() {
        return Stream.of(
            Arguments.of("testPath", false),
            Arguments.of("testPath", true),
            Arguments.of("testPath/", false),
            Arguments.of("testPath/", true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideConstructorWithLabelPathAndIsFolderArguments")
    void testConstructorWithLabelPathAndIsFolder(String path, boolean isFolder) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path, isFolder);

        assertEquals(label, nodeData.getLabel());
        assertEquals(path, nodeData.getPath());
        assertEquals(isFolder, nodeData.isFolder());
        assertEquals(TreeNodeData.DATA_TYPE_UNKNOWN, nodeData.getDataType());
    }

    static Stream<Arguments> provideCheckDataTypeArguments() {
        return Stream.of(
            Arguments.of("test.png", DefaultNodeData.DATA_TYPE_IMAGE),
            Arguments.of("test.txt", DefaultNodeData.DATA_TYPE_TEXT),
            Arguments.of("test.rsa", DefaultNodeData.DATA_TYPE_CERTIFICATION),
            Arguments.of("test.unknown", DefaultNodeData.DATA_TYPE_UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCheckDataTypeArguments")
    void testCheckDataType(String fileName, int expectedDataType) {
        DefaultNodeData nodeData = new DefaultNodeData("testLabel", fileName);
        assertEquals(expectedDataType, nodeData.getDataType());
    }

    @Test
    void testToString() {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label);

        assertEquals(label, nodeData.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"jar:file:/test/path!/", "file:/test/path", "/test/path", "path"})
    void testGetURI(String path) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertNotNull(nodeData.getURI());
        assertEquals(path, nodeData.getURI().toString());
    }

    static Stream<Arguments> provideConstructorWithPathAndIsValidArguments() {
        return Stream.of(
            Arguments.of("jar:file:/test/path!/", true),
            Arguments.of("file:/test/path", true),
            Arguments.of("/test/path", false),
            Arguments.of("path", false),
            Arguments.of("jar:/test/path!/", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideConstructorWithPathAndIsValidArguments")
    void testGetURL(String path, boolean isValid) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        if (isValid) {
            assertNotNull(nodeData.getURL());
            assertEquals(path, nodeData.getURL().toString());
        } else {
            assertNull(nodeData.getURL());
        }
    }

    @Test
    void testGetIconWithSetIcon() {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label);

        Icon initialIcon = nodeData.getIcon();

        Icon icon = new ImageIcon();
        nodeData.setIcon(icon);

        assertNotNull(initialIcon);
        assertEquals(icon, nodeData.getIcon());
        assertNotEquals(initialIcon, nodeData.getIcon());
    }

    static Stream<Arguments> provideGetIconForImageWithPathArguments() {
        return Stream.of(
            Arguments.of(DefaultNodeDataTest.class.getResource("/test.webp").toString(), true),
            Arguments.of(DefaultNodeDataTest.class.getResource("/test.png").toString(), true),
            Arguments.of(new File("test.png").toURI().toString(), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetIconForImageWithPathArguments")
    void testGetIconForImage(String path, boolean isValid) {
        String label = "testImage";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        com.luciad.imageio.webp.internal.NativeLoader.initialize();

        ImageIcon image = new ImageIcon(path);

        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();
        ImageIcon loadingIcon = (ImageIcon) icons.getIcon(ResourceTreeIcons.LOADING_ICON);
        ImageIcon fileIcon = (ImageIcon) icons.getIcon(nodeData.getExtension());

        ImageIcon icon = (ImageIcon) nodeData.getIcon();

        assertNotNull(icon);
        assertTrue(nodeData.getLoadingState());
        assertEquals(loadingIcon.getDescription(), icon.getDescription());

        assertTimeout(Duration.ofMillis(1000), () -> {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            } while (nodeData.getLoadingState());
        });
        assertFalse(nodeData.getLoadingState());

        if (isValid) {
            assertEquals(image.getDescription(), ((ImageIcon) nodeData.getIcon()).getDescription());
        } else {
            assertEquals(fileIcon.getDescription(), ((ImageIcon) nodeData.getIcon()).getDescription());
        }
    }

    @Test
    void testGetIconForLoadingState() {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label);

        assertFalse(nodeData.getLoadingState());

        nodeData.setLoadingState(true);

        assertTrue(nodeData.getLoadingState());

        ImageIcon icon = (ImageIcon) nodeData.getIcon();

        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();
        ImageIcon processingIcon = (ImageIcon) icons.getIcon(ResourceTreeIcons.PROCESSING_ICON);

        assertNotNull(icon);
        assertEquals(processingIcon.getDescription(), icon.getDescription());
    }

    @Test
    void testGetIconForFolder() {
        String label = "testFolder";
        DefaultNodeData nodeData = new DefaultNodeData(label, true);

        Icon icon = nodeData.getIcon();
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();
        Icon folderIcon = icons.getIcon(ResourceTreeIcons.FOLDER_ICON);

        assertNotNull(icon);
        assertEquals(folderIcon, icon);
    }

    @Test
    void testGetIconForText() {
        String label = "testText";
        String path = "test.txt";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        Icon icon = nodeData.getIcon();
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();
        Icon textIcon = icons.getIcon(".txt");

        assertNotNull(icon);
        assertEquals(textIcon, icon);
    }

    static Stream<Arguments> provideGetExtensionWithPathArguments() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("txt", ""),
            Arguments.of(".txt", ".txt"),
            Arguments.of("./.txt", ".txt"),
            Arguments.of("file.txt", ".txt"),
            Arguments.of("/path/.to/filetxt", ""),
            Arguments.of("/path/to/file.test.txt", ".txt")
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetExtensionWithPathArguments")
    void testGetExtension(String path, String expected) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(expected, nodeData.getExtension());
    }

    static Stream<Arguments> provideGetFileNameWithPathArguments() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("txt", "txt"),
            Arguments.of(".txt", ".txt"),
            Arguments.of("./.txt", ".txt"),
            Arguments.of("file.txt", "file.txt"),
            Arguments.of("/path/.to/filetxt", "filetxt"),
            Arguments.of("/path/to/file.test.txt", "file.test.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetFileNameWithPathArguments")
    void testGetFileName(String path, String expected) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(expected, nodeData.getFileName());
    }

    static Stream<Arguments> provideGetFolderNameWithPathArguments() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("txt", ""),
            Arguments.of(".txt", ""),
            Arguments.of("./.txt", "."),
            Arguments.of("file.txt", ""),
            Arguments.of("/path/.to/filetxt", "/path/.to"),
            Arguments.of("/path/to/file.test.txt", "/path/to")
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetFolderNameWithPathArguments")
    void testGetFolderName(String path, String expected) {
        String label = "testLabel";
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(expected, nodeData.getFolderName());
    }

    @Test
    void testClone() {
        String label = "testLabel";
        String path = "testPath";
        boolean isFolder = true;
        DefaultNodeData originalNodeData = new DefaultNodeData(label, path, isFolder);

        DefaultNodeData clonedNodeData = originalNodeData.clone();

        assertNotSame(originalNodeData, clonedNodeData);
        assertEquals(originalNodeData.getLabel(), clonedNodeData.getLabel());
        assertEquals(originalNodeData.getPath(), clonedNodeData.getPath());
        assertEquals(originalNodeData.isFolder(), clonedNodeData.isFolder());
        assertEquals(originalNodeData.getDataType(), clonedNodeData.getDataType());
        assertNull(clonedNodeData.getNode());
        assertFalse(clonedNodeData.getLoadingState());
    }

    static Stream<Arguments> provideGetDataForImageArguments() {
        return Stream.of(
            Arguments.of("test.png", DefaultNodeData.DATA_TYPE_IMAGE),
            Arguments.of("test.webp", DefaultNodeData.DATA_TYPE_IMAGE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGetDataForImageArguments")
    void testGetDataForImage(String fileName, int dataType) {
        String label = "testImage";
        String path = DefaultNodeDataTest.class.getResource("/" + fileName).toString();
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(dataType, nodeData.getDataType());
        assertTrue(nodeData.getData() instanceof Image);
    }

    @Test
    void testGetDataForCertification() {
        String label = "testCert";
        String path = DefaultNodeDataTest.class.getResource("/platform.x509.pem").toString();
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(DefaultNodeData.DATA_TYPE_CERTIFICATION, nodeData.getDataType());

        Object data = nodeData.getData();
        assertNotNull(data);
        assertTrue(data instanceof String);
        assertTrue(((String)data).startsWith("Owner:"));
    }

    @Test
    void testGetDataForText() {
        String label = "testText";
        String path = DefaultNodeDataTest.class.getResource("/test.txt").toString();
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(DefaultNodeData.DATA_TYPE_TEXT, nodeData.getDataType());

        Object data = nodeData.getData();
        assertNotNull(data);
        assertTrue(data instanceof String);
    }

    @Test
    void testGetDataForUnknown() {
        String label = "testUnknown";
        String path = DefaultNodeDataTest.class.getResource("/test.unknown").toString();
        DefaultNodeData nodeData = new DefaultNodeData(label, path);

        assertEquals(DefaultNodeData.DATA_TYPE_UNKNOWN, nodeData.getDataType());

        Object data = nodeData.getData();
        assertNotNull(data);
        assertTrue(data instanceof byte[]);
    }
}
