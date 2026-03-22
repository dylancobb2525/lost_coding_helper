package com.model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Holds the list of users. Load from json in constructor or call load(). Save to json with save().
 */
public class UserList {
    private ArrayList<User> users;

    public UserList() {
        this.users = new ArrayList<>();
        load();
    }

    /**
     * Loads users from the users json file. Replaces the current list. This is the Load users json task.
     */
    public void load() {
        DataLoader dataLoader = new DataLoader();
        ArrayList<User> loadedUsers = dataLoader.getUsers();
        users.clear();
        if (loadedUsers != null) {
            users.addAll(loadedUsers);
        }
    }

    /**
     * Adds a list of users (e.g. from init). Used when loading at startup.
     */
    public void addAll(ArrayList<User> loadedUsers) {
        if (loadedUsers != null) {
            users.addAll(loadedUsers);
        }
    }

    public User createAccount(String displayName, String username, String email, String password) {
        if (displayName == null || username == null || email == null || password == null) {
            return null;
        }

        String u = username.trim();
        String e = email.trim();

        if (u.length() < 3 || u.length() > 25 || !u.matches("[a-zA-Z0-9_]+")) {
            return null;
        }

        int at = e.indexOf('@');
        int dot = e.lastIndexOf('.');
        if (at <= 0 || dot <= at + 1 || dot >= e.length() - 1) {
            return null;
        }

        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
            return null;
        }


        for (User exists : users) {
            if ((exists.getUsername() != null && exists.getUsername().equalsIgnoreCase(u))) {
                return null;
            }

            if (exists.getEmail() != null && exists.getEmail().equalsIgnoreCase(e)) {
                return null;
            }
        }

        String accountId = UUID.randomUUID().toString();
        String hashedPassword = password; //hash password before storing?

        User user = new Contributor(UUID.randomUUID(), displayName, accountId, e, u, hashedPassword);
        users.add(user);
        return user;
    }

    public User authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return null;
        }

        String verify = usernameOrEmail.trim();


        for (User user : users) {
            boolean matchIdentity = (user.getUsername() != null && user.getUsername().equalsIgnoreCase(verify)) ||
                                    (user.getEmail() != null && user.getEmail().equalsIgnoreCase(verify));
                        
            if (matchIdentity) {
                if (user.validateCredentials(user.getUsername(), password)) {
                    return user;
                }
                return null;
            }
        }
        return null;
    }

    public User getById(UUID userId) {
        if (userId == null) {
            return null;
        }

        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }        
        return null;
    }

    public User getByUsername(String username) {
        if (username == null) {
            return null;
        }
        String verify = username.trim();

        for (User user : users) {
            if (user.getUsername() != null && user.getUsername().equalsIgnoreCase(verify)) {
                return user;
            }
        }
        return null;
    }

    public ArrayList<User> getAll() {
        return new ArrayList<>(users);
    }

    /**
     * Saves the list of users to the users json file. This is the Save users json task.
     */
    public boolean save() {
        DataWriter dataWriter = new DataWriter();
        return dataWriter.saveUsers(users);
    }
}
