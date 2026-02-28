package com.lost_coding_helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Solution {
    private UUID id;
    private UUID questionId;
    private UUID authorId;
    private String code;
    private Language language;
    private String explanation;
    private DateTime createdAt;
    private DateTime updatedAt;
    private int voteCount;
    private ArrayList<Comment> comments;

    public Solution() {
        this.id = UUID.randomUUID();
        this.createdAt = DateTime.now();
        this.updatedAt = DateTime.now();
        this.voteCount = 0;
        this.comments = new ArrayList<>();
    }

    public void edit(String code, String explanation) {
        
    }

    public void addComment(Comment comment) {
        
    }

    public void upvote(UUID userId) {
        
    }

    public void downvote(UUID userId) {
        
    }
}
