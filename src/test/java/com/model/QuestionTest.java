package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.model.enums.Topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class QuestionTest {

    private Question question;

    @Before
    public void setUp() {
        question = new Question();
    }

    /** Full constructor initializes hints; default {@link Question()} does not. */
    private Question newQuestionWithHintsList() {
        return new Question(
            UUID.randomUUID(),
            "Title",
            "Prompt",
            "EASY",
            new ArrayList<Topic>(),
            new ArrayList<String>(),
            new ArrayList<String>(),
            UUID.randomUUID(),
            LocalDateTime.now(),
            "DRAFT"
        );
    }

    @Test
    /*
     * Title: addSolution 
     * Test | Reasoning
     * | just tests the very most bassic solution | it adds the basic solution to the question.  |
     */
    public void addSolution_withValidSolution() {
        Solution solution = new Solution();
        question.addSolution(solution);
        assertEquals(1, question.getSolutions().size());
    }

    @Test
    /*
     * Title: addSolution 
     * Test | Reasoning
     * | tests when the solution is null | a solution cannot be null, it needs substance. |
     */
    public void addSolution_withNullSolution() {
        question.addSolution(null);
        assertTrue(question.getSolutions().isEmpty());
    }

    @Test
    /*
     * Title: addComment
     * Test | Reasoning
     * | sees if basic comment can be added | basic check |
     */
    public void addComment_withValidComment_addsToCommentsList() {
        String comment = "This is a valid comment";
        UUID qid = UUID.randomUUID();
        question.addComment(new Comment(UUID.randomUUID(), comment, qid, null));
        assertEquals(1, question.getComments().size());
        assertEquals(comment, question.getComments().get(0).getBody());
    }

    @Test
    /*
     * Title: addComment
     * Test | Reasoning
     * | checks null comment | a comment cannot be null |
     */
    public void addComment_withNullComment_doesNotAddToList() {
        question.addComment(null);
        assertTrue(question.getComments().isEmpty());
    }

    @Test
    /*
     * Title: setPublicStatus
     * Test | Reasoning
     * | true sets published | status string becomes PUBLISHED |
     */
    public void setPublicStatus_true_setsPublished() {
        question.setPublicStatus(true);
        assertEquals("PUBLISHED", question.getStatus());
        assertTrue(question.getPublicStatus());
    }

    @Test
    /*
     * Title: setPublicStatus
     * Test | Reasoning
     * | false sets draft | status string becomes DRAFT |
     */
    public void setPublicStatus_false_setsDraft() {
        question.setPublicStatus(false);
        assertEquals("DRAFT", question.getStatus());
        assertFalse(question.getPublicStatus());
    }

    @Test
    /*
     * Title: updateQuestion
     * Test | Reasoning
     * | copy fields from another question | title and prompt match source |
     */
    public void updateQuestion_withValidSource_copiesFields() {
        Question base = newQuestionWithHintsList();
        Question source = new Question(
            UUID.randomUUID(),
            "NewTitle",
            "NewPrompt",
            "HARD",
            new ArrayList<Topic>(),
            new ArrayList<String>(),
            new ArrayList<String>(),
            UUID.randomUUID(),
            LocalDateTime.now(),
            "PUBLISHED"
        );
        assertTrue(base.updateQuestion(source));
        assertEquals("NewTitle", base.getTitle());
        assertEquals("NewPrompt", base.getPrompt());
        assertEquals("HARD", base.getDifficulty());
        assertEquals("PUBLISHED", base.getStatus());
    }

    @Test
    /*
     * Title: updateQuestion
     * Test | Reasoning
     * | null argument | method returns false  |
     */
    public void updateQuestion_withNull_returnsFalse() {
        Question base = newQuestionWithHintsList();
        assertFalse(base.updateQuestion(null));
    }

    @Test
    /*
     * Title: deleteQuestion
     * Test | Reasoning
     * | basic call | check basic works |
     */
    public void deleteQuestion_returnsTrue() {
        assertTrue(question.deleteQuestion());
    }

    @Test
    /*
     * Title: deleteQuestion
     * Test | Reasoning
     * | on question with data | check if it works when there is data |
     */
    public void deleteQuestion_onPopulatedQuestion_returnsTrue() {
        question.addComment(new Comment(UUID.randomUUID(), "c", UUID.randomUUID(), null));
        question.addAttachment("a.txt");
        assertTrue(question.deleteQuestion());
    }

    @Test
    /*
     * Title: addCodeSnippet
     * Test | Reasoning
     * | add one snippet | check if it works |
     */
    public void addCodeSnippet_basic_addsToHints() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet("int x = 1;");
        assertEquals(1, q.getHints().size());
        assertEquals("int x = 1;", q.getHints().get(0));
    }

    @Test
    /*
     * Title: addCodeSnippet
     * Test | Reasoning
     * | null snippet | nothing added |
     */
    public void addCodeSnippet_null_doesNotAdd() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet(null);
        assertTrue(q.getHints().isEmpty());
    }

    @Test
    /*
     * Title: updateCodeSnippet
     * Test | Reasoning
     * | valid index |  text replaced |
     */
    public void updateCodeSnippet_validIndex_replacesHint() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet("old");
        q.updateCodeSnippet(0, "new");
        assertEquals("new", q.getHints().get(0));
    }

    @Test
    /*
     * Title: updateCodeSnippet
     * Test | Reasoning
     * | null new snippet | existing hint unchanged |
     */
    public void updateCodeSnippet_nullNewSnippet_noChange() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet("keep");
        q.updateCodeSnippet(0, null);
        assertEquals("keep", q.getHints().get(0));
    }

    @Test
    /*
     * Title: deleteCodeSnippet
     * Test | Reasoning
     * | delete first of two | second remains at index zero |
     */
    public void deleteCodeSnippet_validIndex_removesHint() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet("first");
        q.addCodeSnippet("second");
        q.deleteCodeSnippet(0);
        assertEquals(1, q.getHints().size());
        assertEquals("second", q.getHints().get(0));
    }

    @Test
    /*
     * Title: deleteCodeSnippet
     * Test | Reasoning
     * | index out of range | list unchanged |
     */
    public void deleteCodeSnippet_indexTooLarge_noChange() {
        Question q = newQuestionWithHintsList();
        q.addCodeSnippet("one");
        q.deleteCodeSnippet(5);
        assertEquals(1, q.getHints().size());
    }

    @Test
    /*
     * Title: addAttachment
     * Test | Reasoning
     * | basic path string | attachment stored |
     */
    public void addAttachment_basic_addsToList() {
        question.addAttachment("notes.pdf");
        assertEquals(1, question.getAttachments().size());
        assertEquals("notes.pdf", question.getAttachments().get(0));
    }

    @Test
    /*
     * Title: addAttachment
     * Test | Reasoning
     * | null attachment | ignored |
     */
    public void addAttachment_null_doesNotAdd() {
        question.addAttachment(null);
        assertTrue(question.getAttachments().isEmpty());
    }

    @Test
    /*
     * Title: updateAttachment
     * Test | Reasoning
     * | valid index | value replaced |
     */
    public void updateAttachment_validIndex_replacesValue() {
        question.addAttachment("old.txt");
        question.updateAttachment(0, "new.txt");
        assertEquals("new.txt", question.getAttachments().get(0));
    }

    @Test
    /*
     * Title: updateAttachment
     * Test | Reasoning
     * | null new attachment | original kept |
     */
    public void updateAttachment_nullNewValue_noChange() {
        question.addAttachment("same.txt");
        question.updateAttachment(0, null);
        assertEquals("same.txt", question.getAttachments().get(0));
    }

    @Test
    /*
     * Title: deleteAttachment
     * Test | Reasoning
     * | remove first | second shifts down |
     */
    public void deleteAttachment_validIndex_removesItem() {
        question.addAttachment("x");
        question.addAttachment("y");
        question.deleteAttachment(0);
        assertEquals(1, question.getAttachments().size());
        assertEquals("y", question.getAttachments().get(0));
    }

    @Test
    /*
     * Title: deleteAttachment
     * Test | Reasoning
     * | delete sole attachment | list empty |
     */
    public void deleteAttachment_onlyItem_leavesEmpty() {
        question.addAttachment("solo.dat");
        question.deleteAttachment(0);
        assertTrue(question.getAttachments().isEmpty());
    }

}