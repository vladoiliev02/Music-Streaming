package com.vlado.spotify.database;

import com.vlado.spotify.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDatabaseTest {

    private static final String USERNAME = "usernameX";
    private static final String PASSWORD = "password";

    private static final UserDatabase userDatabase = UserDatabase.instance();
    private static StringWriter writer;

    @BeforeAll
    static void beforeAll() {
        writer = new StringWriter();
        userDatabase.setWriter(writer);
    }

    @AfterAll
    static void afterAll() {
        try {
            writer.close();
        } catch (IOException e) {
            fail("Writer closing error");
        }
        UserDatabase.instance().clear();
    }

    @Test
    void testAddUserNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser(null, PASSWORD),
                "Cannot add user with null username.");
    }

    @Test
    void testAddUserEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser("", PASSWORD),
                "Cannot add user with empty username.");
    }

    @Test
    void testAddUserBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser("       ", PASSWORD),
                "Cannot add user with blank username.");
    }

    @Test
    void testAddUserNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser(USERNAME, null),
                "Cannot add user with null password.");
    }

    @Test
    void testAddUserEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser(USERNAME, ""),
                "Cannot add user with empty password.");
    }

    @Test
    void testAddUserBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.addUser(USERNAME, "      "),
                "Cannot add user with blank password.");
    }

    @Test
    @Order(1)
    void testAddUserSuccessfully() {
        assertDoesNotThrow(() -> userDatabase.addUser(USERNAME, PASSWORD),
                "Adding correct user does not throw.");
        assertTrue(userDatabase.isCorrectPassword(USERNAME, PASSWORD),
                "Password is saved correctly.");
        assertTrue(userDatabase.exists(USERNAME),
                "User exists after being added.");
        assertEquals(USERNAME + " " + PASSWORD + System.lineSeparator(), writer.toString(),
                "User is saved correctly in the file.");
    }

    @Test
    @Order(2)
    void testAddAlreadyExistingUser() {
        assertThrows(UserAlreadyExistsException.class, () -> userDatabase.addUser(USERNAME, PASSWORD),
                "Adding an already existing user throws exception.");
    }

    @Test
    void testReadUsersNullReader() {
        assertThrows(IllegalArgumentException.class, () -> userDatabase.readUsers(null),
                "Reading users from a null reader throws exception.");
    }

    @Test
    @Order(3)
    void testReadUsersSuccessfully() {
        Map<String, String> map = new HashMap<>();
        map.put("u1", "p1");
        map.put("u2", "p2");
        map.put("u3", "p3");
        try (var reader = new StringReader("u1 p1\nu2 p2\nu3 p3")) {
            assertDoesNotThrow(() -> userDatabase.readUsers(reader),
                    "Reading correct users does not throw exception.");
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            assertTrue(userDatabase.exists(entry.getKey()),
                    "Read users exist in memory.");
            assertTrue(userDatabase.isCorrectPassword(entry.getKey(), entry.getValue()),
                    "Passwords are saved correctly.");
        }
    }

    @Test
    @Order(4)
    void testReadUsersExistingUsersError() {

        try (var reader = new StringReader("u4 p4\nu2 p2\nu5 p5")) {
            assertThrows(UserAlreadyExistsException.class, () -> userDatabase.readUsers(reader),
                    "Trying to add existing user throws exception.");
        }

        assertTrue(userDatabase.exists("u4"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u4", "p4"),
                "User has correct password.");

        assertTrue(userDatabase.exists("u2"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u2", "p2"),
                "User has correct password.");

        assertFalse(userDatabase.exists("u5"),
                "Not added user does not exist.");
        assertFalse(userDatabase.isCorrectPassword("u5", "p5"),
                "Not added user testing for password is false.");
    }

    @Test
    @Order(4)
    void testReadUsersIllegalFormat() {

        try (var reader = new StringReader("u6 p6\nu2 p2 p3\nu5 p5")) {
            assertThrows(IllegalArgumentException.class, () -> userDatabase.readUsers(reader),
                    "Trying to add user with invalid format throws exception.");
        }

        assertTrue(userDatabase.exists("u6"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u6", "p6"),
                "User has correct password.");

        assertTrue(userDatabase.exists("u4"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u4", "p4"),
                "User has correct password.");

        assertTrue(userDatabase.exists("u2"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u2", "p2"),
                "User has correct password.");

        assertTrue(userDatabase.exists("u3"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u3", "p3"),
                "User has correct password.");

        assertTrue(userDatabase.exists("u1"),
                "User exists.");
        assertTrue(userDatabase.isCorrectPassword("u1", "p1"),
                "User has correct password.");

        assertFalse(userDatabase.exists("u5"),
                "Not added user does not exist.");
        assertFalse(userDatabase.isCorrectPassword("u5", "p5"),
                "Not added user testing for password is false.");
    }
}