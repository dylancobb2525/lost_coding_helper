package com.model;

import com.model.enums.Topic;

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author Christopher Feuchter
 */
public class Contributor extends User {

    /**
     * Constructs a contributor with default  values given by the superclass.
     */
    public Contributor() {
        super(null, null, null, null, null, null);
    }

    /**
     * Constructs a contributor with the given account fields.
     *
     * @param userId         
     * @param displayName    
     * @param accountId      
     * @param email          
     * @param hashedPassword 
     */
    public Contributor(UUID userId, String displayName, String accountId, String email, String username, String hashedPassword) {
        super(userId, displayName, accountId, email, username, hashedPassword);
    }

    /**
     * Returns whether this contributor may use a named feature, based on simple checks.
     *
     * @param feature a feature name or description; null or blank means that it is false
     * @return true if the feature string matches, otherwise false
     */
    @Override
    public boolean hasAccess(String feature) {
        if (feature == null || feature.trim().isEmpty()) {
            return false;
        }
        String f = feature.trim().toLowerCase();
        if (f.contains("solution")) {
            return canSubmitSolutions();
        }
        if (f.contains("progress") || f.contains("track")) {
            return canTrackProgress();
        }
        if (f.contains("problem") || f.contains("create")) {
            return canCreateProblems();
        }
        if (f.contains("hint")) {
            return canViewMultipleHints();
        }
        if (f.contains("favorite") || f.contains("favourite")) {
            return canFavoriteProblems();
        }
        return false;
    }

    /**
     * @return true; contributors may submit solutions
     */
    @Override
    public boolean canSubmitSolutions() {
        return true;
    }

    /**
     * @return true; contributors may track progress
     */
    @Override
    public boolean canTrackProgress() {
        return true;
    }

    /**
     * @return true; contributors may create problems
     */
    @Override
    public boolean canCreateProblems() {
        return true;
    }

    /**
     * @return true; contributors may view multiple hints
     */
    @Override
    public boolean canViewMultipleHints() {
        return true;
    }

    /**
     * @return true; contributors may favorite problems
     */
    @Override
    public boolean canFavoriteProblems() {
        return true;
    }

    /*
     * @param question the question to prepare; ignored if null
     */
    public void addQuestion(Question question) {
        if (question == null) {
            return;
        }
        if (question.getStatus() == null) {
            question.setPublicStatus(false);
        }
        if (question.getDifficulty() == null) {
            setComplexity(question, "EASY", null);
        }
    }

    /**
     
     * @param question the question to normalize; ignored if null
     */
    public void editQuestion(Question question) {
        if (question == null) {
            return;
        }
        question.updateQuestion(question);
    }

    /*
     * @param question the question to delete; ignored if null
     */
    public void deleteQuestion(Question question) {
        if (question == null) {
            return;
        }
        question.deleteQuestion();
    }

    /*
     
     * @param question  the target question; ignored if null
     * @param testCases the test cases to append; ignored if null
     */
    public void addTestCases(Question question, TestCase[] testCases) {
        if (question == null || testCases == null) {
            return;
        }
        ArrayList<String> hints = copyOrEmpty(question.getHints());
        int i = 0;
        for (TestCase tc : testCases) {
            if (tc == null) {
                continue;
            }
            String in = tc.getInput() != null ? tc.getInput().trim() : "";
            String ex = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
            if (in.isEmpty() && ex.isEmpty()) {
                continue;
            }
            i++;
            hints.add("Test case " + i + ": input=" + in + ", expected=" + ex);
        }
        patchQuestion(question, null, null, hints);
    }

    /**
     * Replaces the topics on the question with the given values.
     *
     * @param question the target question; ignored if null
     * @param topics     the topics to assign; null or empty elements are skipped
     */
    public void assignTopics(Question question, Topic[] topics) {
        if (question == null) {
            return;
        }
        ArrayList<Topic> list = new ArrayList<>();
        if (topics != null) {
            for (Topic t : topics) {
                if (t != null) {
                    list.add(t);
                }
            }
        }
        patchQuestion(question, null, list, null);
    }

    /*
     * @param question the target question; ignored if null
     * @param hints    the hints to add; ignored if null
     */
    public void addHints(Question question, String[] hints) {
        if (question == null || hints == null) {
            return;
        }
        ArrayList<String> merged = copyOrEmpty(question.getHints());
        for (String h : hints) {
            if (h == null) {
                continue;
            }
            String t = h.trim();
            if (!t.isEmpty()) {
                merged.add(t);
            }
        }
        patchQuestion(question, null, null, merged);
    }

    /* 
     * @param question         the target question; ignored if null
     * @param timeComplexity   time complexity text; may be null or blank
     * @param spaceComplexity space complexity text; may be null or blank
     */
    public void setComplexity(Question question, String timeComplexity, String spaceComplexity) {
        if (question == null) {
            return;
        }
        String t = timeComplexity != null ? timeComplexity.trim() : "";
        String s = spaceComplexity != null ? spaceComplexity.trim() : "";
        String value;
        if (!t.isEmpty() && !s.isEmpty()) {
            value = t + " | " + s;
        } else if (!t.isEmpty()) {
            value = t;
        } else if (!s.isEmpty()) {
            value = s;
        } else {
            return;
        }
        patchQuestion(question, value, null, null);
    }

    /**
     * @param list the list to copy, or null for an empty list
     * @param <T>  the element type
     * @return a new list containing the same elements, or an empty list if the argument was null
     */
    private static <T> ArrayList<T> copyOrEmpty(ArrayList<T> list) {
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /*
     * @param q             the question to update; ignored if null
     * @param newDifficulty the new difficulty, or null to keep the current value
     * @param newTopics     the new topics list, or null to keep the current value
     * @param newHints      the new hints list, or null to keep the current value
     */
    private void patchQuestion(Question q, String newDifficulty, ArrayList<Topic> newTopics, ArrayList<String> newHints) {
        if (q == null) {
            return;
        }
        String diff = newDifficulty != null ? newDifficulty : q.getDifficulty();
        ArrayList<Topic> topics = newTopics != null ? newTopics : copyOrEmpty(q.getTopics());
        ArrayList<String> tags = copyOrEmpty(q.getCompanyTags());
        ArrayList<String> hints = newHints != null ? newHints : copyOrEmpty(q.getHints());

        Question updated = new Question(
                q.getId(),
                q.getTitle(),
                q.getPrompt(),
                diff,
                topics,
                tags,
                hints,
                q.getCreatedBy(),
                q.getCreatedAt(),
                q.getStatus());
        updated.setLink(q.getLink());
        q.updateQuestion(updated);
    }
}
