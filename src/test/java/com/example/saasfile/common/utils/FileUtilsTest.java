package com.example.saasfile.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @Test
    void formatsCommonFileSizes() {
        assertEquals("0 B", FileUtils.formatFileSize(0));
        assertEquals("1 KB", FileUtils.formatFileSize(1024));
        assertEquals("1 MB", FileUtils.formatFileSize(1024 * 1024));
        assertEquals("1.5 KB", FileUtils.formatFileSize(1536));
    }

    @Test
    void identifiesSupportedKnowledgeDocumentExtensions() {
        assertTrue(FileUtils.isKnowledgeDocumentFile("guide.txt"));
        assertTrue(FileUtils.isKnowledgeDocumentFile("manual.doc"));
        assertTrue(FileUtils.isKnowledgeDocumentFile("manual.docx"));
        assertTrue(FileUtils.isKnowledgeDocumentFile("report.pdf"));

        assertFalse(FileUtils.isKnowledgeDocumentFile(null));
        assertFalse(FileUtils.isKnowledgeDocumentFile("image.png"));
        assertFalse(FileUtils.isKnowledgeDocumentFile("REPORT.PDF"));
    }

    @Test
    void extractsFileNamesFromObjectKeys() {
        assertEquals("file.pdf", FileUtils.getFileNameFromObjectKey("tenant/a/file.pdf"));
        assertEquals("file.pdf", FileUtils.getOriginalFileNameFromObjectKey("tenant/a/123_file.pdf"));
        assertEquals("file.pdf", FileUtils.getOriginalFileNameFromObjectKey("file.pdf"));
        assertNull(FileUtils.getFileNameFromObjectKey(null));
        assertEquals("", FileUtils.getOriginalFileNameFromObjectKey(""));
    }
}
