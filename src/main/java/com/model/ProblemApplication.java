package com.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.model.enums.Topic;

/**
 * Application facade: the UI and driver use this class instead of {@link UserList}, {@link QuestionList}, or JSON directly.
 *
 * @author Christopher Feuchter
 */
public class ProblemApplication {
    private UserList userList;
    private QuestionList questionList;
    private DataLoader dataLoader;
    private DataWriter dataWriter;
    private User currentUser;
    private StudyPlanner studyPlanner;

    /**
     * Constructs lists, loader, writer, and study planner. Call {@link #init()} to load JSON.
     */
    public ProblemApplication() {
        this.userList = new UserList();
        this.questionList = new QuestionList();
        this.dataLoader = new DataLoader();
        this.dataWriter = new DataWriter();
        this.questionList.setDataWriter(this.dataWriter);
        this.studyPlanner = new StudyPlanner(this.questionList);
    }

    /**
     * Registers a new account.
     *
     * @param displayName visible name
     * @param username login name
     * @param email email address
     * @param password plain password (hashed by the list)
     * @return the new user, or {@code null} if validation fails or username/email is taken
     */
    public User createAccount(String displayName, String username, String email, String password) {
        return userList.createAccount(displayName, username, email, password);
    }

    /**
     * Authenticates and sets the current user on success.
     *
     * @param username login name
     * @param password password
     * @return the authenticated user, or {@code null} on failure
     */
    public User login(String username, String password) {
        User user = userList.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
            refreshStreakIfStale(user);
        }
        return user;
    }

    /**
     * If the user did not solve anything yesterday or today, the daily streak is no longer active.
     */
    private void refreshStreakIfStale(User user) {
        if (user == null || user.getProgressTracker() == null) {
            return;
        }
        LocalDate last = user.getProgressTracker().getLastActiveDate();
        if (last == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (last.isBefore(today.minusDays(1))) {
            user.getProgressTracker().setStreak(0);
            user.getProgressTracker().setLastActiveDate(null);
            dataWriter.saveUsers(userList.getAll());
        }
    }

    /**
     * Creates an in-memory guest session and sets it as the current user.
     *
     * @return guest user session
     */
    public User loginAsGuest() {
        this.currentUser = new Guest();
        return this.currentUser;
    }

    /**
     * Clears the logged-in user.
     */
    public void logOut() {
        this.currentUser = null;
    }

    /**
     * @return the logged-in user, or {@code null}
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @return all questions
     */
    public ArrayList<Question> getAllQuestions() {
        return questionList.getAll();
    }

    /**
     * @param questionId question id
     * @return the question, or {@code null} if missing
     */
    public Question getQuestionById(UUID questionId) {
        return questionList.getById(questionId);
    }

    /**
     * Adds a question in memory; use {@link #saveAll()} to persist.
     *
     * @param question the question to add
     */
    public void createQuestion(Question question) {
        questionList.addQuestion(question);
    }

    /**
     * @param question question with updated fields
     * @return {@code true} if updated
     */
    public boolean updateQuestion(Question question) {
        return questionList.updateQuestion(question);
    }

    /**
     * @param questionId id of the question to remove
     * @return {@code true} if removed
     */
    public boolean deleteQuestion(UUID questionId) {
        Question question = questionList.getById(questionId);
        if (question != null) {
            return questionList.deleteQuestion(question);
        }
        return false;
    }

    /**
     * @param questionId target question
     * @param solution solution to attach
     */
    public void addSolution(UUID questionId, Solution solution) {
        Question question = questionList.getById(questionId);
        if (question != null) {
            question.addSolution(solution);
            dataWriter.saveProblem(questionList.getAll());
        }
    }

    /**
     * Adds a comment on the given question for the current user and saves {@code questions.json}.
     * Guests cannot post (same gate as saving a solution).
     *
     * @return {@code true} if saved
     */
    public boolean addQuestionComment(UUID questionId, String body) {
        if (currentUser == null || questionId == null || body == null) {
            return false;
        }
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (!currentUser.canSubmitSolutions()) {
            return false;
        }
        Question question = questionList.getById(questionId);
        if (question == null) {
            return false;
        }
        Comment comment = new Comment(currentUser.getUserId(), trimmed, questionId, null);
        comment.setAuthorDisplayName(displayNameForComment(currentUser));
        question.addComment(comment);
        return dataWriter.saveProblem(questionList.getAll());
    }

    private static String displayNameForComment(User user) {
        if (user == null) {
            return "User";
        }
        String d = user.getDisplayName();
        if (d != null && !d.isBlank()) {
            return d.trim();
        }
        String u = user.getUsername();
        if (u != null && !u.isBlank()) {
            return u.trim();
        }
        return "User";
    }

    /**
     * Records an attempt for the logged-in user. No-op if not logged in.
     *
     * @param questionId question id
     * @param timeSpentSec time spent in seconds
     */
    public void recordAttempt(UUID questionId, int timeSpentSec) {
        if (currentUser == null) {
            return;
        }
        Question question = questionList.getById(questionId);
        if (question != null) {
            currentUser.getProgressTracker().recordAttempt(question);
        }
    }

    /**
     * Marks a question completed for the logged-in user. No-op if not logged in.
     *
     * @param questionId question id
     * @param timeSpentSec time spent in seconds
     */
    public void markCompleted(UUID questionId, int timeSpentSec) {
        if (currentUser == null) {
            return;
        }
        Question question = questionList.getById(questionId);
        if (question != null) {
            currentUser.getProgressTracker().markCompleted(question, timeSpentSec);
            dataWriter.saveUsers(userList.getAll());
        }
    }

    /**
     * Saves users.json (progress, streak, favorites) after a change.
     */
    public void saveUsersToDisk() {
        dataWriter.saveUsers(userList.getAll());
    }

    /**
     * Toggles favorite for the current user and saves. Returns whether the question is favorited after the toggle.
     */
    public boolean toggleFavoriteForCurrentUser(Question question) {
        if (currentUser == null || question == null) {
            return false;
        }
        boolean favorited = currentUser.toggleFavoriteProblem(question);
        dataWriter.saveUsers(userList.getAll());
        return favorited;
    }

    /**
     * @return completed questions for the current user; empty if not logged in
     */
    public ArrayList<Question> getCompletedQuestion() {
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return currentUser.getProgressTracker().getCompletedQuestionsByDifficulty();
    }

    /**
     * Loads users and questions from JSON into the lists.
     */
    public void init() {
        userList.load();
        questionList.getAll().clear();
        ArrayList<Question> questions = dataLoader.getProblems();
        if (questions != null) {
            questionList.getAll().addAll(questions);
        }
        userList.hydrateQuestionReferences(questionList);
    }

    /**
     * Persists users and questions to JSON.
     *
     * @return {@code true} if both saves succeed
     */
    public boolean saveAll() {
        boolean usersSaved = dataWriter.saveUsers(userList.getAll());
        boolean questionsSaved = dataWriter.saveProblem(questionList.getAll());
        return usersSaved && questionsSaved;
    }

    /**
     * Today's personalized plan for the logged-in user (completed problems skipped; lineup changes daily).
     */
    public LearningPlan createStudyPlan(String language, int level) {
        return createDailyStudyPlan(LocalDate.now(), language, level, null);
    }

    /**
     * Study lineup for {@code planDate}: excludes finished problems when logged in; stable per user + date;
     * optional topic filter narrows candidates (widened automatically if empty).
     */
    public LearningPlan createDailyStudyPlan(LocalDate planDate, String language, int level, Topic topicFocus) {
        if (studyPlanner == null) {
            studyPlanner = new StudyPlanner(questionList);
        }
        UUID salt = UUID.fromString("00000000-0000-4000-8000-000000000001");
        ArrayList<UUID> completed = new ArrayList<>();
        if (currentUser != null && currentUser.getProgressTracker() != null) {
            salt = currentUser.getUserId();
            completed.addAll(currentUser.getProgressTracker().getCompletedQuestionIds());
        }
        return studyPlanner.generateDailyPersonalizedPlan(planDate, salt, language, level, topicFocus, completed);
    }

    /**
     * @return leaderboard snapshot from the current user list
     */
    public LeaderBoard getLeaderBoard() {
        return new LeaderBoard(userList.getAll());
    }

    /**
     * @param limit max number of users
     * @return top users by streak
     */
    public List<User> getLeaderboardTopPerformers(int limit) {
        return getLeaderBoard().getTopPerformers(limit);
    }

    /**
     * @return aggregate leaderboard counts (see {@link LeaderBoard#getStats()})
     */
    public HashMap<String, Integer> getLeaderboardStats() {
        return getLeaderBoard().getStats();
    }

    /**
     * Writes a text export of the question to a file.
     *
     * @param question the question; ignored if {@code null}
     * @param filePath output path
     * @return {@code true} if written successfully
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
     * Title search (case-insensitive substring).
     *
     * @param query search text
     * @return matching questions
     */
    public ArrayList<Question> searchQuestions(String query) {
        return questionList.search(query);
    }
}
