package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

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

    public Solution() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.voteCount = 0;
        this.isCorrect = false;
        this.comments = new ArrayList<>();
    }

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

    public void edit(String code, String explanation) {
        if (code != null) {
            this.code = code;
        }
        if (explanation != null) {
            this.explanation = explanation;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addComment(String comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void upvote(UUID userId) {
        voteCount++;
    }

    public void downvote(UUID userId) {
        voteCount--;
    }

    public UUID getId() {
        return id;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getCode() {
        return code;
    }

    public String getLanguage() {
        return language;
    }

    public String getExplanation() {
        return explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getVoteCount() {
        return voteCount;
    }
}
