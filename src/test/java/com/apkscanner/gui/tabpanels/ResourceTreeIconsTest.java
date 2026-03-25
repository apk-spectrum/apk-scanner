package com.apkscanner.gui.tabpanels;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;

import javax.print.attribute.UnmodifiableSetException;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.apkscanner.resource.RImg;

class ResourceTreeIconsTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance before each test
        Field instanceField = ResourceTreeIcons.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void testGetDefaultSet() {
        // Given
        ResourceTreeIcons firstInstance = ResourceTreeIcons.getDefaultSet();
        ResourceTreeIcons secondInstance = ResourceTreeIcons.getDefaultSet();

        // When & Then
        assertNotNull(firstInstance);
        assertSame(firstInstance, secondInstance, "getDefaultSet should return the same instance");
    }

    @Test
    void testSetIconWithDefaultInstance() {
        // Given
        ResourceTreeIcons defaultInstance = ResourceTreeIcons.getDefaultSet();

        // When & Then
        assertThrows(UnmodifiableSetException.class, () -> {
            defaultInstance.setIcon(".test", RImg.TREE_FOLDER.getImageIcon());
        }, "setIcon should throw UnmodifiableSetException for default instance");
    }

    @Test
    void testSetIconWithCustomInstance() {
        // Given
        ResourceTreeIcons customInstance = new ResourceTreeIcons();
        Icon testIcon = RImg.TREE_FOLDER.getImageIcon();

        // When & Then
        assertDoesNotThrow(() -> {
            customInstance.setIcon(".test", testIcon);
        }, "setIcon should not throw exception for custom instance");

        Icon retrievedIcon = customInstance.getIcon(".test");
        assertSame(testIcon, retrievedIcon, "getIcon should return the set icon");
    }

    @Test
    void testGetIconForFolder() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon folderIcon = icons.getIcon(ResourceTreeIcons.FOLDER_ICON);
        ImageIcon expectedIcon = RImg.TREE_FOLDER.getImageIcon();

        // Then
        assertNotNull(folderIcon, "Folder icon should not be null");
        assertTrue(folderIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, folderIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) folderIcon).getDescription());
    }

    @Test
    void testGetIconForLoading() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon loadingIcon = icons.getIcon(ResourceTreeIcons.LOADING_ICON);
        ImageIcon expectedIcon = RImg.TREE_LOADING.getImageIcon();

        // Then
        assertNotNull(loadingIcon, "Loading icon should not be null");
        assertTrue(loadingIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, loadingIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) loadingIcon).getDescription());
    }

    @Test
    void testGetIconForProcessing() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon processingIcon = icons.getIcon(ResourceTreeIcons.PROCESSING_ICON);
        ImageIcon expectedIcon = RImg.RESOURCE_TREE_OPEN_JD.getImageIcon();

        // Then
        assertNotNull(processingIcon, "Processing icon should not be null");
        assertTrue(processingIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, processingIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) processingIcon).getDescription());
    }

    @Test
    void testGetIconForXml() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon xmlIcon = icons.getIcon(".xml");
        ImageIcon expectedIcon = RImg.RESOURCE_TREE_XML.getImageIcon();

        // Then
        assertNotNull(xmlIcon, "XML icon should not be null");
        assertTrue(xmlIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, xmlIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) xmlIcon).getDescription());
    }

    @Test
    void testGetIconForDex() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon dexIcon = icons.getIcon(".dex");
        ImageIcon expectedIcon = RImg.RESOURCE_TREE_CODE.getImageIcon();

        // Then
        assertNotNull(dexIcon, "DEX icon should not be null");
        assertTrue(dexIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, dexIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) dexIcon).getDescription());
    }

    @Test
    void testGetIconForArsc() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon arscIcon = icons.getIcon(".arsc");
        ImageIcon expectedIcon = RImg.RESOURCE_TREE_ARSC.getImageIcon();

        // Then
        assertNotNull(arscIcon, "ARSC icon should not be null");
        assertTrue(arscIcon instanceof ImageIcon);
        assertNotSame(expectedIcon, arscIcon);
        assertEquals(expectedIcon.getDescription(), ((ImageIcon) arscIcon).getDescription());
    }

    @Test
    void testGetIconCaching() throws Exception {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();
        Field cacheField = ResourceTreeIcons.class.getDeclaredField("cacheIcon");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, Icon> cache = (HashMap<String, Icon>) cacheField.get(icons);

        // When
        Icon xmlIcon1 = icons.getIcon(".xml");
        int cacheSizeBefore = cache.size();

        Icon xmlIcon2 = icons.getIcon(".xml");
        int cacheSizeAfter = cache.size();

        // Then
        assertSame(xmlIcon1, xmlIcon2, "Same icon should be returned for the same suffix");
        assertEquals(cacheSizeBefore, cacheSizeAfter, "Cache size should not change after second call");
        assertTrue(cache.containsKey(".xml"), "Cache should contain the key");
    }

    @Test
    void testGetIconForUnknownSuffix() {
        // Given
        ResourceTreeIcons icons = ResourceTreeIcons.getDefaultSet();

        // When
        Icon unknownIcon = icons.getIcon(".unknown");

        // Then - Icon may be not null depending on system
        assertNotNull(unknownIcon);
    }
}