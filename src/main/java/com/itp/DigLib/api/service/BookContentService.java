package com.itp.DigLib.api.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.itp.DigLib.api.model.PagedContent;

@Service
public class BookContentService {
    private static final Logger logger = LoggerFactory.getLogger(BookContentService.class);
    
    private final Storage storage;
    private final String bucketName;
    private final int defaultPageSize;

    public BookContentService(
            @Value("${gcp.bucket.name}") String bucketName,
            @Value("${book.page.size:1000}") int pageSize
    ) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
        this.defaultPageSize = pageSize;
        logger.info("Initialized cloud storage with bucket: {}", bucketName);
    }

    public String storeFile(MultipartFile file, String filename) throws IOException {
        BlobId blobId = BlobId.of(bucketName, "books/" + filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        try {
            storage.create(blobInfo, file.getBytes());
            logger.info("Successfully stored file in cloud storage: {}", filename);
            return filename;
        } catch (IOException e) {
            logger.error("Failed to store file in cloud storage: {}", e.getMessage());
            throw new IOException("Failed to store file in cloud storage", e);
        }
    }

    public PagedContent readBookContent(String filename, int pageNumber, Integer pageSize) throws IOException {
        BlobId blobId = BlobId.of(bucketName, "books/" + filename);
        Blob blob = storage.get(blobId);
        
        if (blob == null) {
            logger.error("File not found in cloud storage: {}", filename);
            throw new IOException("File not found in cloud storage");
        }

        byte[] content = blob.getContent();
        int charactersPerPage = pageSize != null ? pageSize : defaultPageSize;
        int totalSize = content.length;
        int totalPages = (totalSize + charactersPerPage - 1) / charactersPerPage;

        if (pageNumber < 0 || pageNumber >= totalPages) {
            throw new IllegalArgumentException("Invalid page number");
        }

        int startPosition = pageNumber * charactersPerPage;
        int length = Math.min(charactersPerPage, totalSize - startPosition);
        
        byte[] pageContent = new byte[length];
        System.arraycopy(content, startPosition, pageContent, 0, length);

        return new PagedContent(
            new String(pageContent),
            pageNumber,
            totalPages,
            length,
            totalSize
        );
    }

    public boolean deleteBookContent(String filename) {
        try {
            BlobId blobId = BlobId.of(bucketName, "books/" + filename);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                logger.info("Successfully deleted file from cloud storage: {}", filename);
            } else {
                logger.warn("File not found in cloud storage: {}", filename);
            }
            return deleted;
        } catch (Exception e) {
            logger.error("Failed to delete file from cloud storage: {}", e.getMessage());
            return false;
        }
    }
}