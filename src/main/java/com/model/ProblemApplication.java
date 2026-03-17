package com.model;

import java.util.ArrayList;
import java.util.UUID;

public class ProblemApplication {
    private UserList userList; 
    private QuestionList questionList;
    private DataLoader dataLoader;
    private DataWriter dataWriter;
    //private Leaderboard leaderboard;
    private User currentUser; // Track currently logged-in user (need to update uml )

    // Constructor to initialize all components
    public ProblemApplication() {
        this.userList = new UserList();
        this.questionList = new QuestionList();
        this.dataLoader = new DataLoader();
        this.dataWriter = new DataWriter();
        // Set DataWriter for QuestionList so it can save
        this.questionList.setDataWriter(this.dataWriter);
    }

    public User createAccount(String displayName, String username, String email, String password) {
        return userList.createAccount(displayName, username, email, password);
    }

    public User login(String username, String password) {
        User user = userList.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }

    public void logOut() {
        this.currentUser = null;
    }

    public ArrayList<Question> getAllQuestions() {
        return questionList.getAll();
    }
    
    public Question getQuestionById(UUID questionId) {
        return questionList.getById(questionId);
    }

    public void createQuestion(Question question) {
        if (question != null) {
            questionList.getAll().add(question);
        }
    }

    public boolean updateQuestion(Question question) {
        return questionList.update(question);
    }

    public boolean deleteQuestion(UUID questionId) {
        Question question = questionList.getById(questionId);
        if (question != null) {
            return questionList.getAll().remove(question);
        }
        return false;
    }

    public void addSolution(UUID questionId, Solution solution) {
        Question question = questionList.getById(questionId);
        if (question != null) {
            question.addSolution(solution);
        }
    }

    public void recordAttempt(UUID questionId, int timeSpentSec) {
        if (currentUser == null) {
            return; // No user logged in
        }
        Question question = questionList.getById(questionId);
        if (question != null) {
            currentUser.getProgressTracker().recordAttempt(question);
        }
    }

    public void markCompleted(UUID questionId, int timeSpentSec) {
        if (currentUser == null) {
            return; // No user logged in
        }
        Question question = questionList.getById(questionId);
        if (question != null) {
            currentUser.getProgressTracker().markCompleted(question, timeSpentSec);
        }
    }

    public ArrayList<Question> getCompletedQuestion() {
        if (currentUser == null) {
            return new ArrayList<>(); // No user logged in
        }
        return currentUser.getProgressTracker().getCompletedQuestionsByDifficulty();
    }

    /**
     * Loads users and questions from json files into the lists.
     */
    public void init() {
        userList.load();
        ArrayList<Question> questions = dataLoader.getProblems();
        if (questions != null) {
            questionList.getAll().addAll(questions);
        }
    }

    public boolean saveAll() {
        boolean usersSaved = dataWriter.saveUsers(userList.getAll());
        boolean questionsSaved = dataWriter.saveProblem(questionList.getAll());
        return usersSaved && questionsSaved;
    }
}
