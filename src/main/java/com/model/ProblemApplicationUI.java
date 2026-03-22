package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
