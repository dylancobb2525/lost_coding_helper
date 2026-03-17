package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Attachment {

    private UUID id;
    private UUID questionId;
    private String filename;
    private FileType fileType;
    private String storageUrl;
    private LocalDateTime uploadedAt;

    public Attachment(UUID questionId, String filename, FileType fileType, String storageUrl) {
        this(UUID.randomUUID(), questionId, filename, fileType, storageUrl, LocalDateTime.now());
    }

    public Attachment(UUID id, UUID questionId, String filename, FileType fileType, String storageUrl, LocalDateTime uploadedAt) {
        if (id != null) {
            this.id = id; 
        } else {
            this.id = UUID.randomUUID();
        }
        this.questionId = questionId;
        this.filename = filename;
        this.fileType = fileType;
        this.storageUrl = storageUrl;
        this.uploadedAt = (uploadedAt != null) ? uploadedAt : LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
