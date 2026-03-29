package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class CommentTest {

    private UUID id;
    private UUID authorId;
    private UUID questionId;
    private UUID solutionId;
    private LocalDateTime createdOn;
    private LocalDateTime updatedAt;

    @Before
    public void setUp() {
        id = UUID.randomUUID();
        authorId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        solutionId = UUID.randomUUID();
        createdOn = LocalDateTime.of(2025, 1, 1, 10, 0);
        updatedAt = LocalDateTime.of(2025, 1, 1, 11, 0);
    }

    @Test
    public void constructor_createsIdAndTimestamps() {
        Comment comment = new Comment(authorId, "Nice work", questionId, solutionId);

        assertNotNull(comment.getId());
        assertNotNull(comment.getCreatedOn());
        assertNotNull(comment.getUpdatedAt());
        assertEquals("Nice work", comment.getBody());
    }

    @Test
    public void fullConstructor_keepsProvidedTimes() {
        Comment comment = new Comment(id, authorId, "Nice work", createdOn, updatedAt, questionId, solutionId);

        assertEquals(createdOn, comment.getCreatedOn());
        assertEquals(updatedAt, comment.getUpdatedAt());
    }

    @Test
    public void fullConstructor_usesCurrentTimeWhenTimesAreNull() {
        Comment comment = new Comment(id, authorId, "Nice work", null, null, questionId, solutionId);

        assertNotNull(comment.getCreatedOn());
        assertNotNull(comment.getUpdatedAt());
    }

    @Test
    public void editComment_changesBody() {
        Comment comment = new Comment(authorId, "Old body", questionId, solutionId);

        comment.editComment("New body");

        assertEquals("New body", comment.getBody());
    }

    @Test
    public void editComment_updatesUpdatedAt() throws InterruptedException {
        Comment comment = new Comment(authorId, "Old body", questionId, solutionId);
        LocalDateTime oldTime = comment.getUpdatedAt();

        Thread.sleep(5);
        comment.editComment("New body");

        assertTrue(comment.getUpdatedAt().isAfter(oldTime));
    }

    @Test
    public void editComment_doesNothingWhenBodyNull() {
        Comment comment = new Comment(authorId, "Old body", questionId, solutionId);
        LocalDateTime oldTime = comment.getUpdatedAt();

        comment.editComment(null);

        assertEquals("Old body", comment.getBody());
        assertEquals(oldTime, comment.getUpdatedAt());
    }

    @Test
    public void deleteComment_setsBodyToNull() {
        Comment comment = new Comment(authorId, "Old body", questionId, solutionId);

        comment.deleteComment();

        assertNull(comment.getBody());
    }

    /*
     * This class tests Comment creation, editing, and delete behavior. We check constructor
     * timestamps, editing text, editing with null, and that deleteComment clears the body.
     */
}
