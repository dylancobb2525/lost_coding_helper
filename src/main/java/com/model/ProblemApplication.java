package com.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
public class ProblemApplication {
    private UserList userList; 
    private QuestionList questionList;
    private DataLoader dataLoader;
    private DataWriter dataWriter;
    private User currentUser; // Track currently logged-in user (need to update uml )
    private StudyPlanner studyPlanner;

    // Constructor to initialize all components
    public ProblemApplication() {
        this.userList = new UserList();
        this.questionList = new QuestionList();
        this.dataLoader = new DataLoader();
        this.dataWriter = new DataWriter();
        // Set DataWriter for QuestionList so it can save
        this.questionList.setDataWriter(this.dataWriter);
        this.studyPlanner = new StudyPlanner(this.questionList);
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
        questionList.addQuestion(question);
    }

    public boolean updateQuestion(Question question) {
        return questionList.updateQuestion(question);
    }

    public boolean deleteQuestion(UUID questionId) {
        Question question = questionList.getById(questionId);
        if (question != null) {
            return questionList.deleteQuestion(question);
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

    /**
     * Creates a study plan for the given language and level.
     * level: 1 = beginner, 2 = intermediate, 3 = advanced (conceptually).
     * All levels currently map to the underlying "EASY" difficulty used
     * by questions in this project.
     * language: for example "Java", "C++", "Python".
     */
    public LearningPlan createStudyPlan(String language, int level) {
        if (studyPlanner == null) {
            studyPlanner = new StudyPlanner(questionList);
        }
        return studyPlanner.generatePlan(language, level);
    }

    /**
     * Snapshot of all users for leaderboard (by streak). A new instance each call so it matches the current UserList.
     */
    public LeaderBoard getLeaderBoard() {
        return new LeaderBoard(userList.getAll());
    }

    /** Convenience: top {@code limit} users by streak. */
    public List<User> getLeaderboardTopPerformers(int limit) {
        return getLeaderBoard().getTopPerformers(limit);
    }

    /** Convenience: aggregate stats (see {@link LeaderBoard#getStats()}). */
    public HashMap<String, Integer> getLeaderboardStats() {
        return getLeaderBoard().getStats();
    }

    /**
     * Writes a formatted text representation of the question to a file for offline review.
     * @param question the question to export; ignored if null
     * @param filePath path for the output .txt file
     * @return true if the file was written successfully
     */
    public boolean exportQuestionToFile(Question question, String filePath) {
        if (question == null || filePath == null || filePath.isBlank()) {
            return false;
        }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(filePath)))) {
            out.println("=== " + question.getTitle() + " ===");
            out.println("Difficulty: " + question.getDifficulty());
            out.println();
            out.println("PROMPT:");
            out.println(question.getPrompt() != null ? question.getPrompt() : "");
            out.println();
            if (question.getHints() != null && !question.getHints().isEmpty()) {
                out.println("HINTS:");
                for (String h : question.getHints()) {
                    out.println("  - " + h);
                }
                out.println();
            }
            if (question.getSolutions() != null && !question.getSolutions().isEmpty()) {
                out.println("SOLUTIONS:");
                int i = 1;
                for (Solution s : question.getSolutions()) {
                    out.println("  Solution " + i + " (" + (s.getLanguage() != null ? s.getLanguage() : "") + "):");
                    out.println("    File: " + (s.getCode() != null ? s.getCode() : "(none)"));
                    out.println("    " + (s.getExplanation() != null ? s.getExplanation() : ""));
                    if (s.getComments() != null && !s.getComments().isEmpty()) {
                        for (String c : s.getComments()) {
                            out.println("    Comment: " + c);
                        }
                    }
                    out.println();
                    i++;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Searches questions by title (contains query, case-insensitive).
     */
    public ArrayList<Question> searchQuestions(String query) {
        return questionList.search(query);
    }
}
