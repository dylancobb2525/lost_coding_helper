package com.model;

import java.util.UUID;
import java.time.LocalDateTime;

public class Attachment {

    private UUID id;
    private UUID questionId;
    private String filename;
    private FileType fileType;
    private String storageUrl;
    private LocalDateTime uploadedAt;

}
