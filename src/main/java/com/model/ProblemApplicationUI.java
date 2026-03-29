package com.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.model.enums.Topic;


public class ProblemApplicationUI {

    private static final String SULLIVAN_EMAIL = "sullivan.sparrow.lost247@example.com";
    private static final String SALLY_UNIQUE_EMAIL = "sally.sparrow.lost247@example.com";

    private final ProblemApplication problemApplication;

    public ProblemApplicationUI() {
        problemApplication = new ProblemApplication();
        try {
            problemApplication.init();
        } catch (Exception e) {
            System.out.println("Initialization failed: " + e.getMessage());
        }
    }

    public void run() {
        scenarioCreateAccountDuplicateUser();
        scenarioCreateAccountSuccessPublisher();
        scenarioSallyCreatesQuestionAndTwoSolutions();
        scenarioJimmyBauerCompletesDailyTask();

        System.out.println("Problem Application UI Tests");
        testAccountAndLoginFlow();
        testQuestionLifecycleFlow();
        testSolutionAndSearchFlow();
        testProgressFlow();
        testSystemUtilitiesFlow();
    }

    private void printPass(String message) {
    System.out.println("PASS: " + message);
}

    private void printFail(String message) {
        System.out.println("FAIL: " + message);
    }

    /**
     * Sally tries to register using the same email as her brother Sullivan; registration is rejected.
     */
    private void scenarioCreateAccountDuplicateUser() {
        System.out.println("=== Create Account - Duplicate User ===");
        User sullivan = problemApplication.createAccount(
                "Sullivan Sparrow",
                "SullivanSparrow",
                SULLIVAN_EMAIL,
                "Sullivan1A");
        if (sullivan != null) {
            System.out.println("Brother Sullivan Sparrow account exists (seeded for this scenario).");
            User rejectedSally = problemApplication.createAccount(
                    "Sally Sparrow",
                    "SallySparrow",
                    SULLIVAN_EMAIL,
                    "SallyPass2a");
            if (rejectedSally == null) {
                System.out.println("Sally Sparrow rejected: email already used by Sullivan.");
            } else {
                System.out.println("Unexpected: Sally was accepted (duplicate check should have failed).");
            }
        } else {
            System.out.println("Sullivan account was not created (duplicate username/email in loaded data). Skipping duplicate-email rejection demo.");
        }
        System.out.println();
    }

    /**
     * Sally uses unique credentials, gets a publisher (contributor) account, and logs in.
     */
    private void scenarioCreateAccountSuccessPublisher() {
        System.out.println("=== Create Account - Success (Publisher) ===");
        User sally = problemApplication.createAccount(
                "Sally Sparrow",
                "SallySparrow",
                SALLY_UNIQUE_EMAIL,
                "SallyPass2a");
        if (sally == null) {
            System.out.println("Sally account creation failed (validation or duplicate username/email).");
            return;
        }
        System.out.println("Sally Sparrow created a Publisher account (stored as Contributor in this app).");

        User loggedIn = problemApplication.login("SallySparrow", "SallyPass2a");
        if (loggedIn == null) {
            System.out.println("Sally login failed.");
            return;
        }
        System.out.println("Sally logged in successfully.");
        System.out.println();
    }

    /**
     * Sally authors "Longest Subarray with given Sum" and two solutions; Java sources are referenced by filename only.
     */
    private void scenarioSallyCreatesQuestionAndTwoSolutions() {
        System.out.println("=== Sally Creates a New Question and Two Solutions ===");
        User loggedIn = problemApplication.login("SallySparrow", "SallyPass2a");
        if (loggedIn == null) {
            System.out.println("Sally could not log in; skipping question scenario.");
            return;
        }

        UUID qId = UUID.randomUUID();
        ArrayList<Topic> topics = new ArrayList<>();
        topics.add(Topic.ALGORITHMS_DATASTRUCTURE);

        ArrayList<String> hints = new ArrayList<>();
        hints.add("Follow-up: What is the time complexity of your algorithm?");
        hints.add("Follow-up: Can you find a way to make your algorithm faster?");

        Question q = new Question(
                qId,
                "Longest Subarray with given Sum",
                buildLongestSubarrayPrompt(),
                "MEDIUM",
                topics,
                new ArrayList<>(),
                hints,
                loggedIn.getUserId(),
                LocalDateTime.now(),
                "PUBLISHED");

        if (loggedIn instanceof Contributor) {
            Contributor contributor = (Contributor) loggedIn;
            contributor.addQuestion(q);
            TestCase[] examples = new TestCase[] {
                    new TestCase("nums = [1, -1, 5, -2, 3], k = 3", "4"),
                    new TestCase("nums = [-2, -1, 2, 1], k = 3", "2")
            };
            contributor.addTestCases(q, examples);
        }

        problemApplication.createQuestion(q);
        System.out.println("Question created: " + q.getTitle());

        LocalDateTime now = LocalDateTime.now();
        Solution brute = new Solution(
                UUID.randomUUID(),
                qId,
                loggedIn.getUserId(),
                "LongestSubarrayBruteForce.java",
                "Java",
                "Brute force: try every subarray; time O(n^2). Full Java source lives in the named file.",
                now,
                now,
                0);

        Solution hashMap = new Solution(
                UUID.randomUUID(),
                qId,
                loggedIn.getUserId(),
                "LongestSubarrayHashMap.java",
                "Java",
                "Prefix sums + HashMap O(n); store first occurrence of each prefix sum for the longest subarray. Full Java source lives in the named file.",
                now,
                now,
                0);

        problemApplication.addSolution(qId, brute);
        problemApplication.addSolution(qId, hashMap);

        Question stored = problemApplication.getQuestionById(qId);
        System.out.println("Solution 1 code (filename only): " + brute.getCode());
        System.out.println("Solution 2 code (filename only): " + hashMap.getCode());
        if (stored != null) {
            System.out.println("Attached solution count: " + stored.getSolutions().size());
        }
        problemApplication.saveAll();
        problemApplication.logOut();
        System.out.println();
    }

    /**
     * Jimmy Bauer completes his daily challenge: logs in, gets study plan, reviews solutions,
     * adds a comment, exports to file, searches for BST questions, marks completed, logs out.
     */
    private void scenarioJimmyBauerCompletesDailyTask() {
        System.out.println("=== Jimmy Bauer Completes Daily Task ===");

        User jimmy = problemApplication.createAccount(
                "Jimmy Bauer",
                "JimmyBauer",
                "jimmy.bauer.lost247@example.com",
                "JimmyPass1a");
        if (jimmy == null) {
            System.out.println("Jimmy account creation failed; skipping scenario.");
            return;
        }
        jimmy.getProgressTracker().setStreak(8);
        jimmy.getProgressTracker().setLastActiveDate(LocalDate.now().minusDays(1));
        System.out.println("Jimmy Bauer created with streak 8, last active yesterday.");

        problemApplication.logOut();
        User loggedIn = problemApplication.login("JimmyBauer", "JimmyPass1a");
        if (loggedIn == null) {
            System.out.println("Jimmy login failed.");
            return;
        }
        System.out.println("Jimmy logged in.");

        LearningPlan plan = problemApplication.createStudyPlan("Java", 2);
        Question dailyChallenge = null;
        List<PlannerStep> steps = plan.getSteps();
        if (!steps.isEmpty()) {
            List<UUID> ids = steps.get(0).getQuestionIds();
            if (!ids.isEmpty()) {
                dailyChallenge = problemApplication.getQuestionById(ids.get(0));
            }
        }
        if (dailyChallenge == null) {
            System.out.println("No daily challenge found; skipping remainder of scenario.");
            return;
        }
        System.out.println("Daily challenge: " + dailyChallenge.getTitle());

        List<Solution> solutions = dailyChallenge.getSolutions();
        if (solutions != null && solutions.size() >= 2) {
            Solution second = solutions.get(1);
            String comment = "Jimmy Bauer | " + LocalDate.now() + " | " + dailyChallenge.getTitle();
            second.addComment(comment);
            System.out.println("Jimmy added comment on second solution.");
        }

        String exportPath = "exported_daily_challenge.txt";
        if (problemApplication.exportQuestionToFile(dailyChallenge, exportPath)) {
            System.out.println("Exported question to " + exportPath);
        } else {
            System.out.println("Export failed.");
        }

        ArrayList<Topic> bstTopics = new ArrayList<>();
        bstTopics.add(Topic.ALGORITHMS_DATASTRUCTURE);
        Question bst1 = new Question(
                UUID.randomUUID(),
                "Validate Binary Search Tree",
                "Given a binary tree, determine if it is a valid binary search tree.",
                "MEDIUM",
                bstTopics,
                new ArrayList<>(),
                new ArrayList<>(),
                loggedIn.getUserId(),
                LocalDateTime.now(),
                "PUBLISHED");
        Question bst2 = new Question(
                UUID.randomUUID(),
                "Binary Search Tree to Doubly Linked List",
                "Convert a BST to a sorted doubly linked list in-place.",
                "MEDIUM",
                new ArrayList<>(bstTopics),
                new ArrayList<>(),
                new ArrayList<>(),
                loggedIn.getUserId(),
                LocalDateTime.now(),
                "PUBLISHED");
        Solution javaSol = new Solution(
                UUID.randomUUID(),
                bst1.getId(),
                loggedIn.getUserId(),
                "BST.java",
                "Java",
                "Java implementation",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0);
        bst1.addSolution(javaSol);
        problemApplication.createQuestion(bst1);
        bst2.addSolution(new Solution(
                UUID.randomUUID(),
                bst2.getId(),
                loggedIn.getUserId(),
                "BST2List.java",
                "Java",
                "Java implementation",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0));
        problemApplication.createQuestion(bst2);
        System.out.println("Created 2 Binary Search Tree questions.");

        ArrayList<Question> bstResults = problemApplication.searchQuestions("Binary Search Tree");
        System.out.println("Search 'Binary Search Tree' returned " + bstResults.size() + " question(s).");
        for (Question q : bstResults) {
            System.out.println("  - " + q.getTitle());
        }

        problemApplication.markCompleted(dailyChallenge.getId(), 300);
        System.out.println("Jimmy marked daily challenge completed (streak bumped to 9).");
        problemApplication.logOut();
        System.out.println("Jimmy logged out.");
        System.out.println();
    }

    private static String buildLongestSubarrayPrompt() {
        return String.join("\n",
                "Given an integer array nums and an integer k, return the length of the longest contiguous subarray whose total equals k.",
                "Note: the array can contain negative numbers.",
                "",
                "Example 1:",
                "Input: nums = [1, -1, 5, -2, 3], k = 3",
                "Output: 4",
                "Explanation: The subarray [1, -1, 5, -2] sums to 3 and has length 4.",
                "",
                "Example 2:",
                "Input: nums = [-2, -1, 2, 1], k = 3",
                "Output: 2");
    }

    public static void main(String[] args) {
        ProblemApplicationUI ui = new ProblemApplicationUI();
        ui.run();
    }
}
