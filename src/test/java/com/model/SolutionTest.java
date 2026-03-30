package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SolutionTest {

    private Solution solution;

    @Before
    public void setUp() {
        solution = new Solution();
    }

    @Test
    /*
     * Title: Solution
     * Test | Reasoning
     * | default constructor basics | id exists vote count zero comments empty |
     */
    public void defaultConstructor_initializesIdVotesAndComments() {
        assertNotNull(solution.getId());
        assertEquals(0, solution.getVoteCount());
        assertTrue(solution.getComments().isEmpty());
        assertFalse(solution.getIsCorrect());
    }

    @Test
    /*
     * Title: Solution
     * Test | Reasoning
     * | two default  | each get its own random id |
     */
    public void defaultConstructor_distinctInstancesHaveDistinctIds() {
        Solution other = new Solution();
        assertNotEquals(solution.getId(), other.getId());
    }

    @Test
    /*
     * Title: Solution
     * Test | Reasoning
     * | full constructor stores fields | check if stored |
     */
    public void fullConstructor_storesProvidedFields() {
        UUID id = UUID.randomUUID();
        UUID qid = UUID.randomUUID();
        UUID aid = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.of(2024, 1, 2, 3, 4);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 3, 5, 6);
        Solution s = new Solution(id, qid, aid, "code", "Java", "why", created, updated, 7);
        assertEquals(id, s.getId());
        assertEquals(qid, s.getQuestionId());
        assertEquals(aid, s.getAuthorId());
        assertEquals("code", s.getCode());
        assertEquals("Java", s.getLanguage());
        assertEquals("why", s.getExplanation());
        assertEquals(created, s.getCreatedAt());
        assertEquals(updated, s.getUpdatedAt());
        assertEquals(7, s.getVoteCount());
        assertTrue(s.getComments().isEmpty());
    }

    @Test
    /*
     * Title: Solution
     * Test | Reasoning
     * | null id  | generates a non-null id instead |
     */
    public void fullConstructor_nullId_generatesRandomId() {
        Solution s = new Solution(
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "c",
            "Py",
            "e",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        assertNotNull(s.getId());
    }

    @Test
    /*
     * Title: edit
     * Test | Reasoning
     * | update code only |  unchanged code replaced |
     */
    public void edit_codeOnly_updatesCodeLeavesExplanation() {
        Solution s = new Solution(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "oldCode",
            "Java",
            "oldExp",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        s.edit("newCode", null);
        assertEquals("newCode", s.getCode());
        assertEquals("oldExp", s.getExplanation());
    }

    @Test
    /*
     * Title: edit
     * Test | Reasoning
     * | both args null | fields unchanged  |
     */
    public void edit_bothNull_refreshesUpdatedAt() throws InterruptedException {
        Solution s = new Solution(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "c",
            "C",
            "e",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        LocalDateTime before = s.getUpdatedAt();
        Thread.sleep(15);
        s.edit(null, null);
        assertEquals("c", s.getCode());
        assertEquals("e", s.getExplanation());
        assertTrue(s.getUpdatedAt().isAfter(before));
    }

    @Test
    /*
     * Title: addComment
     * Test | Reasoning
     * | basic comment | stored in list |
     */
    public void addComment_valid_addsToList() {
        solution.addComment("nice solution");
        assertEquals(1, solution.getComments().size());
        assertEquals("nice solution", solution.getComments().get(0));
    }

    @Test
    /*
     * Title: addComment
     * Test | Reasoning
     * | null comment | ignored |
     */
    public void addComment_null_doesNotAdd() {
        solution.addComment(null);
        assertTrue(solution.getComments().isEmpty());
    }
}
