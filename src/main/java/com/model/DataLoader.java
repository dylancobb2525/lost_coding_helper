package com.model;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.model.enums.Topic;

/**
 * Reads users and questions from the JSON files so the app can start up with real data.
 */
public class DataLoader extends DataConstants {

    /**
     * Reads users.json and builds User objects. If the file is missing or broken you get an empty list.
     * @return list of users (empty if something went wrong)
     */
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        
        try {
            FileReader reader = new FileReader(resolveDataPath(USER_FILE_NAME));
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray usersJSON = getJSONArray(jsonObject, USERS);
            if (usersJSON == null) {
                reader.close();
                return users;
            }

            for (int i = 0; i < usersJSON.size(); i++) {
                JSONObject userJSON = (JSONObject) usersJSON.get(i);
                UUID userId = parseUUID((String) userJSON.get(USER_ID));
                if (userId == null) userId = UUID.randomUUID();
                String username = (String) userJSON.get(USER_USERNAME);
                String email = (String) userJSON.get(USER_EMAIL);
                String displayName = (String) userJSON.get(USER_DISPLAY_NAME);
                String hashedPassword = (String) userJSON.get(USER_HASHED_PASSWORD);
                String accountId = (String) userJSON.get(USER_ACCOUNT_ID);
                User user = new Contributor(userId, displayName, accountId, email, username, hashedPassword);
                users.add(user);
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * Reads questions.json and builds Question objects (title, prompt, topics, etc.).
     * @return list of questions (empty if something went wrong)
     */
    public ArrayList<Question> getProblems() {
        ArrayList<Question> questions = new ArrayList<>();
        
        try {
            FileReader reader = new FileReader(resolveDataPath(QUESTION_FILE_NAME));
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray questionsJSON = getJSONArray(jsonObject, QUESTIONS);
            if (questionsJSON == null) {
                reader.close();
                return questions;
            }

            for (int i = 0; i < questionsJSON.size(); i++) {
                JSONObject questionJSON = (JSONObject) questionsJSON.get(i);
                UUID id = parseUUID((String) questionJSON.get(QUESTION_ID));
                String title = (String) questionJSON.get(QUESTION_TITLE);
                String prompt = (String) questionJSON.get(QUESTION_PROMPT);
                String difficulty = (String) questionJSON.get(QUESTION_DIFFICULTY);
                ArrayList<Topic> topicsList = parseTopics((JSONArray) questionJSON.get(QUESTION_TOPICS));
                ArrayList<String> companyTagsList = parseStringList((JSONArray) questionJSON.get(QUESTION_COMPANY_TAGS));
                ArrayList<String> hintsList = parseStringList((JSONArray) questionJSON.get(QUESTION_HINTS));
                UUID createdBy = parseUUID((String) questionJSON.get(QUESTION_CREATED_BY));
                LocalDateTime createdAt = parseDateTime((String) questionJSON.get(QUESTION_CREATED_AT));
                String status = (String) questionJSON.get(QUESTION_STATUS);
                int voteCount = parseVoteCount(questionJSON.get(QUESTION_VOTE_COUNT));

                Question question = new Question(id, title, prompt, difficulty, topicsList, companyTagsList,
                        hintsList, createdBy, createdAt, status);
                if (voteCount > 0) {
                    for (int v = 0; v < voteCount; v++) {
                        question.upvote(null);
                    }
                }
                questions.add(question);
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return questions;
    }

    /** @return the array at key, or null if missing or wrong type (avoids NPE / ClassCastException). */
    private static JSONArray getJSONArray(JSONObject root, String key) {
        Object raw = root.get(key);
        return raw instanceof JSONArray ? (JSONArray) raw : null;
    }

    /**
     * JSON may store voteCount as Long, Integer, or other Number; plain cast to Long fails on Integer.
     */
    private static int parseVoteCount(Object raw) {
        if (raw == null) {
            return 0;
        }
        if (raw instanceof Number) {
            return Math.max(0, ((Number) raw).intValue());
        }
        try {
            return Math.max(0, Integer.parseInt(raw.toString().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static UUID parseUUID(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Parses a date-time string from JSON into LocalDateTime. */
    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    /** Turns a JSON array of topic strings into Topic enums (skips ones we do not recognize). */
    private static ArrayList<Topic> parseTopics(JSONArray arr) {
        ArrayList<Topic> list = new ArrayList<>();
        if (arr == null) return list;
        for (Object o : arr) {
            String s = String.valueOf(o);
            try {
                String normalized = s.replace("/", "").replace(" ", "_").toUpperCase();
                if (normalized.equals("ALGORITHMDATASTRUCTURE")) normalized = "ALGORITHMS_DATASTRUCTURE";
                list.add(Topic.valueOf(normalized));
            } catch (Exception ignored) {
                // skip unknown topic
            }
        }
        return list;
    }

    /** Converts a JSON array into a list of strings. */
    private static ArrayList<String> parseStringList(JSONArray arr) {
        ArrayList<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (Object o : arr) {
            if (o != null) list.add(o.toString());
        }
        return list;
    }

    /**
     * Test main. Run this to see if loading works. Prints how many users and problems were loaded.
     */
    public static void main(String[] args) {
        DataLoader loader = new DataLoader();
        ArrayList<User> users = loader.getUsers();
        ArrayList<Question> problems = loader.getProblems();
        System.out.println("DataLoader test:");
        System.out.println("  Loaded " + users.size() + " user(s).");
        System.out.println("  Loaded " + problems.size() + " problem(s).");
        if (!problems.isEmpty()) {
            Question first = problems.get(0);
            System.out.println("  First problem title: " + first.getTitle());
        }
    }
}
