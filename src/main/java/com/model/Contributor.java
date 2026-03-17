package com.model;

import com.model.Question;
import com.model.enums.Topic;

import java.util.UUID;

public class Contributor extends User {

    public Contributor() {
        super(null, null, null, null, null, null);
    }

    public Contributor(UUID userId, String displayName, String accountId, String email, String username, String hashedPassword) {
        super(userId, displayName, accountId, email, username, hashedPassword);
    }

    @Override
    public boolean hasAccess(String feature) {
        return false;
    }

    @Override
    public boolean canSubmitSolutions() {
        return true;
    }

    @Override
    public boolean canTrackProgress() {
        return true;
    }

    @Override
    public boolean canCreateProblems() {
        return true;
    }

    @Override
    public boolean canViewMultipleHints() {
        return true;
    }

    @Override
    public boolean canFavoriteProblems() {
        return true;
    }

    public void addQuestion(Question question) {
        
    }

    public void editQuestion(Question question) {
        
    }

    public void deleteQuestion(Question question) {
        
    }

   
    public void addTestCases(Question question, String[] inputs, String[] expectedOutputs) {
        // TODO: implement when Question supports test cases
    }

    public void assignTopics(Question question, Topic[] topics) {
        
    }

    public void addHints(Question question, String[] hints) {
        
    }

    public void setComplexity(Question question, String timeComplexity, String spaceComplexity) {
         
    }
}
