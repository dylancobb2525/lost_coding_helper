package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

import com.model.enums.Topic;

/**
 * Behavior tests for {@link User} and {@link Contributor} (not simple getters/setters).
 * <p>
 * AI helped with boilerplate; the table is the behavior checklist I wanted.
 * <pre>
 * +--------------------------------------+------------------------------------------------------------------+
 * | Test                                 | Reasoning                                                        |
 * +--------------------------------------+------------------------------------------------------------------+
 * | credentials match / wrong password   | login success and failure                                        |
 * | locked account / max fails / unlock  | lockout rules                                                    |
 * | change password                      | old correct + new valid; wrong old; weak new                     |
 * | validate email / username / password | helpers match our signup rules                                   |
 * | setFavoriteProblems dedupe           | same question id once                                            |
 * | hasAccess keywords                   | contributor feature strings                                      |
 * | addQuestion defaults                 | draft + easy when missing                                        |
 * | addHints / assignTopics / complexity / test cases | question editing helpers                              |
 * | password whitespace on validate      | trimmed password should work (currently fails)                   |
 * | isUsernameUnique                     | should detect in-use names (currently fails)                     |
 * +--------------------------------------+------------------------------------------------------------------+
 * </pre>
 */
public class UserContributorBehaviorTest {

    private static Contributor user(String username, String password) {
        String s = UUID.randomUUID().toString().substring(0, 8);
        return new Contributor(UUID.randomUUID(), "Display", "acc-" + s, username + "_" + s + "@mail.com", username, password);
    }

    @Test
    public void validateCredentials_returnsTrue_whenUsernameAndPasswordMatch() {
        Contributor u = user("validuser", "Password67");

        assertTrue(u.validateCredentials("validuser", "Password67"));
    }

    @Test
    public void validateCredentials_returnsFalse_whenPasswordWrong() {
        Contributor u = user("badpass", "Password67");

        assertFalse(u.validateCredentials("badpass", "Wrongpass1"));
    }

    @Test
    public void validateCredentials_returnsFalse_whenAccountLocked() {
        Contributor u = user("locked", "Password67");
        u.lockAccount();

        assertFalse(u.validateCredentials("locked", "Password67"));
    }

    @Test
    public void validateCredentials_locksAccount_afterMaxFailedAttempts() {
        Contributor u = user("fails", "Password67");

        for (int i = 0; i < 5; i++) {
            u.validateCredentials("fails", "Wrong1");
        }

        assertTrue(u.isLocked());
    }

    @Test
    public void unlockAccount_allowsLoginAgain_afterLock() {
        Contributor u = user("unlockme", "Password67");
        u.lockAccount();

        u.unlockAccount();

        assertTrue(u.validateCredentials("unlockme", "Password67"));
    }

    @Test
    public void changePassword_updatesPassword_whenOldPasswordCorrectAndNewValid() {
        Contributor u = user("chgpw", "Oldpass1");

        u.changePassword("Oldpass1", "Newpass2");

        assertTrue(u.validateCredentials("chgpw", "Newpass2"));
    }

    @Test
    public void changePassword_doesNothing_whenOldPasswordWrong() {
        Contributor u = user("chgpw2", "Oldpass1");

        u.changePassword("NotOld1", "Newpass2");

        assertTrue(u.validateCredentials("chgpw2", "Oldpass1"));
    }

    @Test
    public void changePassword_doesNothing_whenNewPasswordInvalid() {
        Contributor u = user("chgpw3", "Oldpass1");

        u.changePassword("Oldpass1", "short");

        assertTrue(u.validateCredentials("chgpw3", "Oldpass1"));
    }

    @Test
    public void validateEmail_returnsExpectedResults_forValidAndInvalidInput() {
        Contributor u = user("emailt", "Password67");

        assertTrue(u.validateEmail("a@b.co"));
        assertFalse(u.validateEmail(null));
        assertFalse(u.validateEmail(""));
        assertFalse(u.validateEmail("not-an-email"));
    }

    @Test
    public void validateUsername_returnsExpectedResults_forValidAndInvalidInput() {
        Contributor u = user("vuser", "Password67");

        assertTrue(u.validateUsername("abc"));
        assertFalse(u.validateUsername("ab"));
        assertFalse(u.validateUsername("bad!name"));
    }

    @Test
    public void validatePassword_returnsExpectedResults_forValidAndInvalidInput() {
        Contributor u = user("puser", "Password67");

        assertTrue(u.validatePassword("Aa123456"));
        assertFalse(u.validatePassword(null));
        assertFalse(u.validatePassword("short1"));
        assertFalse(u.validatePassword("alllower1"));
    }

    @Test
    public void setFavoriteProblems_replacesList_andDeduplicatesByQuestionId() {
        Contributor u = user("fav", "Password67");
        UUID id = UUID.randomUUID();
        Question q1 = new Question(id, "T", "P", "EASY", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");
        Question qDup = new Question(id, "Other", "X", "HARD", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");
        ArrayList<Question> favorites = new ArrayList<>();
        favorites.add(q1);
        favorites.add(qDup);

        u.setFavoriteProblems(favorites);

        assertEquals(1, u.getFavoriteProblems().size());
        assertEquals(1, u.getFavoritedProblemIds().size());
        assertEquals(id, u.getFavoritedProblemIds().get(0));
    }

    @Test
    public void contributor_hasAccess_returnsTrue_forKnownFeatureKeywords() {
        Contributor c = new Contributor();

        assertTrue(c.hasAccess("submit solution"));
        assertTrue(c.hasAccess("track progress"));
        assertTrue(c.hasAccess("create problem"));
        assertTrue(c.hasAccess("view hint"));
        assertTrue(c.hasAccess("favorite"));
    }

    @Test
    public void contributor_addQuestion_setsDefaults_whenStatusAndDifficultyNull() {
        Contributor c = new Contributor();
        Question q = new Question();
        q.setPublicStatus(false);

        c.addQuestion(q);

        assertEquals("DRAFT", q.getStatus());
        assertEquals("EASY", q.getDifficulty());
    }

    @Test
    public void contributor_addHints_appendsNonEmptyTrimmedStrings() {
        Contributor c = new Contributor();
        UUID id = UUID.randomUUID();
        Question q = new Question(id, "T", "P", "EASY", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");

        c.addHints(q, new String[] { "  first  ", null, "", "second" });

        assertEquals(2, q.getHints().size());
        assertEquals("first", q.getHints().get(0));
        assertEquals("second", q.getHints().get(1));
    }

    @Test
    public void contributor_assignTopics_setsTopics_skippingNullEntries() {
        Contributor c = new Contributor();
        UUID id = UUID.randomUUID();
        Question q = new Question(id, "T", "P", "EASY", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");

        c.assignTopics(q, new Topic[] { Topic.OOP, null, Topic.DATABASE });

        assertEquals(2, q.getTopics().size());
        assertEquals(Topic.OOP, q.getTopics().get(0));
        assertEquals(Topic.DATABASE, q.getTopics().get(1));
    }

    @Test
    public void contributor_setComplexity_setsDifficultyFromTimeAndSpace() {
        Contributor c = new Contributor();
        UUID id = UUID.randomUUID();
        Question q = new Question(id, "T", "P", "EASY", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");

        c.setComplexity(q, "O(n)", "O(1)");

        assertEquals("O(n) | O(1)", q.getDifficulty());
    }

    @Test
    public void contributor_addTestCases_appendsHintLines_forNonEmptyCases() {
        Contributor c = new Contributor();
        UUID id = UUID.randomUUID();
        Question q = new Question(id, "T", "P", "EASY", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                UUID.randomUUID(), LocalDateTime.now(), "PUBLISHED");

        c.addTestCases(q, new TestCase[] {
                new TestCase("1", "1"),
                new TestCase(null, null),
                new TestCase("2", "2")
        });

        assertEquals(2, q.getHints().size());
        assertTrue(q.getHints().get(0).contains("input=1"));
        assertTrue(q.getHints().get(1).contains("input=2"));
    }

    @Test
    public void validateCredentials_returnsTrue_whenPasswordHasSurroundingWhitespace() {
        Contributor u = user("trimcred", "Password67");

        assertTrue(u.validateCredentials("trimcred", "  Password67  "));
    }

    @Test
    public void isUsernameUnique_returnsFalse_whenUsernameIsAlreadyInUse() {
        Contributor u = user("uniqchk", "Password67");

        assertFalse(u.isUsernameUnique("uniqchk"));
    }
}
