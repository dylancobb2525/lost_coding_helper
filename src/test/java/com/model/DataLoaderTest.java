package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.model.enums.Topic;

/**
 * Tests DataLoader with fixture JSON and error cases.
 */
public class DataLoaderTest {
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
    public void getUsers_readsFixtureUsers() throws IOException {
        writeUsersJson("{\"users\":[{\"userId\":\"11111111-1111-1111-1111-111111111111\",\"username\":\"AlphaUser\",\"email\":\"alpha@example.com\",\"displayName\":\"Alpha\",\"hashedPassword\":\"hash1\",\"accountId\":\"PUB-001\"},{\"userId\":\"22222222-2222-2222-2222-222222222222\",\"username\":\"BetaUser\",\"email\":\"beta@example.com\",\"displayName\":\"Beta\",\"hashedPassword\":\"hash2\",\"accountId\":\"PUB-002\"}]}");
        writeQuestionsJson("{\"languages\":[],\"questions\":[]}");

        DataLoader loader = new DataLoader();
        ArrayList<User> users = loader.getUsers();

        assertEquals(2, users.size());
        assertEquals("AlphaUser", users.get(0).getUsername());
        assertEquals("beta@example.com", users.get(1).getEmail());
    }

    @Test
    public void getProblems_readsFixtureQuestionAndParsesFields() throws IOException {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"questions\":[{\"id\":\"33333333-3333-3333-3333-333333333333\",\"title\":\"BST Check\",\"prompt\":\"Check if tree is valid BST\",\"difficulty\":\"MEDIUM\",\"topics\":[\"Algorithms/DataStructure\"],\"companyTags\":[\"Google\"],\"hints\":[\"Use inorder traversal\"],\"createdBy\":\"11111111-1111-1111-1111-111111111111\",\"createdAt\":\"2026-03-26T12:00:00\",\"status\":\"PUBLISHED\",\"voteCount\":3}]}");

        DataLoader loader = new DataLoader();
        ArrayList<Question> questions = loader.getProblems();

        assertEquals(1, questions.size());
        Question q = questions.get(0);
        assertEquals("BST Check", q.getTitle());
        assertEquals("MEDIUM", q.getDifficulty());
        assertEquals(1, q.getTopics().size());
        assertEquals(Topic.ALGORITHMS_DATASTRUCTURE, q.getTopics().get(0));
        assertEquals(3, q.getVoteCount());
    }

    @Test
    public void getProblems_handlesMissingQuestionsArray() throws IOException {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"languages\":[]}");

        DataLoader loader = new DataLoader();
        ArrayList<Question> questions = loader.getProblems();

        assertTrue(questions.isEmpty());
    }

    @Test
    public void getUsers_handlesMalformedJson() throws IOException {
        writeRaw(usersPath, "{\"users\":[{bad json");
        writeQuestionsJson("{\"questions\":[]}");

        DataLoader loader = new DataLoader();
        ArrayList<User> users = loader.getUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void getProblems_handlesBadVoteAndBadTopicGracefully() throws IOException {
        writeUsersJson("{\"users\":[]}");
        writeQuestionsJson("{\"questions\":[{\"id\":\"44444444-4444-4444-4444-444444444444\",\"title\":\"X\",\"prompt\":\"Y\",\"difficulty\":\"EASY\",\"topics\":[\"NotARealTopic\"],\"companyTags\":[],\"hints\":[],\"createdBy\":\"11111111-1111-1111-1111-111111111111\",\"createdAt\":\"bad-date\",\"status\":\"DRAFT\",\"voteCount\":\"not-a-number\"}]}");

        DataLoader loader = new DataLoader();
        ArrayList<Question> questions = loader.getProblems();

        assertEquals(1, questions.size());
        assertTrue(questions.get(0).getTopics().isEmpty());
        assertEquals(0, questions.get(0).getVoteCount());
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
}
