package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata for a file attached to a question (name, type, storage location, upload time).
 *
 * @author Christopher Feuchter
 */
public class Attachment {

    private UUID id;
    private UUID questionId;
    private String filename;
    private FileType fileType;
    private String storageUrl;
    private LocalDateTime uploadedAt;

    /**
     * Creates an attachment with a new id and {@link LocalDateTime#now()} as upload time.
     *
     * @param questionId owning question
     * @param filename display or stored name
     * @param fileType file category
     * @param storageUrl where the bytes live
     */
    public Attachment(UUID questionId, String filename, FileType fileType, String storageUrl) {
        this(UUID.randomUUID(), questionId, filename, fileType, storageUrl, LocalDateTime.now());
    }

    /**
     * Creates an attachment with explicit id and timestamp.
     *
     * @param id attachment id; if {@code null}, a random id is assigned
     * @param questionId owning question
     * @param filename display or stored name
     * @param fileType file category
     * @param storageUrl where the bytes live
     * @param uploadedAt upload time; if {@code null}, {@link LocalDateTime#now()} is used
     */
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

    /**
     * @return attachment id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return owning question id
     */
    public UUID getQuestionId() {
        return questionId;
    }

    /**
     * @param questionId owning question id
     */
    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    /**
     * @return file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename file name
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return file type
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * @param fileType file type
     */
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    /**
     * @return storage URL or path
     */
    public String getStorageUrl() {
        return storageUrl;
    }

    /**
     * @param storageUrl storage URL or path
     */
    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    /**
     * @return upload timestamp
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * @param uploadedAt upload timestamp
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
