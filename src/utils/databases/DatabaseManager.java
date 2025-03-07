package com.framework.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {
    private static Connection connection;

    public static void connect() {
        try {
            String dbUrl = ConfigReader.getProperty("db.url");
            String dbDriver = ConfigReader.getProperty("db.driver");

            // Load credentials from JKS
            JKSReader jksReader = new JKSReader("src/test/resources/db-credentials.jks", "changeit");
            String dbUser = jksReader.getSecret("dbUsername", "changeit");
            String dbPassword = jksReader.getSecret("dbPassword", "changeit");

            Class.forName(dbDriver); // Load JDBC Driver
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("✅ Database Connected Successfully!");
        } catch (Exception e) {
            throw new RuntimeException("❌ Database Connection Failed: " + e.getMessage(), e);
        }
    }

    public static ResultSet executeQuery(String query) {
        try {
            if (connection == null) {
                connect();
            }
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception e) {
            throw new RuntimeException("❌ Query Execution Failed: " + e.getMessage(), e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("✅ Database Connection Closed.");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to Close Database Connection: " + e.getMessage(), e);
        }
    }
}