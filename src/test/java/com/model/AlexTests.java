package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import com.model.enums.Topic;

public class AlexTests {

    private ProblemApplication app;

    @Before
    public void setUp() {
        app = new ProblemApplication();
        try {
            app.init();
        } catch (Exception e) {
            System.out.println("Warning: init failed during test setup: " + e.getMessage());
        }
    }

    /**
     * Prints if the test failed or passed.
     * @param testName
     * @param passed
     */
    public void printTestResult(String testName, boolean passed) {
        if (passed) {
            System.out.println("PASSED: " + testName);
        } else {
            System.out.println("FAILED: " + testName);
        }
    }

    /**
     * Initializes a fresh problemapplication to test.
     * @return the initialized problemapplication.
     */
    private ProblemApplication createFreshApplication() {
        ProblemApplication app = new ProblemApplication();
        try {
            app.init();
        } catch (Exception e) {
            System.out.println("Warning: init failed during test setup: " + e.getMessage());
        }
        return app;
    }

    /**
     * Question that is is used in the tests.
     * @param creatorId
     * @param title
     * @return the Question object.
     */
    private Question buildSampleQuestion(UUID creatorId, String title) {
        ArrayList<Topic> topics = new ArrayList<>();
        topics.add(Topic.ALGORITHMS_DATASTRUCTURE);

        ArrayList<String> hints = new ArrayList<>();
        hints.add("Use a hashmap.");

        return new Question(
            UUID.randomUUID(),
            title,
            "Sample prompt",
            "MEDIUM",
            topics,
            new ArrayList<>(),
            hints,
            creatorId,
            LocalDateTime.now(),
            "PUBLISHED"
        );
    }

    @Test
    public void testValidAccountCreation() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "alex" + unique;
        String email = "alex" + unique + "@gmail.com";

        User user = app.createAccount("Alex", username, email, "Password1239");
        assertNotNull(user);
    }

    @Test
    public void testDuplicateEmailRejected() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String email = "same" + unique + "@gmail.com";

        User first = app.createAccount("Alex", "alex" + unique, email, "Password1239");
        User second = app.createAccount("Bill", "bill" + unique, email, "Password1239");

        assertNotNull(first);
        assertNull(second);
    }

    @Test
    public void testValidLogin() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "john" + unique;
        String email = "john" + unique + "@gmail.com";

        app.createAccount("John", username, email, "Password1239");
        User user = app.login(username, "Password1239");

        assertNotNull(user);
    }

    @Test
    public void testLogoutClearsSessionBehavior() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "mike" + unique;
        String email = "mike" + unique + "@gmail.com";

        app.createAccount("Mike", username, email, "Password1239");
        app.login(username, "Password1239");
        app.logOut();

        boolean passed = app.getCompletedQuestion().size() == 0;
        assertTrue(passed);
    }

    @Test
    public void testSearchMissingKeyword() {
        ArrayList<Question> results = app.searchQuestions("zzzznotfoundkeyword");
        assertEquals(0, results.size());
    }

    @Test
    public void testQuestionIsStoredAfterCreation() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        User user = app.createAccount("Creator", "creator" + unique, "creator" + unique + "@gmail.com", "Password1239");

        Question question = buildSampleQuestion(user.getUserId(), "Two Sum");
        UUID questionId = question.getId();

        app.createQuestion(question);

        assertNotNull(app.getQuestionById(questionId));
    }

    @Test
    public void testDeleteMissingQuestionFails() {
        boolean result = app.deleteQuestion(UUID.randomUUID());
        assertFalse(result);
    }

    @Test
    public void testAddingSolutionIncreasesCount() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        User user = app.createAccount("Solver", "solver" + unique, "solver" + unique + "@gmail.com", "Password1239");

        Question question = buildSampleQuestion(user.getUserId(), "Binary Search");
        app.createQuestion(question);

        int before = app.getQuestionById(question.getId()).getSolutions().size();

        Solution solution = new Solution(
            UUID.randomUUID(),
            question.getId(),
            user.getUserId(),
            "BinarySearch.java",
            "Java",
            "Uses binary search",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );

        app.addSolution(question.getId(), solution);

        int after = app.getQuestionById(question.getId()).getSolutions().size();

        assertEquals(before + 1, after);
    }

    @Test
    public void testSearchQuestionsFindsMatchingTitle() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        User user = app.createAccount("Searcher", "searcher" + unique, "searcher" + unique + "@gmail.com", "Password1239");

        Question question = buildSampleQuestion(user.getUserId(), "Binary Search Tree");
        app.createQuestion(question);

        ArrayList<Question> results = app.searchQuestions("Binary Search Tree");

        assertTrue(results.size() > 0);
    }

    @Test
    public void testLoginReturnsNullForWrongPassword() {
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "jane" + unique;
        String email = "jane" + unique + "@gmail.com";

        app.createAccount("Jane", username, email, "Password1239");
        User user = app.login(username, "WrongPassword1239");

        assertNull(user);
    }

    @Test
    public void testFileReturnsFalseForBlankPath() {
        boolean result = app.exportQuestionToFile(null, "");
        assertFalse(result);
    }

    @Test
    public void testCompletedQuestionsEmptyWhenLoggedOut() {
        assertEquals(0, app.getCompletedQuestion().size());
    }

    @Test
    public void testMarkCompletedDoesNothingWhenLoggedOut() {
        app.markCompleted(UUID.randomUUID(), 300);

        assertEquals(0, app.getCompletedQuestion().size());
    }
}