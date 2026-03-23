package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;

/**
 * Represents one coding problem in the app. It has a title, prompt, difficulty, topics, etc.
 * A question can have multiple solutions, comments, and attachments.
 */
public class Question {
    private UUID id;
    private String title;
    private String prompt;
    private String difficulty;
    private ArrayList<Topic> topics;
    private ArrayList<String> companyTags;
    private String link;
    private ArrayList<String> hints;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private String status;
    private int voteCount;
    private ArrayList<Solution> solutions;
    private ArrayList<String> comments;
    private ArrayList<String> attachments;

    /**
     * Makes an empty question and sets up empty lists for solutions, comments, and attachments.
     */
    public Question() {
        this.solutions = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    /**
     * Makes a question with all the main fields filled in (id, title, prompt, difficulty, and so on).
     */
    public Question(UUID id, String title, String prompt, String difficulty, ArrayList<Topic> topics, ArrayList<String> companyTags,
                    ArrayList<String> hints, UUID createdBy, LocalDateTime createdAt, String status) {
        this.id = id;
        this.title = title;
        this.prompt = prompt;
        this.difficulty = difficulty;
        this.topics = topics != null ? topics : new ArrayList<>();
        this.companyTags = companyTags != null ? companyTags : new ArrayList<>();
        this.hints = hints != null ? hints : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
        this.voteCount = 0;
        this.solutions = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    /**
     * Adds a solution to this question if it is not null.
     */
    public void addSolution(Solution solution) {
        if (solution != null) {
            solutions.add(solution);
        }
    }

    /**
     * @return all solutions attached to this question
     */
    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    /**
     * Adds a comment string to this question (stored in a simple list).
     */
    public void addComment(String comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    /**
     * @return the list of comment strings on this question
     */
    public ArrayList<String> getComments() {
        return comments;
    }

    /** Adds one to the vote count (upvote). */
    public void upvote(UUID userId) {
        voteCount++;
    }

    /** Takes one away from the vote count if it is above zero. */
    public void downvote(UUID userId) {
        if (voteCount > 0) {
            voteCount--;
        }
    }

    /**
     * Sets whether the question is published or still a draft.
     */
    public void setPublicStatus(boolean publicStatus) {
        this.status = publicStatus ? "PUBLISHED" : "DRAFT";
    }

    /**
     * Copies fields from another question into this one (like editing). Returns false if the other question is null.
     */
    public boolean updateQuestion(Question updatedQuestion) {
        if (updatedQuestion == null) return false;
        this.title = updatedQuestion.getTitle();
        this.prompt = updatedQuestion.getPrompt();
        this.difficulty = updatedQuestion.getDifficulty();
        this.topics = updatedQuestion.getTopics() != null ? new ArrayList<>(updatedQuestion.getTopics()) : new ArrayList<>();
        this.companyTags = updatedQuestion.getCompanyTags() != null ? new ArrayList<>(updatedQuestion.getCompanyTags()) : new ArrayList<>();
        this.hints = updatedQuestion.getHints() != null ? new ArrayList<>(updatedQuestion.getHints()) : new ArrayList<>();
        this.status = updatedQuestion.getStatus();
        this.link = updatedQuestion.getLink();
        return true;
    }

    /**
     * Placeholder for delete logic; returns true for now.
     */
    public boolean deleteQuestion() {
        return true;
    }

    /** Adds a string to the hints list (used like a code snippet helper). */
    public void addCodeSnippet(String snippet) {
        if (snippet != null) {
            hints.add(snippet);
        }
    }

    /** Replaces the hint at the given index with a new string. */
    public void updateCodeSnippet(int index, String newSnippet) {
        if (index >= 0 && index < hints.size() && newSnippet != null) {
            hints.set(index, newSnippet);
        }
    }

    /** Removes the hint at the given index if it is valid. */
    public void deleteCodeSnippet(int index) {
        if (index >= 0 && index < hints.size()) {
            hints.remove(index);
        }
    }

    /** Adds an attachment string to the list. */
    public void addAttachment(String attachment) {
        if (attachment != null) {
            attachments.add(attachment);
        }
    }

    /** Changes the attachment at index to a new value. */
    public void updateAttachment(int index, String newAttachment) {
        if (index >= 0 && index < attachments.size() && newAttachment != null) {
            attachments.set(index, newAttachment);
        }
    }

    /** Removes the attachment at the given index. */
    public void deleteAttachment(int index) {
        if (index >= 0 && index < attachments.size()) {
            attachments.remove(index);
        }
    }

    /** @return this question's id */
    public UUID getId() {
        return id;
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @return the problem description / prompt */
    public String getPrompt() {
        return prompt;
    }

    /** @return difficulty (like EASY or MEDIUM) */
    public String getDifficulty() {
        return difficulty;
    }

    /** @return topic tags for this question */
    public ArrayList<Topic> getTopics() {
        return topics;
    }

    /** @return company tags (Google, etc.) */
    public ArrayList<String> getCompanyTags() {
        return companyTags;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    /** @return when the question was created */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return status string (e.g. PUBLISHED or DRAFT) */
    public String getStatus() {
        return status;
    }

    public boolean getPublicStatus() {
        return "PUBLISHED".equals(status);
    }

    /** @return optional external link */
    public String getLink() {
        return link;
    }

    /** Sets the optional link field. */
    public void setLink(String link) {
        this.link = link;
    }

    /** @return hint strings */
    public ArrayList<String> getHints() {
        return hints;
    }

    /** @return how many votes this question has */
    public int getVoteCount() {
        return voteCount;
    }

    /** @return attachment strings */
    public ArrayList<String> getAttachments() {
        return attachments;
    }
}
