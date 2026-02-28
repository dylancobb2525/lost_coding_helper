package com.model;

import java.util.ArrayList;
import java.util.UUID;

public class QuestionList {
    private ArrayList<Question> questions;

    public QuestionList() {
        this.questions = new ArrayList<>();
    }

    public ArrayList<Question> getAll() {
        return questions;
    }

    public Question getById(UUID questionId) {
        return null;
    }

    public ArrayList<Question> search(String query) {
        return new ArrayList<>();
    }

    public boolean update(Question question) {
        return false;
    }

    public boolean save() {
        return false;
    }
}
