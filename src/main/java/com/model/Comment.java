package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A comment someone left on a question or solution. Keeps the text and who wrote it and when.
 */
public class Comment {
    private UUID id;
    private UUID authorId;
    private String body;
    private LocalDateTime createdOn;
    private LocalDateTime updatedAt;
    private UUID questionId;
    private UUID solutionId;

    /**
     * Makes a new comment with a fresh id and timestamps set to now.
     */
    public Comment(UUID authorId, String body, UUID questionId, UUID solutionId) {
        this.id = UUID.randomUUID();
        this.authorId = authorId;
        this.body = body;
        this.createdOn = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.questionId = questionId;
        this.solutionId = solutionId;
    }

    /**
     * Makes a comment when you already know the id and times (like loading from JSON).
     */
    public Comment(UUID id, UUID authorId, String body, LocalDateTime createdOn, LocalDateTime updatedAt,
                   UUID questionId, UUID solutionId) {
        this.id = id;
        this.authorId = authorId;
        this.body = body;
        this.createdOn = createdOn != null ? createdOn : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.questionId = questionId;
        this.solutionId = solutionId;
    }

    /** Changes the comment text and updates the "last updated" time. */
    public void editComment(String newBody) {
        if (newBody != null) {
            this.body = newBody;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /** Clears the body (soft delete style). */
    public void deleteComment() {
        this.body = null;
    }

    /** @return comment id */
    public UUID getId() {
        return id;
    }

    /** @return who wrote the comment */
    public UUID getAuthorId() {
        return authorId;
    }

    /** @return the comment text */
    public String getBody() {
        return body;
    }

    /** Sets the comment text. */
    public void setBody(String body) {
        this.body = body;
    }

    /** @return when it was first posted */
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    /** @return last edit time */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** @return related question id, if any */
    public UUID getQuestionId() {
        return questionId;
    }

    /** Sets which question this comment is on. */
    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    /** @return related solution id, if any */
    public UUID getSolutionId() {
        return solutionId;
    }

    /** Sets which solution this comment is on. */
    public void setSolutionId(UUID solutionId) {
        this.solutionId = solutionId;
    }
}
