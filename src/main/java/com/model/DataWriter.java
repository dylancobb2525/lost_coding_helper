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
 * Writes users and questions to json files. Used for save and update.
 */
public class DataWriter extends DataConstants {

    /**
     * Save users to the users json file. This is the Save users json task.
     * @param users the list to save
     * @return true if it worked
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
     * Saves the list of questions to the questions json file. Overwrites the questions in the file.
     * @param problems the list to save
     * @return true if it worked
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
     * Updates one question in the questions json file. Finds it by id and replaces it.
     * @param problem the question with the new data (same id as the one to update)
     * @return true if it found the question and wrote the file
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
     * Deletes one question from the questions json file. Finds it by id and removes it.
     * @param problem the question to delete (only the id is used to find it)
     * @return true if it found the question and wrote the file
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
     * Saves a user's favorite problems list to the users json file.
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

    private JSONObject readJson(String path) {
        try (FileReader fr = new FileReader(path)) {
            Object parsed = new JSONParser().parse(fr);
            return (JSONObject) parsed;
        } catch (IOException | ParseException e) {
            return null;
        }
    }

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
        obj.put(QUESTION_SOLUTIONS, new JSONArray());
        obj.put(QUESTION_COMMENTS, new JSONArray());
        obj.put(QUESTION_ATTACHMENTS, new JSONArray());
        return obj;
    }

    /**
     * Test main. Loads questions then saves them. Prints true if save worked.
     */
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();
        ArrayList<Question> problems = loader.getProblems();
        DataWriter writer = new DataWriter();
        boolean ok = writer.saveProblem(problems);
        System.out.println("DataWriter test: saveProblem(problems) = " + ok);
    }
}
