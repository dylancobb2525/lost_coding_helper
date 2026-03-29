package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class AttachmentTest {

    private UUID id;
    private UUID questionId;
    private LocalDateTime uploadedAt;

    @Before
    public void setUp() {
        id = UUID.randomUUID();
        questionId = UUID.randomUUID();
        uploadedAt = LocalDateTime.of(2025, 1, 1, 12, 0);
    }

    @Test
    public void shortConstructor_createsIdAndUploadTime() {
        Attachment attachment = new Attachment(questionId, "notes.pdf", null, "/files/notes.pdf");

        assertNotNull(attachment.getId());
        assertNotNull(attachment.getUploadedAt());
        assertEquals(questionId, attachment.getQuestionId());
        assertEquals("notes.pdf", attachment.getFilename());
        assertEquals("/files/notes.pdf", attachment.getStorageUrl());
    }

    @Test
    public void fullConstructor_keepsProvidedIdAndTimestamp() {
        Attachment attachment = new Attachment(id, questionId, "notes.pdf", null, "/files/notes.pdf", uploadedAt);

        assertEquals(id, attachment.getId());
        assertEquals(uploadedAt, attachment.getUploadedAt());
    }

    @Test
    public void fullConstructor_createsIdWhenNullPassed() {
        Attachment attachment = new Attachment(null, questionId, "notes.pdf", null, "/files/notes.pdf", uploadedAt);

        assertNotNull(attachment.getId());
    }

    @Test
    public void fullConstructor_usesCurrentTimeWhenUploadedAtNull() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Attachment attachment = new Attachment(id, questionId, "notes.pdf", null, "/files/notes.pdf", null);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(attachment.getUploadedAt());
        assertTrue(!attachment.getUploadedAt().isBefore(before));
        assertTrue(!attachment.getUploadedAt().isAfter(after));
    }

    /*
     * This class tests Attachment constructor behavior. We check automatic id creation,
     * automatic upload time creation, and that provided values stay the same when passed in.
     */
}