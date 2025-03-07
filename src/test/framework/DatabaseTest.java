package com.tests;

import com.framework.utils.DatabaseManager;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    @BeforeAll
    public static void setup() {
        DatabaseManager.connect();
    }

    @Test
    public void testReadUserData() {
        List<Map<String, Object>> results = DatabaseManager.executeQuery("SELECT * FROM users WHERE status='ACTIVE'");
        assertFalse(results.isEmpty(), "No active users found in database.");
    }

    @Test
    public void testInsertUserData() {
        int rowsAffected = DatabaseManager.executeUpdate("INSERT INTO users (username, password, status) VALUES ('testuser', 'securepass', 'ACTIVE')");
        assertTrue(rowsAffected > 0, "User insertion failed.");
    }

    @AfterAll
    public static void tearDown() {
        DatabaseManager.closeConnection();
    }
}