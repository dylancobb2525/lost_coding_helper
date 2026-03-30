package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.model.enums.Topic;

/**
 * Tests DataWriter save/update/delete/favorites against fixture JSON files.
 */
public class DataWriterTest {

    private static final UUID USER_ALICE = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID Q_ONE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID Q_TWO = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private Path usersPath;
    private Path questionsPath;
    private String originalUsersContent;
    private String originalQuestionsContent;

    @Before
    public void setUp() throws IOException {
        usersPath = ensurePathExists(DataConstants.resolveDataPath(DataConstants.USER_FILE_NAME));
        questionsPath = ensurePathExists(DataConstants.resolveDataPath(DataConstants.QUESTION_FILE_NAME));
        originalUsersContent = Files.readString(usersPath, StandardCharsets.UTF_8);
        originalQuestionsContent = Files.readString(questionsPath, StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws IOException {
        Files.writeString(usersPath, originalUsersContent, StandardCharsets.UTF_8);
        Files.writeString(questionsPath, originalQuestionsContent, StandardCharsets.UTF_8);
    }

    @Test
    public void saveUsers_persistsUsersToFile() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        ArrayList<User> users = new ArrayList<>();
        users.add(new Contributor(USER_ALICE, "Alice", "ACC-1", "alice@example.com", "AliceUser", "Hash1aBc"));

        DataWriter writer = new DataWriter();
        assertTrue(writer.saveUsers(users));

        DataLoader loader = new DataLoader();
        ArrayList<User> loaded = loader.getUsers();
        assertEquals(1, loaded.size());
        assertEquals("AliceUser", loaded.get(0).getUsername());
        assertEquals("alice@example.com", loaded.get(0).getEmail());
    }

    @Test
    public void saveProblem_persistsQuestionsToFile() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        ArrayList<Topic> topics = new ArrayList<>();
        topics.add(Topic.OOP);
        Question q = new Question(
                Q_ONE,
                "Two Sum",
                "Return indices that sum to target.",
                "EASY",
                topics,
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.parse("2026-03-27T10:00:00"),
                "PUBLISHED");
        ArrayList<Question> list = new ArrayList<>();
        list.add(q);

        DataWriter writer = new DataWriter();
        assertTrue(writer.saveProblem(list));

        DataLoader loader = new DataLoader();
        ArrayList<Question> loaded = loader.getProblems();
        assertEquals(1, loaded.size());
        assertEquals("Two Sum", loaded.get(0).getTitle());
        assertEquals("EASY", loaded.get(0).getDifficulty());
    }

    @Test
    public void updateProblem_replacesQuestionWithSameId() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":["
                + "{\"id\":\"" + Q_ONE + "\",\"title\":\"Old\",\"prompt\":\"p\",\"difficulty\":\"EASY\","
                + "\"topics\":[],\"companyTags\":[],\"hints\":[],\"createdBy\":\"" + USER_ALICE
                + "\",\"createdAt\":\"2026-03-27T10:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":0},"
                + "{\"id\":\"" + Q_TWO + "\",\"title\":\"Keep\",\"prompt\":\"p2\",\"difficulty\":\"MEDIUM\","
                + "\"topics\":[],\"companyTags\":[],\"hints\":[],\"createdBy\":\"" + USER_ALICE
                + "\",\"createdAt\":\"2026-03-27T11:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":0}"
                + "]}");

        Question updated = new Question(
                Q_ONE,
                "New Title",
                "Updated prompt.",
                "HARD",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.parse("2026-03-27T12:00:00"),
                "DRAFT");

        DataWriter writer = new DataWriter();
        assertTrue(writer.updateProblem(updated));

        DataLoader loader = new DataLoader();
        ArrayList<Question> loaded = loader.getProblems();
        assertEquals(2, loaded.size());
        Question found = null;
        for (Question q : loaded) {
            if (Q_ONE.equals(q.getId())) {
                found = q;
                break;
            }
        }
        assertNotNull(found);
        assertEquals("New Title", found.getTitle());
        assertEquals("HARD", found.getDifficulty());
    }

    @Test
    public void updateProblem_returnsFalseWhenIdNotInFile() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[{\"id\":\"" + Q_ONE
                + "\",\"title\":\"Only\",\"prompt\":\"p\",\"difficulty\":\"EASY\",\"topics\":[],"
                + "\"companyTags\":[],\"hints\":[],\"createdBy\":\"" + USER_ALICE
                + "\",\"createdAt\":\"2026-03-27T10:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":0}]}");

        Question missing = new Question(
                Q_TWO,
                "Nope",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");

        DataWriter writer = new DataWriter();
        assertFalse(writer.updateProblem(missing));
    }

    @Test
    public void updateProblem_returnsFalseWhenProblemOrIdNull() {
        DataWriter writer = new DataWriter();
        Question noId = new Question();
        assertFalse(writer.updateProblem(null));
        assertFalse(writer.updateProblem(noId));
    }

    @Test
    public void deleteProblem_removesMatchingQuestion() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":["
                + "{\"id\":\"" + Q_ONE + "\",\"title\":\"A\",\"prompt\":\"p\",\"difficulty\":\"EASY\","
                + "\"topics\":[],\"companyTags\":[],\"hints\":[],\"createdBy\":\"" + USER_ALICE
                + "\",\"createdAt\":\"2026-03-27T10:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":0},"
                + "{\"id\":\"" + Q_TWO + "\",\"title\":\"B\",\"prompt\":\"p2\",\"difficulty\":\"EASY\","
                + "\"topics\":[],\"companyTags\":[],\"hints\":[],\"createdBy\":\"" + USER_ALICE
                + "\",\"createdAt\":\"2026-03-27T11:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":0}"
                + "]}");

        Question toRemove = new Question(
                Q_ONE,
                "A",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");

        DataWriter writer = new DataWriter();
        assertTrue(writer.deleteProblem(toRemove));

        DataLoader loader = new DataLoader();
        ArrayList<Question> loaded = loader.getProblems();
        assertEquals(1, loaded.size());
        assertEquals(Q_TWO, loaded.get(0).getId());
    }

    @Test
    public void deleteProblem_returnsFalseWhenNotFoundOrNull() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        DataWriter writer = new DataWriter();
        Question ghost = new Question(
                Q_ONE,
                "X",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");
        assertFalse(writer.deleteProblem(ghost));
        assertFalse(writer.deleteProblem(null));

        Question noId = new Question();
        assertFalse(writer.deleteProblem(noId));
    }

    @Test
    public void saveFavorites_writesFavoriteProblemIds() throws Exception {
        writeUsersJson("{\"users\":[{\"userId\":\"" + USER_ALICE
                + "\",\"username\":\"AliceUser\",\"email\":\"alice@example.com\",\"displayName\":\"Alice\","
                + "\"hashedPassword\":\"h\",\"accountId\":\"A1\",\"favoriteProblems\":[]}]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        Question fav1 = new Question(
                Q_ONE,
                "F1",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");
        Question fav2 = new Question(
                Q_TWO,
                "F2",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");
        ArrayList<Question> favorites = new ArrayList<>();
        favorites.add(fav1);
        favorites.add(fav2);

        DataWriter writer = new DataWriter();
        assertTrue(writer.saveFavorites(USER_ALICE, favorites));

        JSONObject root = (JSONObject) new JSONParser().parse(Files.readString(usersPath, StandardCharsets.UTF_8));
        JSONArray users = (JSONArray) root.get(DataConstants.USERS);
        JSONObject u = (JSONObject) users.get(0);
        JSONArray favIds = (JSONArray) u.get("favoriteProblems");
        assertEquals(2, favIds.size());
        assertEquals(Q_ONE.toString(), favIds.get(0).toString());
        assertEquals(Q_TWO.toString(), favIds.get(1).toString());
    }

    @Test
    public void saveUsers_writesMultiLineFormattedJson() throws Exception {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        ArrayList<User> users = new ArrayList<>();
        users.add(new Contributor(USER_ALICE, "Alice", "ACC-1", "alice@example.com", "AliceUser", "Hash1aBc"));

        DataWriter writer = new DataWriter();
        assertTrue(writer.saveUsers(users));

        String content = Files.readString(usersPath, StandardCharsets.UTF_8);
        assertTrue("Saved JSON should use multiple lines for readability", content.indexOf('\n') >= 0);
    }

    @Test
    public void saveFavorites_returnsFalseWhenUserIdNotInFile() throws Exception {
        writeUsersJson("{\"users\":[{\"userId\":\"" + USER_ALICE
                + "\",\"username\":\"AliceUser\",\"email\":\"a@a.com\",\"displayName\":\"A\","
                + "\"hashedPassword\":\"h\",\"accountId\":\"A1\"}]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        UUID other = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        ArrayList<Question> favorites = new ArrayList<>();
        favorites.add(sampleQuestion(Q_ONE));

        DataWriter writer = new DataWriter();
        assertFalse(writer.saveFavorites(other, favorites));
    }

    @Test
    public void saveFavorites_skipsNullAndNullIdQuestions() throws Exception {
        writeUsersJson("{\"users\":[{\"userId\":\"" + USER_ALICE
                + "\",\"username\":\"AliceUser\",\"email\":\"a@a.com\",\"displayName\":\"A\","
                + "\"hashedPassword\":\"h\",\"accountId\":\"A1\",\"favoriteProblems\":[]}]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        ArrayList<Question> favorites = new ArrayList<>();
        favorites.add(null);
        favorites.add(sampleQuestion(Q_ONE));
        Question noId = new Question();
        favorites.add(noId);

        DataWriter writer = new DataWriter();
        assertTrue(writer.saveFavorites(USER_ALICE, favorites));

        JSONObject root = (JSONObject) new JSONParser().parse(Files.readString(usersPath, StandardCharsets.UTF_8));
        JSONArray users = (JSONArray) root.get(DataConstants.USERS);
        JSONObject u = (JSONObject) users.get(0);
        JSONArray favIds = (JSONArray) u.get("favoriteProblems");
        assertEquals(1, favIds.size());
        assertEquals(Q_ONE.toString(), favIds.get(0).toString());
    }

    private static Question sampleQuestion(UUID id) {
        return new Question(
                id,
                "T",
                "p",
                "EASY",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                USER_ALICE,
                LocalDateTime.now(),
                "PUBLISHED");
    }

    private void writeUsersJson(String json) throws IOException {
        writeRaw(usersPath, json);
    }

    private void writeQuestionsJson(String json) throws IOException {
        writeRaw(questionsPath, json);
    }

    private void writeRaw(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private Path ensurePathExists(String resolvedPath) throws IOException {
        Path path = Paths.get(resolvedPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.writeString(path, "{}", StandardCharsets.UTF_8);
        }
        return path;
    }

    /*
     * Simple summary for our group / teacher:
     * This class tests DataWriter writing json. We try save users, save questions, update one
     * question by id, delete one question, and save favorite problem ids for a user. We also
     * test the cases where it should return false or skip bad data. Same backup idea as the
     * loader tests so we don't mess up the real json files.
     */
}
