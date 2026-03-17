package com.model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Holds the list of questions. Add, edit, and delete go through here and can be saved to json.
 */
public class QuestionList {
    private final ArrayList<Question> questions;
    private DataWriter dataWriter;

    public QuestionList() {
        this.questions = new ArrayList<>();
    }

    public void setDataWriter(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    /**
     * Gets all questions in the list.
     */
    public ArrayList<Question> getAll() {
        return questions;
    }

    /**
     * Gets one question by its id.
     */
    public Question getById(UUID questionId) {
        if (questionId == null) {
            return null;
        }
        for (Question q : questions) {
            if (questionId.equals(q.getId())) {
                return q;
            }
        }
        return null;
    }

    /**
     * Searches questions by title (contains the query string).
     */
    public ArrayList<Question> search(String query) {
        ArrayList<Question> results = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return results;
        }
        String lowerQuery = query.toLowerCase();
        for (Question q : questions) {
            if (q.getTitle() != null && q.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(q);
            }
        }
        return results;
    }

    /**
     * Adds a new question to the list. Does not save to file (call save() after if needed).
     */
    public void addQuestion(Question question) {
        if (question != null) {
            questions.add(question);
        }
    }

    /**
     * Updates a question in the list and in the json file. Finds it by id and applies the new data.
     * @return true if found and update worked
     */
    public boolean updateQuestion(Question question) {
        if (question == null || question.getId() == null || dataWriter == null) {
            return false;
        }
        Question existing = getById(question.getId());
        if (existing == null) {
            return false;
        }
        if (!existing.updateQuestion(question)) {
            return false;
        }
        return dataWriter.updateProblem(existing);
    }

    /**
     * Deletes a question from the list and from the json file.
     * @return true if found and delete worked
     */
    public boolean deleteQuestion(Question question) {
        if (question == null || dataWriter == null) {
            return false;
        }
        boolean removed = questions.remove(question);
        if (!removed) {
            return false;
        }
        return dataWriter.deleteProblem(question);
    }

    /**
     * Saves the full list of questions to the json file.
     */
    public boolean save() {
        if (dataWriter == null) {
            return false;
        }
        return dataWriter.saveProblem(questions);
    }

    /**
     * Returns all questions that both:
     *  - have at least one solution in the given language, and
     *  - have the given difficulty.
     */
    public ArrayList<Question> getByLanguageAndDifficulty(String language, String difficulty) {
        ArrayList<Question> results = new ArrayList<>();
        if (language == null || language.isEmpty() ||
            difficulty == null || difficulty.isEmpty()) {
            return results;
        }
        String targetLang = language.toLowerCase();
        String targetDiff = difficulty.toLowerCase();

        for (Question q : questions) {
            if (q.getDifficulty() == null ||
                !q.getDifficulty().toLowerCase().equals(targetDiff) ||
                q.getSolutions() == null) {
                continue;
            }

            boolean hasLanguage = q.getSolutions().stream()
                    .anyMatch(s -> s.getLanguage() != null && s.getLanguage().toLowerCase().equals(targetLang));

            if (hasLanguage) {
                results.add(q);
            }
        }
        return results;
    }
}
