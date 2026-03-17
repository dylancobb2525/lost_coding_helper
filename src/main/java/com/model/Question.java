package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;

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

    public Question() {
        this.solutions = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

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

    public void addSolution(Solution solution) {
        if (solution != null) {
            solutions.add(solution);
        }
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
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

    public void setPublicStatus(boolean publicStatus) {
        this.status = publicStatus ? "PUBLISHED" : "DRAFT";
    }

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

    public boolean deleteQuestion() {
        return true;
    }

    public void addCodeSnippet(String snippet) {
        if (snippet != null) {
            hints.add(snippet);
        }
    }

    public void updateCodeSnippet(int index, String newSnippet) {
        if (index >= 0 && index < hints.size() && newSnippet != null) {
            hints.set(index, newSnippet);
        }
    }

    public void deleteCodeSnippet(int index) {
        if (index >= 0 && index < hints.size()) {
            hints.remove(index);
        }
    }

    public void addAttachment(String attachment) {
        if (attachment != null) {
            attachments.add(attachment);
        }
    }

    public void updateAttachment(int index, String newAttachment) {
        if (index >= 0 && index < attachments.size() && newAttachment != null) {
            attachments.set(index, newAttachment);
        }
    }

    public void deleteAttachment(int index) {
        if (index >= 0 && index < attachments.size()) {
            attachments.remove(index);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public ArrayList<String> getCompanyTags() {
        return companyTags;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public boolean getPublicStatus() {
        return "PUBLISHED".equals(status);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ArrayList<String> getHints() {
        return hints;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }
}
