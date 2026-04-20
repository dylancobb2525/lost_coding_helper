package com.model;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.model.enums.Topic;

/**
 * Loads application data from JSON files (users and questions).
 *
 * @author Christopher Feuchter
 */
public class DataLoader extends DataConstants {

    /**
     * Loads {@code users.json} into {@link User} instances.
     *
     * @return users, or an empty list if the file is missing or invalid
     */
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();

        try (FileReader reader = new FileReader(resolveDataPath(USER_FILE_NAME))) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray usersJSON = getJSONArray(jsonObject, USERS);
            if (usersJSON == null) {
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

                JSONArray achievementJson = getJSONArray(userJSON, USER_ACHIEVEMENT_IDS);
                if (achievementJson != null) {
                    user.getAchievementIds().clear();
                    for (Object o : achievementJson) {
                        UUID aid = parseUUID(o != null ? o.toString() : null);
                        if (aid != null) {
                            user.getAchievementIds().add(aid);
                        }
                    }
                }

                int streak = parseIntValue(userJSON.get(USER_STREAK));
                user.getProgressTracker().setStreak(Math.max(0, streak));
                Object lastActiveRaw = userJSON.get(USER_LAST_ACTIVE_DATE);
                user.getProgressTracker().setLastActiveDate(parseLocalDate(lastActiveRaw != null ? lastActiveRaw.toString() : null));

                Object compDayRaw = userJSON.get(USER_COMPLETIONS_DAY);
                int compToday = parseIntValue(userJSON.get(USER_COMPLETIONS_TODAY));
                user.getProgressTracker().restoreCompletionsForDay(
                        parseLocalDate(compDayRaw != null ? compDayRaw.toString() : null),
                        compToday);

                Object photoRaw = userJSON.get(USER_PROFILE_PHOTO_URI);
                if (photoRaw != null && !photoRaw.toString().isBlank()) {
                    user.setProfilePhotoUri(photoRaw.toString().trim());
                }

                user.getFavoritedProblemIds().clear();
                user.getFavoriteProblems().clear();
                JSONArray favJson = getJSONArray(userJSON, USER_FAVORITE_PROBLEMS);
                if (favJson != null) {
                    for (Object o : favJson) {
                        UUID qid = parseUUID(o != null ? o.toString() : null);
                        if (qid != null) {
                            user.getFavoritedProblemIds().add(qid);
                        }
                    }
                }

                user.getPendingCompletedProblemIds().clear();
                JSONArray completedJson = getJSONArray(userJSON, USER_COMPLETED_PROBLEMS);
                if (completedJson != null) {
                    for (Object o : completedJson) {
                        UUID qid = parseUUID(o != null ? o.toString() : null);
                        if (qid != null) {
                            user.getPendingCompletedProblemIds().add(qid);
                        }
                    }
                }

                users.add(user);
            }
        } catch (Exception e) {
            // Malformed JSON, I/O errors, or bad structure: return empty list (API contract).
        }

        return users;
    }

    /**
     * Loads {@code questions.json} into {@link Question} instances.
     *
     * @return questions, or an empty list if the file is missing or invalid
     */
    public ArrayList<Question> getProblems() {
        ArrayList<Question> questions = new ArrayList<>();

        try (FileReader reader = new FileReader(resolveDataPath(QUESTION_FILE_NAME))) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray questionsJSON = getJSONArray(jsonObject, QUESTIONS);
            if (questionsJSON == null) {
                return questions;
            }

            for (int i = 0; i < questionsJSON.size(); i++) {
                JSONObject questionJSON = (JSONObject) questionsJSON.get(i);
                UUID id = parseUUID((String) questionJSON.get(QUESTION_ID));
                if (id == null) id = UUID.randomUUID();
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
                JSONArray solutionsJson = getJSONArray(questionJSON, QUESTION_SOLUTIONS);
                for (Solution solution : parseSolutions(solutionsJson, id)) {
                    question.addSolution(solution);
                }
                questions.add(question);
            }
        } catch (Exception e) {
            // Malformed JSON, I/O errors, or bad structure: return empty list (API contract).
        }

        return questions;
    }

    /**
     * Returns the {@link JSONArray} at {@code key}, or null if missing or not an array.
     */
    private static JSONArray getJSONArray(JSONObject root, String key) {
        Object raw = root.get(key);
        return raw instanceof JSONArray ? (JSONArray) raw : null;
    }

    /**
     * Parses vote count from JSON (handles {@link Number} subtypes safely).
     *
     * @return a non-negative count, or 0 if invalid
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

    /**
     * Parses a UUID string, or returns null if null, empty, or invalid.
     */
    private static UUID parseUUID(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parses an  date-time string into {@link LocalDateTime}.
     */
    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseLocalDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String t = s.trim();
        int tIdx = t.indexOf('T');
        if (tIdx > 0) {
            t = t.substring(0, tIdx);
        }
        try {
            return LocalDate.parse(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseIntValue(Object raw) {
        if (raw == null) {
            return 0;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.parseInt(raw.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Converts a JSON array of topic names to {@link Topic} values and skips unrecognized names.
     */
    private static ArrayList<Topic> parseTopics(JSONArray arr) {
        ArrayList<Topic> list = new ArrayList<>();
        if (arr == null) return list;
        for (Object o : arr) {
            String s = String.valueOf(o);
            try {
                String normalized = s.replace("/", "").replace(" ", "_").toUpperCase();
                // "Algorithms/DataStructure" -> ALGORITHMSDATASTRUCTURE (note the "s" in Algorithms).
                if ("ALGORITHMSDATASTRUCTURE".equals(normalized) || "ALGORITHMDATASTRUCTURE".equals(normalized)) {
                    normalized = "ALGORITHMS_DATASTRUCTURE";
                }
                list.add(Topic.valueOf(normalized));
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    /**
     * Converts a JSON array to a list of strings.
     */
    private static ArrayList<String> parseStringList(JSONArray arr) {
        ArrayList<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (Object o : arr) {
            if (o != null) list.add(o.toString());
        }
        return list;
    }

    /**
     * Parses {@code solutions} JSON for one question. {@code authorId} {@code null} means a course-provided reference.
     */
    private static ArrayList<Solution> parseSolutions(JSONArray arr, UUID defaultQuestionId) {
        ArrayList<Solution> list = new ArrayList<>();
        if (arr == null) {
            return list;
        }
        for (Object o : arr) {
            if (!(o instanceof JSONObject)) {
                continue;
            }
            JSONObject sj = (JSONObject) o;
            UUID sid = parseUUID((String) sj.get(SOLUTION_ID));
            if (sid == null) {
                sid = UUID.randomUUID();
            }
            UUID qid = parseUUID((String) sj.get(SOLUTION_QUESTION_ID));
            if (qid == null) {
                qid = defaultQuestionId;
            }
            UUID authorId = parseUUID((String) sj.get(SOLUTION_AUTHOR_ID));
            String code = (String) sj.get(SOLUTION_CODE);
            String language = (String) sj.get(SOLUTION_LANGUAGE);
            String explanation = (String) sj.get(SOLUTION_EXPLANATION);
            LocalDateTime createdAt = parseDateTime((String) sj.get(SOLUTION_CREATED_AT));
            LocalDateTime updatedAt = parseDateTime((String) sj.get(SOLUTION_UPDATED_AT));
            int votes = parseVoteCount(sj.get(SOLUTION_VOTE_COUNT));
            list.add(new Solution(sid, qid, authorId, code, language, explanation, createdAt, updatedAt, votes));
        }
        return list;
    }

    /**
     * Prints loaded user and question counts (manual smoke test).
     *
     * @param args unused
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
