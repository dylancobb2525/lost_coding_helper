package com.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Handles saving our data to JSON. Called when we need to persist users or questions.
 */
public class DataWriter extends DataConstants {

    /**
     * Writes the user list to users.json. Returns false if something went wrong.
     */
    public boolean saveUsers(ArrayList<User> users) {
        try {
            String path = resolveDataPath(USER_FILE_NAME);
            JSONObject root = readJson(path);
            if (root == null) {
                root = new JSONObject();
                root.put("achievements", new JSONArray());
                root.put(USERS, new JSONArray());
                root.put("progressTrackers", new JSONArray());
                root.put("leaderboard", new JSONArray());
            }
            JSONArray usersArray = new JSONArray();
            for (User u : users) {
                usersArray.add(userToJson(u));
            }
            root.put(USERS, usersArray);
            return writeJson(path, root);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Writes the question list to questions.json. Replaces whatever was in there before.
     */
    public boolean saveProblem(ArrayList<Question> problems) {
        try {
            String path = resolveDataPath(QUESTION_FILE_NAME);
            JSONObject root = readJson(path);
            if (root == null) {
                root = new JSONObject();
                root.put("languages", new JSONArray());
                root.put(QUESTIONS, new JSONArray());
            }
            JSONArray questionsArray = new JSONArray();
            for (Question q : problems) {
                questionsArray.add(questionToJson(q));
            }
            root.put(QUESTIONS, questionsArray);
            return writeJson(path, root);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds a question in the file by id and swaps it with the new version. Returns false if it wasn't found.
     */
    public boolean updateProblem(Question problem) {
        try {
            if (problem == null || problem.getId() == null) return false;
            String path = resolveDataPath(QUESTION_FILE_NAME);
            JSONObject root = readJson(path);
            if (root == null) return false;
            JSONArray questions = (JSONArray) root.get(QUESTIONS);
            if (questions == null) return false;
            String targetId = problem.getId().toString();
            for (int i = 0; i < questions.size(); i++) {
                JSONObject q = (JSONObject) questions.get(i);
                if (targetId.equals(q.get(QUESTION_ID))) {
                    questions.set(i, questionToJson(problem));
                    return writeJson(path, root);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Removes a question from the file by matching its id. Returns false if we couldn't find it.
     */
    public boolean deleteProblem(Question problem) {
        try {
            if (problem == null || problem.getId() == null) return false;
            String path = resolveDataPath(QUESTION_FILE_NAME);
            JSONObject root = readJson(path);
            if (root == null) return false;
            JSONArray questions = (JSONArray) root.get(QUESTIONS);
            if (questions == null) return false;
            String targetId = problem.getId().toString();
            for (int i = 0; i < questions.size(); i++) {
                JSONObject q = (JSONObject) questions.get(i);
                if (targetId.equals(q.get(QUESTION_ID))) {
                    questions.remove(i);
                    return writeJson(path, root);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates one user's favorite problems in users.json. Uses the userId to find the right user.
     */
    public boolean saveFavorites(UUID userId, ArrayList<Question> favorites) {
        try {
            String path = resolveDataPath(USER_FILE_NAME);
            JSONObject root = readJson(path);
            if (root == null) return false;
            JSONArray users = (JSONArray) root.get(USERS);
            if (users == null) return false;
            String targetId = userId.toString();
            for (Object o : users) {
                JSONObject u = (JSONObject) o;
                if (targetId.equals(u.get(USER_ID))) {
                    JSONArray favIds = new JSONArray();
                    for (Question q : favorites) {
                        if (q == null || q.getId() == null) {
                            continue;
                        }
                        favIds.add(q.getId().toString());
                    }
                    u.put("favoriteProblems", favIds);
                    return writeJson(path, root);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads a JSON file and parses it into an object. Returns null if the file is missing or invalid.
     */
    private JSONObject readJson(String path) {
        try (FileReader fr = new FileReader(path)) {
            Object parsed = new JSONParser().parse(fr);
            return parsed instanceof JSONObject ? (JSONObject) parsed : null;
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    /**
     * Writes a JSON object to a file. Returns false if the write fails for any reason.
     */
    private boolean writeJson(String path, JSONObject root) {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(root.toJSONString());
            fw.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Turns a User object into a JSON object so we can save it. Just maps each field to the right key.
     */
    @SuppressWarnings("unchecked")
    private JSONObject userToJson(User u) {
        JSONObject obj = new JSONObject();
        obj.put(USER_ID, u.getUserId() != null ? u.getUserId().toString() : null);
        obj.put(USER_JOIN_DATE, u.getJoinDate() != null ? u.getJoinDate().toString() : null);
        obj.put(USER_DISPLAY_NAME, u.getDisplayName());
        obj.put(USER_ACCOUNT_ID, u.getAccountId());
        obj.put(USER_EMAIL, u.getEmail());
        obj.put(USER_USERNAME, u.getUsername());
        obj.put(USER_HASHED_PASSWORD, u.getHashedPassword());
        obj.put(USER_IS_LOCKED, u.isLocked());
        obj.put(USER_FAILED_LOGIN_COUNT, u.getFailedLoginCount());
        obj.put(USER_LAST_FAILED_LOGIN_AT, u.getLastFailedLoginAt() != null ? u.getLastFailedLoginAt().toString() : null);
        JSONArray achievementIds = new JSONArray();
        if (u.getAchievementIds() != null) {
            for (UUID id : u.getAchievementIds()) {
                achievementIds.add(id.toString());
            }
        }
        obj.put(USER_ACHIEVEMENT_IDS, achievementIds);
        obj.put(USER_STREAK, u.getStreak());
        obj.put(USER_LAST_ACTIVE_DATE, u.getLastActiveDate() != null ? u.getLastActiveDate().toString() : null);
        JSONArray favoriteIds = new JSONArray();
        if (u.getFavoriteProblems() != null) {
            for (Question q : u.getFavoriteProblems()) {
                favoriteIds.add(q.getId().toString());
            }
        }
        obj.put(USER_FAVORITE_PROBLEMS, favoriteIds);
        obj.put(USER_PROGRESS_TRACKER_ID, u.getProgressTrackerId() != null ? u.getProgressTrackerId().toString() : null);
        return obj;
    }

    /**
     * Turns a Question object into a JSON object for saving. Maps all the question fields to the JSON format.
     */
    @SuppressWarnings("unchecked")
    private JSONObject questionToJson(Question q) {
        JSONObject obj = new JSONObject();
        obj.put(QUESTION_ID, q.getId() != null ? q.getId().toString() : null);
        obj.put(QUESTION_TITLE, q.getTitle());
        obj.put(QUESTION_PROMPT, q.getPrompt());
        obj.put(QUESTION_DIFFICULTY, q.getDifficulty() != null ? q.getDifficulty().toString() : null);
        JSONArray topics = new JSONArray();
        if (q.getTopics() != null) {
            for (Object t : q.getTopics()) {
                topics.add(t != null ? t.toString() : null);
            }
        }
        obj.put(QUESTION_TOPICS, topics);
        JSONArray companyTags = new JSONArray();
        if (q.getCompanyTags() != null) {
            for (Object ct : q.getCompanyTags()) {
                companyTags.add(ct != null ? ct.toString() : null);
            }
        }
        obj.put(QUESTION_COMPANY_TAGS, companyTags);
        obj.put(QUESTION_HINTS, q.getHints() != null ? q.getHints() : new JSONArray());
        obj.put(QUESTION_CREATED_BY, q.getCreatedBy() != null ? q.getCreatedBy().toString() : null);
        obj.put(QUESTION_CREATED_AT, q.getCreatedAt() != null ? q.getCreatedAt().toString() : null);
        obj.put(QUESTION_STATUS, q.getStatus() != null ? q.getStatus().toString() : null);
        obj.put(QUESTION_VOTE_COUNT, q.getVoteCount());
        JSONArray solutions = new JSONArray();
        if (q.getSolutions() != null) {
            for (Solution s : q.getSolutions()) {
                if (s != null) {
                    solutions.add(solutionToJson(s));
                }
            }
        }
        obj.put(QUESTION_SOLUTIONS, solutions);
        obj.put(QUESTION_COMMENTS, new JSONArray());
        obj.put(QUESTION_ATTACHMENTS, new JSONArray());
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject solutionToJson(Solution s) {
        JSONObject obj = new JSONObject();
        obj.put(SOLUTION_ID, s.getId() != null ? s.getId().toString() : null);
        obj.put(SOLUTION_QUESTION_ID, s.getQuestionId() != null ? s.getQuestionId().toString() : null);
        obj.put(SOLUTION_AUTHOR_ID, s.getAuthorId() != null ? s.getAuthorId().toString() : null);
        obj.put(SOLUTION_CODE, s.getCode());
        obj.put(SOLUTION_LANGUAGE, s.getLanguage());
        obj.put(SOLUTION_EXPLANATION, s.getExplanation());
        obj.put(SOLUTION_CREATED_AT, s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
        obj.put(SOLUTION_UPDATED_AT, s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null);
        obj.put(SOLUTION_VOTE_COUNT, s.getVoteCount());
        return obj;
    }

    /**
     * Quick test - loads questions and saves them back. Just run this to check if saving works.
     */
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();
        ArrayList<Question> problems = loader.getProblems();
        DataWriter writer = new DataWriter();
        boolean ok = writer.saveProblem(problems);
        System.out.println("DataWriter test: saveProblem(problems) = " + ok);
    }
}
