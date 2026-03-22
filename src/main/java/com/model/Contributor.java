package com.model;

import com.model.enums.Topic;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Subclass of {@link User} for users who create and edit problems (UML: Contributor / Contributer).
 * Question changes are in-memory; persistence is via {@link QuestionList} / {@link DataWriter}.
 */
public class Contributor extends User {

    public Contributor() {
        super(null, null, null, null, null, null);
    }

    public Contributor(UUID userId, String displayName, String accountId, String email, String username, String hashedPassword) {
        super(userId, displayName, accountId, email, username, hashedPassword);
    }

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

    /** Prepares a new {@link Question}; does not add it to {@link QuestionList}. */
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

    /** Normalizes null collections on a {@link Question} (e.g. after the no-arg constructor). */
    public void editQuestion(Question question) {
        if (question == null) {
            return;
        }
        question.updateQuestion(question);
    }

    /** Model hook; removing from storage is done by {@link QuestionList#deleteQuestion(Question)}. */
    public void deleteQuestion(Question question) {
        if (question == null) {
            return;
        }
        question.deleteQuestion();
    }

    /**
     * {@link Question} has no test-case field yet; cases are appended as lines on {@link Question#getHints()}.
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

    /**
     * UML: time and space complexity strings. Stored in {@link Question#getDifficulty()} until the model has separate fields.
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

    private static <T> ArrayList<T> copyOrEmpty(ArrayList<T> list) {
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

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
