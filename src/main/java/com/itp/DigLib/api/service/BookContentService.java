package com.itp.DigLib.api.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itp.DigLib.api.model.PagedContent;


@Service
public class BookContentService {
    private static final Logger logger = LoggerFactory.getLogger(BookContentService.class);
    
    private final Path uploadDir;
    private final int defaultPageSize;

    public BookContentService(
            @Value("${book.upload.dir:/app/bookcontents}") String uploadDirPath,
            @Value("${book.page.size:1000}") int pageSize
    ) {
        this.defaultPageSize = pageSize;
        
        try {
            this.uploadDir = Paths.get(uploadDirPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("Created upload directory at: {}", uploadDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", e.getMessage());
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String storeFile(MultipartFile file, String filename) throws IOException {
        Path filePath = this.uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }

    public PagedContent readBookContent(String filename, int pageNumber, Integer pageSize) throws IOException {
        Path filePath = this.uploadDir.resolve(filename);
        int charactersPerPage = pageSize != null ? pageSize : defaultPageSize;
        
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
            long fileLength = file.length();
            long totalPages = (fileLength + charactersPerPage - 1) / charactersPerPage;
            
            if (pageNumber < 0 || pageNumber >= totalPages) {
                throw new IllegalArgumentException("Invalid page number");
            }
            
            long startPosition = (long) pageNumber * charactersPerPage;
            int length = (int) Math.min(charactersPerPage, fileLength - startPosition);
            
            file.seek(startPosition);
            byte[] bytes = new byte[length];
            file.read(bytes);
            
            return new PagedContent(
                new String(bytes),
                pageNumber,
                (int) totalPages,
                length,
                (int) fileLength
            );
        }
    }

    public boolean deleteBookContent(String filename) {
        try {
            Path filePath = this.uploadDir.resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Failed to delete file {}: {}", filename, e.getMessage());
            return false;
        }

        
    }
    
}