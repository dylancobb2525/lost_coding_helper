package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class QuestionListTest {

    private QuestionList list;

    @Before
    public void setUp() {
        list = new QuestionList();
    }

    private static class RecordingDataWriter extends DataWriter {
        boolean saveResult = true;
        boolean updateResult = true;
        boolean deleteResult = true;
        int saveCalls;
        int updateCalls;
        int deleteCalls;
        ArrayList<Question> lastSaveList;
        Question lastUpdateQuestion;
        Question lastDeleteQuestion;

        @Override
        public boolean saveProblem(ArrayList<Question> problems) {
            saveCalls++;
            lastSaveList = problems;
            return saveResult;
        }

        @Override
        public boolean updateProblem(Question problem) {
            updateCalls++;
            lastUpdateQuestion = problem;
            return updateResult;
        }

        @Override
        public boolean deleteProblem(Question problem) {
            deleteCalls++;
            lastDeleteQuestion = problem;
            return deleteResult;
        }
    }

    private Question newQuestion(UUID id, String title, String difficulty) {
        return new Question(
            id,
            title,
            "prompt",
            difficulty,
            new ArrayList<Topic>(),
            new ArrayList<String>(),
            new ArrayList<String>(),
            UUID.randomUUID(),
            LocalDateTime.now(),
            "DRAFT"
        );
    }

    private Question newQuestionWithJavaSolution(UUID id, String title, String difficulty) {
        Question q = newQuestion(id, title, difficulty);
        Solution s = new Solution(
            UUID.randomUUID(),
            id,
            UUID.randomUUID(),
            "code",
            "Java",
            "explanation",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        q.addSolution(s);
        return q;
    }

    @Test
    /*
     * Title: QuestionList
     * Test | Reasoning
     * | new list empty | getAll starts with no questions |
     */
    public void newList_getAll_isEmpty() {
        assertTrue(list.getAll().isEmpty());
    }

    @Test
    /*
     * Title: addQuestion
     * Test | Reasoning
     * | add non-null | list grows |
     */
    public void addQuestion_valid_addsToList() {
        Question q = newQuestion(UUID.randomUUID(), "T", "EASY");
        list.addQuestion(q);
        assertEquals(1, list.getAll().size());
        assertSame(q, list.getAll().get(0));
    }

    @Test
    /*
     * Title: addQuestion
     * Test | Reasoning
     * | null ignored | size stays zero |
     */
    public void addQuestion_null_doesNotAdd() {
        list.addQuestion(null);
        assertTrue(list.getAll().isEmpty());
    }

    @Test
    /*
     * Title: getById
     * Test | Reasoning
     * | null id | returns null |
     */
    public void getById_null_returnsNull() {
        list.addQuestion(newQuestion(UUID.randomUUID(), "x", "EASY"));
        assertNull(list.getById(null));
    }

    @Test
    /*
     * Title: getById
     * Test | Reasoning
     * | matching id | returns same question |
     */
    public void getById_found_returnsQuestion() {
        UUID id = UUID.randomUUID();
        Question q = newQuestion(id, "FindMe", "MEDIUM");
        list.addQuestion(q);
        assertSame(q, list.getById(id));
    }

    @Test
    /*
     * Title: search
     * Test | Reasoning
     * | null or empty query | empty results |
     */
    public void search_nullOrEmpty_returnsEmpty() {
        list.addQuestion(newQuestion(UUID.randomUUID(), "Hello", "EASY"));
        assertTrue(list.search(null).isEmpty());
        assertTrue(list.search("").isEmpty());
    }

    @Test
    /*
     * Title: search
     * Test | Reasoning
     * | substring match case insensitive | question included |
     */
    public void search_caseInsensitive_findsTitle() {
        Question q = newQuestion(UUID.randomUUID(), "Binary Search Tree", "HARD");
        list.addQuestion(q);
        ArrayList<Question> hits = list.search("binary");
        assertEquals(1, hits.size());
        assertSame(q, hits.get(0));
    }

    @Test
    /*
     * Title: search
     * Test | Reasoning
     * | null title on question | skipped no false match |
     */
    public void search_nullTitle_skipped() {
        Question noTitle = new Question();
        noTitle.addComment(new Comment(UUID.randomUUID(), "only", UUID.randomUUID(), null));
        list.addQuestion(noTitle);
        assertTrue(list.search("anything").isEmpty());
    }

    @Test
    /*
     * Title: setDataWriter
     * Test | Reasoning
     * | see if set save works | writer receives list |
     */
    public void setDataWriter_save_delegatesToWriter() {
        RecordingDataWriter writer = new RecordingDataWriter();
        list.setDataWriter(writer);
        list.addQuestion(newQuestion(UUID.randomUUID(), "x", "EASY"));
        assertTrue(list.save());
        assertEquals(1, writer.saveCalls);
        assertSame(list.getAll(), writer.lastSaveList);
    }

    @Test
    /*
     * Title: save
     * Test | Reasoning
     * | no writer | returns false |
     */
    public void save_withoutDataWriter_returnsFalse() {
        assertFalse(list.save());
    }

    @Test
    /*
     * Title: updateQuestion
     * Test | Reasoning
     * | bad input | false  |
     */
    public void updateQuestion_whenInvalid_returnsFalse() {
        RecordingDataWriter writer = new RecordingDataWriter();
        list.setDataWriter(writer);
        assertFalse(list.updateQuestion(null));

        Question noId = new Question();
        noId.addComment(new Comment(UUID.randomUUID(), "no id", UUID.randomUUID(), null));
        assertFalse(list.updateQuestion(noId));

        list.setDataWriter(null);
        UUID id = UUID.randomUUID();
        list.addQuestion(newQuestion(id, "Old", "EASY"));
        assertFalse(list.updateQuestion(newQuestion(id, "New", "HARD")));

        list.setDataWriter(writer);
        assertFalse(list.updateQuestion(newQuestion(UUID.randomUUID(), "Ghost", "EASY")));
        assertEquals(0, writer.updateCalls);
    }

    @Test
    /*
     * Title: updateQuestion
     * Test | Reasoning
     * | valid update | check if it works |
     */
    public void updateQuestion_success_updatesInMemoryAndCallsWriter() {
        RecordingDataWriter writer = new RecordingDataWriter();
        list.setDataWriter(writer);
        UUID id = UUID.randomUUID();
        Question existing = newQuestion(id, "OldTitle", "EASY");
        list.addQuestion(existing);
        Question patch = newQuestion(id, "NewTitle", "HARD");
        assertTrue(list.updateQuestion(patch));
        assertEquals("NewTitle", existing.getTitle());
        assertEquals("HARD", existing.getDifficulty());
        assertEquals(1, writer.updateCalls);
        assertSame(existing, writer.lastUpdateQuestion);
    }

    @Test
    /*
     * Title: deleteQuestion
     * Test | Reasoning
     * | null wrong list state  |  list unchanged when delete did not run |
     */
    public void deleteQuestion_whenInvalid_returnsFalse() {
        RecordingDataWriter writer = new RecordingDataWriter();
        list.setDataWriter(writer);
        assertFalse(list.deleteQuestion(null));

        Question q = newQuestion(UUID.randomUUID(), "x", "EASY");
        list.addQuestion(q);
        list.setDataWriter(null);
        assertFalse(list.deleteQuestion(q));
        assertEquals(1, list.getAll().size());

        list.setDataWriter(writer);
        assertFalse(list.deleteQuestion(newQuestion(UUID.randomUUID(), "y", "EASY")));
        assertEquals(1, list.getAll().size());
    }

    @Test
    /*
     * Title: deleteQuestion
     * Test | Reasoning
     * | valid remove | list shrinks writer called |
     */
    public void deleteQuestion_success_removesAndCallsWriter() {
        RecordingDataWriter writer = new RecordingDataWriter();
        list.setDataWriter(writer);
        Question q = newQuestion(UUID.randomUUID(), "x", "EASY");
        list.addQuestion(q);
        assertTrue(list.deleteQuestion(q));
        assertTrue(list.getAll().isEmpty());
        assertEquals(1, writer.deleteCalls);
        assertSame(q, writer.lastDeleteQuestion);
    }

    @Test
    /*
     * Title: getByLanguageAndDifficulty
     * Test | Reasoning
     * | null language , empty difficulty | empty result |
     */
    public void getByLanguageAndDifficulty_invalidArgs_returnsEmpty() {
        list.addQuestion(newQuestionWithJavaSolution(UUID.randomUUID(), "Q", "easy"));
        assertTrue(list.getByLanguageAndDifficulty(null, "easy").isEmpty());
        assertTrue(list.getByLanguageAndDifficulty("Java", "").isEmpty());
    }

    @Test
    /*
     * Title: getByLanguageAndDifficulty
     * Test | Reasoning
     * | matching pair | only questions with that language and difficulty |
     */
    public void getByLanguageAndDifficulty_matchExcludesOtherDifficulty() {
        Question easyJava = newQuestionWithJavaSolution(UUID.randomUUID(), "Algo", "easy");
        Question hardJava = newQuestionWithJavaSolution(UUID.randomUUID(), "HardQ", "hard");
        list.addQuestion(easyJava);
        list.addQuestion(hardJava);
        ArrayList<Question> out = list.getByLanguageAndDifficulty("java", "easy");
        assertEquals(1, out.size());
        assertSame(easyJava, out.get(0));
    }
}
