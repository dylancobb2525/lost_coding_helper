package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;



public class ProblemApplicationUI {
    private ProblemApplication problemApplication;

    public ProblemApplicationUI() {
        problemApplication = new ProblemApplication();
        try {
        problemApplication.init();
        } catch (Exception e) {
            System.out.println("Initialization failed: " + e.getMessage());
        }
    }

    public void run() {
        scenario1();
        scenario2();
    }

    public void scenario1() {
        System.out.println();

        /* 
        User user = problemApplication.createAccount("Jane good", "Jane", "JaneGood249@gmail.com", "Password123");
        if (user == null) {
            System.out.println("Account creation failed for Jane good");
            return;
        } 
        System.out.println("Account created successfully for Jane good");
        */
        

        User loggedInUser = problemApplication.login("Jane", "Password123");
        if (loggedInUser == null) {
            System.out.println("Login failed for Jane good");
            return;
        }
        System.out.println("Login successful for Jane good");
        

        UUID qId = UUID.randomUUID();
        ArrayList<Topic> topics = new ArrayList<>();
        topics.add(Topic.OOP);

        ArrayList<String> companyTags = new ArrayList<>();
        companyTags.add("Google");

        ArrayList<String> hints = new ArrayList<>();
        hints.add("Think about using a hash map.");

        Question q = new Question(qId, "Two Sum", "Given an array of integers, return indices of the two numbers such that they add up to a specific target.", 
                                "EASY", topics, companyTags, hints, loggedInUser.getUserId(), LocalDateTime.now(), "PUBLISHED");

        problemApplication.createQuestion(q);
        System.out.println("Question " + q.getTitle() + " created ");

        Solution s = new Solution(UUID.randomUUID(), qId, loggedInUser.getUserId(), "public int[] twoSum(int[] nums, int target)...",
                         "Java", "Use a map...", LocalDateTime.now(), LocalDateTime.now(), 0);

        problemApplication.addSolution(qId, s);
        System.out.println("Solution added to question " + q.getTitle());
        System.out.println("Solution count is " + q.getSolutions().size());

    }

    public void scenario2() {
        System.out.println();

        /* 
        User user = problemApplication.createAccount("Philly bob", "Pbob", "PhillyBob@gmail.com", "Password058");

        if (user == null) {
            System.out.println("Account creation failed for Philly bob");
            return;
        }
        System.out.println("Account created successfully for Philly bob");
        */

        User loggedInUser = problemApplication.login("Pbob", "Password058");
        if (loggedInUser == null) {
            System.out.println("Login failed for Philly bob");
            return;
        }
        System.out.println("Login successful for Philly bob");
    
            UUID qdId = UUID.randomUUID();

            ArrayList<Topic> topics = new ArrayList<>();
            topics.add(Topic.DATABASE);

            ArrayList<String> companyTags = new ArrayList<>();
            companyTags.add("Microsoft");

            ArrayList<String> hints = new ArrayList<>();
            hints.add("...");

            Question qd = new Question(qdId, "SQL", "Write a SQL query that returns all users that have been created in the last 30 days.", 
                "EASY", topics, companyTags, hints, loggedInUser.getUserId(), LocalDateTime.now(), "PUBLISHED");

            problemApplication.createQuestion(qd);
            System.out.println("Question " + qd.getTitle() + " created ");
     


            Question found = problemApplication.getQuestionById(qd.getId());
            if (found == null) {
                System.out.println("Could not find question by id");
            } else {
                System.out.println("Question found by id");
            }

            problemApplication.recordAttempt(qd.getId(), 55);
            System.out.println("Recorded attempt for question " + qd.getTitle());

            problemApplication.markCompleted(qd.getId(), 55);
            System.out.println("Marked question " + qd.getTitle() + " as completed");

            if (!problemApplication.saveAll()) {
                System.out.println("Save failed");
            } else {
                System.out.println("Save successful");
            }

            problemApplication.logOut();
            System.out.println("Logged out successfully");
        } 




        public static void main(String[] args) {
            ProblemApplicationUI ui = new ProblemApplicationUI();
            ui.run();
        }
    }





