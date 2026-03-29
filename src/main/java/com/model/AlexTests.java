package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;

public class AlexTests {


    public static void main(String[] args) {
        AlexTests tests = new AlexTests();
        tests.runAllTests();
    }

    public void runAllTests() {
        System.out.println("Running AlexTests...\n");

        testValidAccountCreation();
        testDuplicateEmailRejected();
        testValidLogin();
        testLoginReturnsNullForWrongPassword();
        testLogoutClearsSessionBehavior();
        testSearchMissingKeyword();
        testQuestionIsStoredAfterCreation();
        testDeleteMissingQuestionFails();
        testAddingSolutionIncreasesCount();
        testSearchQuestionsFindsMatchingTitle();
        testFileReturnsFalseForBlankPath();
        testCompletedQuestionsEmptyWhenLoggedOut();
        testMarkCompletedDoesNothingWhenLoggedOut();
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
     * @return the initialized problemappplication.
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

    public void testValidAccountCreation() {
        ProblemApplication app = createFreshApplication();
    
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "alex" + unique;
        String email = "alex" + unique + "@gmail.com";

        User user = app.createAccount("Alex", username, email, "Password1239");
        printTestResult("testValidAccountCreation", user != null);     
}


    public void testDuplicateEmailRejected() {
        ProblemApplication app = createFreshApplication();
    
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String email = "same" + unique + "@gmail.com";

        User first = app.createAccount("Alex", "alex" + unique, email, "Password1239");
        User second = app.createAccount("Bill", "bill" + unique, email, "Password1239");
        printTestResult("testDuplicateEmailRejected", first != null && second == null);
    }

    public void testValidLogin() {
        ProblemApplication app = createFreshApplication();
    
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "john" + unique;
        String email = "john" + unique + "@gmail.com";

        app.createAccount("John", username, email, "Password1239");
        User user = app.login(username, "Password1239");
        printTestResult("testValidLogin", user != null);
    }

    public void testLogoutClearsSessionBehavior() {
        ProblemApplication app = createFreshApplication();
    
        String unique = UUID.randomUUID().toString().substring(0, 5);
        String username = "mike" + unique;
        String email = "mike" + unique + "@gmail.com";

        app.createAccount("Mike", username, email, "Password1239");
        app.login(username, "Password1239");
        app.logOut();

        boolean passed = app.getCompletedQuestion().size() == 0;
         printTestResult("testLogoutClearsSessionBehavior", passed);
    }

    public void testSearchMissingKeyword() {
        ProblemApplication app = createFreshApplication();

        ArrayList<Question> results = app.searchQuestions("zzzznotfoundkeyword");

        printTestResult("testSearchMissingKeyword", results.size() == 0);
    }

    public void testQuestionIsStoredAfterCreation() {
        ProblemApplication app = createFreshApplication();
    
        String unique = UUID.randomUUID().toString().substring(0, 5);
        User user = app.createAccount("Creator", "creator" + unique, "creator" + unique + "@gmail.com", "Password1239");

        Question question = buildSampleQuestion(user.getUserId(), "Two Sum");
        UUID questionId = question.getId();

        app.createQuestion(question);

        printTestResult("testQuestionIsStoredAfterCreation", app.getQuestionById(questionId) != null);
        }


    public void testDeleteMissingQuestionFails() {
        ProblemApplication app = createFreshApplication();

        boolean result = app.deleteQuestion(UUID.randomUUID());

        printTestResult("testDeleteQuestionFails", result == false);
    }

        public void testAddingSolutionIncreasesCount() {
        ProblemApplication app = createFreshApplication();

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

        printTestResult("testAddingSolutionIncreasesCount", after == before + 1);
    }

    public void testSearchQuestionsFindsMatchingTitle() {
        ProblemApplication app = createFreshApplication();

        String unique = UUID.randomUUID().toString().substring(0, 5);
        User user = app.createAccount("Searcher", "searcher" + unique, "searcher" + unique + "@gmail.com", "Password1239");

        Question question = buildSampleQuestion(user.getUserId(), "Binary Search Tree");
        app.createQuestion(question);

        ArrayList<Question> results = app.searchQuestions("Binary Search Tree");

        printTestResult("testSearchQuestionsFindsMatchingTitle", results.size() > 0);
    }

    public void testLoginReturnsNullForWrongPassword() {
    ProblemApplication app = createFreshApplication();

    String unique = UUID.randomUUID().toString().substring(0, 5);
    String username = "jane" + unique;
    String email = "jane" + unique + "@gmail.com";

    app.createAccount("Jane", username, email, "Password1239");
    User user = app.login(username, "WrongPassword1239");

    printTestResult("testLoginReturnsNullForWrongPassword", user == null);
    }   

    public void testFileReturnsFalseForBlankPath() {
        ProblemApplication app = createFreshApplication();

        boolean result = app.exportQuestionToFile(null, "");

        printTestResult("testFileReturnsFalseForBlankPath", result == false);
    }

    public void testCompletedQuestionsEmptyWhenLoggedOut() {
        ProblemApplication app = createFreshApplication();

        printTestResult("testCompletedQuestionsEmptyWhenLoggedOut",
                app.getCompletedQuestion().size() == 0);
    }

    public void testMarkCompletedDoesNothingWhenLoggedOut() {
        ProblemApplication app = createFreshApplication();

        app.markCompleted(UUID.randomUUID(), 300);

        printTestResult("testMarkCompletedDoesNothingWhenLoggedOut",
                app.getCompletedQuestion().size() == 0);
    }
}
