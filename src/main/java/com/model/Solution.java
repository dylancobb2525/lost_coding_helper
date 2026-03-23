package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * One answer or approach for a question. Stores code (or a filename), language, explanation, and comments.
 */
public class Solution {
    private UUID id;
    private UUID questionId;
    private UUID authorId;
    private String code;
    private String language;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isCorrect;
    private int voteCount;
    private ArrayList<String> comments;

    /**
     * Default constructor: makes a new id, empty comments, and sets timestamps to now.
     */
    public Solution() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.voteCount = 0;
        this.isCorrect = false;
        this.comments = new ArrayList<>();
    }

    /**
     * Full constructor for a solution tied to a question and author.
     */
    public Solution(UUID id, UUID questionId, UUID authorId, String code, String language, String explanation,
                    LocalDateTime createdAt, LocalDateTime updatedAt, int voteCount) {
        this.id = id != null ? id : UUID.randomUUID();
        this.questionId = questionId;
        this.authorId = authorId;
        this.code = code;
        this.language = language;
        this.explanation = explanation;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.voteCount = voteCount;
        this.isCorrect = false;
        this.comments = new ArrayList<>();
    }

    /**
     * Updates the code and/or explanation and bumps the updated time.
     */
    public void edit(String code, String explanation) {
        if (code != null) {
            this.code = code;
        }
        if (explanation != null) {
            this.explanation = explanation;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Adds a comment string to this solution. */
    public void addComment(String comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    /** @return all comment strings on this solution */
    public ArrayList<String> getComments() {
        return comments;
    }

    /** Adds one to the solution's vote count. */
    public void upvote(UUID userId) {
        voteCount++;
    }

    /** Subtracts one from the vote count if it is above zero. */
    public void downvote(UUID userId) {
        if (voteCount > 0) {
            voteCount--;
        }
    }

    /** @return this solution's id */
    public UUID getId() {
        return id;
    }

    /** @return which question this solution belongs to */
    public UUID getQuestionId() {
        return questionId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    /** @return code text or filename depending on how we store it */
    public String getCode() {
        return code;
    }

    /** @return programming language (e.g. Java) */
    public String getLanguage() {
        return language;
    }

    /** @return written explanation of the approach */
    public String getExplanation() {
        return explanation;
    }

    /** @return when the solution was created */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return last time the solution was edited */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** @return whether this solution is marked correct */
    public boolean getIsCorrect() {
        return isCorrect;
    }

    /** Sets if the solution counts as correct. */
    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    /** @return vote total */
    public int getVoteCount() {
        return voteCount;
    }
}
