package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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
     * Title: addSolution 
     * Test | Reasoning
     * | adds a solution with all data | to see if it takes an other solution other than the basic one |
     */
    public void addSolution_withFullyPopulatedSolution() {
        Solution realSolution = new Solution(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "for(int i=0;i<n;i++)",
            "Java",
            "iterate through array",
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        question.addSolution(realSolution);
        assertSame(realSolution, question.getSolutions().get(0));
        assertEquals("Java", question.getSolutions().get(0).getLanguage());
    }

    @Test
    /*
     * Title: addSolution 
     * Test | Reasoning
     * |if question will take multiple solutions | does it show all of the solutions|
     */
    public void addSolution_withMultipleSolutions() {
        Solution s1 = new Solution();
        Solution s2 = new Solution();
        Solution s3 = new Solution();
        question.addSolution(s1);
        question.addSolution(s2);
        question.addSolution(s3);
        assertEquals(3, question.getSolutions().size());
    }

    @Test
    /*
     * Title: addSolution 
     * Test | Reasoning
     * | see if the same solution can get added twice | it doesn't black the same solution from being added |
     */
    public void addSolution_withDuplicateSolution() {
        Solution solution = new Solution();
        question.addSolution(solution);
        question.addSolution(solution);
        assertEquals(2, question.getSolutions().size());
    }

    @Test
    /*
     * Title: addComment
     * Test | Reasoning
     * | sees if basic comment can be added | basic check |
     */
    public void addComment_withValidComment_addsToCommentsList() {
        String comment = "This is a valid comment";
        question.addComment(comment);
        assertEquals(1, question.getComments().size());
        assertEquals(comment, question.getComments().get(0));
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
     * Title: addComment
     * Test | Reasoning
     * | special characters comment | sees the extent of the comment String. |
     */
    public void addComment_withSpecialCharacters_commentStoredCorrectly() {
        String comment = "@tag! $100.50? (note) [brackets] {curly} \"quotes\" /slashes/";
        question.addComment(comment);
        assertEquals(1, question.getComments().size());
        assertEquals(comment, question.getComments().get(0));
    }

    @Test
    /*
     * Title: upvote
     * Test | Reasoning
     * | upvote once from zero | makes sure one upvote sets vote count to 1 |
     */
    public void upvote_fromZero_setsVoteCountToOne() {
        question.upvote(UUID.randomUUID());
        assertEquals(1, question.getVoteCount());
    }

    @Test
    /*
     * Title: upvote
     * Test | Reasoning
     * | upvote multiple times | confirms vote count matches number of upvote calls |
     */
    public void upvote_multipleTimes_voteCountMatchesCalls() {
        question.upvote(UUID.randomUUID());
        question.upvote(UUID.randomUUID());
        question.upvote(UUID.randomUUID());
        assertEquals(3, question.getVoteCount());
    }

    @Test
    /*
     * Title: upvote
     * Test | Reasoning
     * | upvote and verify solutions unchanged | checks that upvote only changes vote count |
     */
    public void upvote_doesNotAffectSolutionsList() {
        assertTrue(question.getSolutions().isEmpty());
        question.upvote(UUID.randomUUID());
        assertEquals(1, question.getVoteCount());
        assertTrue(question.getSolutions().isEmpty());
    }

    @Test
    /*
     * Title: downvote
     * Test | Reasoning
     * | downvote at zero | confirms vote count cannot go negative from zero |
     */
    public void downvote_fromZero_staysZero() {
        question.downvote(UUID.randomUUID());
        assertEquals(0, question.getVoteCount());
    }

    @Test
    /*
     * Title: downvote
     * Test | Reasoning
     * | upvote once then downvote once | checks that opposite actions return count to zero |
     */
    public void downvote_afterSingleUpvote_returnsToZero() {
        question.upvote(UUID.randomUUID());
        question.downvote(UUID.randomUUID());
        assertEquals(0, question.getVoteCount());
    }

    @Test
    /*
     * Title: downvote
     * Test | Reasoning
     * | multiple downvotes from zero | confirms repeated downvotes never go below zero |
     */
    public void downvote_multipleTimesFromZero_neverBelowZero() {
        question.downvote(UUID.randomUUID());
        question.downvote(UUID.randomUUID());
        question.downvote(UUID.randomUUID());
        assertEquals(0, question.getVoteCount());
    }


}