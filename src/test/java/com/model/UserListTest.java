package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

/**
 * Unit tests for {@link UserList}: account creation, authentication, lookups, duplicates, and invalid input.
 * <p>
 * AI was used to help write the boilerplate; the table is my test plan.
 * <pre>
 * +--------------------------------------+------------------------------------------------------------------+
 * | Test                                 | Reasoning                                                        |
 * +--------------------------------------+------------------------------------------------------------------+
 * | valid signup                         | good data creates a user and increases list size                 |
 * | any required field null              | null display name, username, email, or password rejects signup   |
 * | username too short / bad chars       | length 3-25 and [a-zA-Z0-9_]                                     |
 * | invalid email                        | needs @ and valid dot placement                                    |
 * | password rules                       | length 8+, upper, lower, digit                                   |
 * | duplicate username / email         | case-insensitive uniqueness                                      |
 * | login username / email               | correct password returns user                                    |
 * | wrong password                       | bad password returns null                                        |
 * | unknown user / null login args       | no match or null args return null                                |
 * | getById / getByUsername              | found vs missing                                                 |
 * | addAll                               | bulk add merges into list                                        |
 * | blank display name (expected reject) | whitespace-only name should not register (currently fails)      |
 * | trimmed password login (expected ok) | login with trimmed password after padded signup (currently fails)|
 * +--------------------------------------+------------------------------------------------------------------+
 * </pre>
 */
public class UserListTest {

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    public void createAccount_returnsUser_andIncrementsListSize_whenInputIsValid() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();

        User created = userList.createAccount("Chris Feuchter", "user_" + s, "user_" + s + "@mail.com", "Password67");

        assertNotNull(created);
        assertEquals("user_" + s, created.getUsername());
        assertEquals("user_" + s + "@mail.com", created.getEmail());
        assertEquals(before + 1, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenRequiredArgumentIsNull() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();
        String u = "u_" + s;
        String em = u + "@mail.com";

        assertNull(userList.createAccount(null, u, em, "Password67"));
        assertNull(userList.createAccount("Name", null, em, "Password67"));
        assertNull(userList.createAccount("Name", u, null, "Password67"));
        assertNull(userList.createAccount("Name", u, em, null));
        assertEquals(before, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenUsernameTooShort() {
        UserList userList = new UserList();
        int before = userList.getAll().size();

        assertNull(userList.createAccount("Name", "ab", "shortuser@mail.com", "Password67"));
        assertEquals(before, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenUsernameHasInvalidCharacters() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();

        assertNull(userList.createAccount("Name", "bad-name-" + s, "badname_" + s + "@mail.com", "Password67"));
        assertEquals(before, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenEmailIsInvalid() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();

        assertNull(userList.createAccount("Name", "user_" + s, "notanemail", "Password67"));
        assertEquals(before, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenPasswordViolatesComplexityRequirements() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();
        String u = "user_" + s;
        String em = u + "@mail.com";

        assertNull(userList.createAccount("Name", u, em, "short1"));
        assertNull(userList.createAccount("Name", u, em, "alllowercase1"));
        assertNull(userList.createAccount("Name", u, em, "ALLUPPERCASE1"));
        assertNull(userList.createAccount("Name", u, em, "NoDigitsHere"));
        assertEquals(before, userList.getAll().size());
    }

    @Test
    public void createAccount_returnsNull_whenUsernameAlreadyExists_caseInsensitive() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String username = "dupuser_" + s;
        String email1 = "first_" + s + "@mail.com";
        String email2 = "second_" + s + "@mail.com";

        assertNotNull(userList.createAccount("First", username, email1, "Password67"));
        assertNull(userList.createAccount("Second", username.toUpperCase(), email2, "Password67"));
    }

    @Test
    public void createAccount_returnsNull_whenEmailAlreadyExists_caseInsensitive() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String email = "shared_" + s + "@mail.com";

        assertNotNull(userList.createAccount("First", "first_" + s, email, "Password67"));
        assertNull(userList.createAccount("Second", "second_" + s, email.toUpperCase(), "Password67"));
    }

    @Test
    public void authenticate_returnsUser_whenUsernameAndPasswordMatch() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String username = "auth_" + s;
        String password = "Password67";
        User created = userList.createAccount("Auth User", username, "auth_" + s + "@mail.com", password);

        User loggedIn = userList.authenticate(username, password);

        assertNotNull(loggedIn);
        assertEquals(created.getUserId(), loggedIn.getUserId());
    }

    @Test
    public void authenticate_returnsUser_whenEmailAndPasswordMatch() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String email = "authmail_" + s + "@mail.com";
        String password = "Password67";
        userList.createAccount("Auth User", "authmailuser_" + s, email, password);

        User loggedIn = userList.authenticate(email, password);

        assertNotNull(loggedIn);
        assertEquals(email, loggedIn.getEmail());
    }

    @Test
    public void authenticate_returnsNull_whenPasswordWrong() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        userList.createAccount("U", "wrongpass_" + s, "wrongpass_" + s + "@mail.com", "Password67");

        assertNull(userList.authenticate("wrongpass_" + s, "WrongPassword1"));
    }

    @Test
    public void authenticate_returnsNull_whenUserNotFoundOrLoginArgumentIsNull() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        userList.createAccount("U", "nullargs_" + s, "nullargs_" + s + "@mail.com", "Password67");

        assertNull(userList.authenticate("no_such_user_zzzz", "Password67"));
        assertNull(userList.authenticate(null, "Password67"));
        assertNull(userList.authenticate("nullargs_" + s, null));
    }

    @Test
    public void getById_returnsUser_whenIdExists_andNullWhenMissing() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        User created = userList.createAccount("U", "byid_" + s, "byid_" + s + "@mail.com", "Password67");

        assertEquals(created, userList.getById(created.getUserId()));
        assertNull(userList.getById(null));
        assertNull(userList.getById(UUID.randomUUID()));
    }

    @Test
    public void getByUsername_returnsUser_caseInsensitive_andNullWhenMissing() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String username = "CaseUser_" + s;
        userList.createAccount("U", username, "case_" + s + "@mail.com", "Password67");

        assertNotNull(userList.getByUsername(username.toUpperCase()));
        assertNull(userList.getByUsername(null));
        assertNull(userList.getByUsername("unknown_user_xyz"));
    }

    @Test
    public void addAll_addsUsers_whenListNonNull() {
        UserList userList = new UserList();
        int before = userList.getAll().size();
        String s = uniqueSuffix();
        UUID id = UUID.randomUUID();
        ArrayList<User> extra = new ArrayList<>();
        extra.add(new Contributor(id, "Extra", "acc-" + s, "extra_" + s + "@mail.com", "extrauser_" + s, "Password67"));

        userList.addAll(extra);

        assertEquals(before + 1, userList.getAll().size());
        assertEquals(id, userList.getById(id).getUserId());
    }

    @Test
    public void createAccount_returnsNull_whenDisplayNameIsOnlyWhitespace() {
        UserList userList = new UserList();
        String s = uniqueSuffix();

        assertNull(userList.createAccount("   ", "wsdn_" + s, "wsdn_" + s + "@mail.com", "Password67"));
    }

    @Test
    public void authenticate_returnsUser_whenLoginUsesTrimmedPasswordAfterPaddedSignup() {
        UserList userList = new UserList();
        String s = uniqueSuffix();
        String username = "trimpw_" + s;
        userList.createAccount("U", username, "trimpw_" + s + "@mail.com", "  Password67  ");

        assertNotNull(userList.authenticate(username, "Password67"));
    }
}
