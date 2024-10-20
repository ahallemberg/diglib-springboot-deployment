package com.itp.DigLib.api.model;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PagedContentTest {

    @Test
    void testValidPagedContentCreation() {
        PagedContent content = new PagedContent(
            "Test content",
            0,
            5,
            20,
            100
        );

        assertEquals("Test content", content.getContent());
        assertEquals(0, content.getPageNumber());
        assertEquals(5, content.getTotalPages());
        assertEquals(20, content.getPageSize());
        assertEquals(100, content.getTotalSize());
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationScenarios")
    void testVariousPaginationScenarios(
            String content,
            int pageNumber,
            int totalPages,
            int pageSize,
            int totalSize
    ) {
        PagedContent pagedContent = new PagedContent(content, pageNumber, totalPages, pageSize, totalSize);

        assertEquals(content, pagedContent.getContent());
        assertEquals(pageNumber, pagedContent.getPageNumber());
        assertEquals(totalPages, pagedContent.getTotalPages());
        assertEquals(pageSize, pagedContent.getPageSize());
        assertEquals(totalSize, pagedContent.getTotalSize());
        
        // Verify pagination logic
        assertTrue(pagedContent.getPageNumber() < pagedContent.getTotalPages());
        assertTrue(pagedContent.getPageSize() <= pagedContent.getTotalSize());
        assertTrue(pagedContent.getTotalPages() >= 1);
    }

    private static Stream<Arguments> provideValidPaginationScenarios() {
        return Stream.of(
            // Single page scenario
            Arguments.of("Short content", 0, 1, 12, 12),
            
            // Multi-page scenario
            Arguments.of("Page 1 content", 0, 5, 20, 100),
            
            // Last page scenario (possibly shorter content)
            Arguments.of("Last", 4, 5, 4, 100),
            
            // Empty content scenario
            Arguments.of("", 0, 1, 0, 0),
            
            // Large book scenario
            Arguments.of("Chapter 1", 0, 100, 1000, 100000)
        );
    }

    @Test
    void testContentConsistency() {
        String expectedContent = "Test content";
        PagedContent content = new PagedContent(
            expectedContent,
            0,
            1,
            expectedContent.length(),
            expectedContent.length()
        );

        assertEquals(expectedContent.length(), content.getPageSize());
        assertEquals(expectedContent, content.getContent());
    }

    @Test
    void testPaginationBoundaries() {
        String content = "Test content";
        int totalSize = 100;
        int pageSize = 20;
        int expectedTotalPages = (totalSize + pageSize - 1) / pageSize; // Ceiling division

        PagedContent pagedContent = new PagedContent(
            content,
            0,
            expectedTotalPages,
            pageSize,
            totalSize
        );

        // Verify total pages calculation is consistent with size
        assertEquals(expectedTotalPages, pagedContent.getTotalPages());
        assertTrue(pagedContent.getTotalPages() * pagedContent.getPageSize() >= pagedContent.getTotalSize());
    }

    @Test
    void testEmptyContent() {
        PagedContent content = new PagedContent(
            "",
            0,
            1,
            0,
            0
        );

        assertEquals("", content.getContent());
        assertEquals(0, content.getPageSize());
        assertEquals(1, content.getTotalPages()); // Even empty content should have 1 page
    }
}