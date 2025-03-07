package utils.databases;

import java.sql.*;
import java.util.*;

import utils.config.ConfigReader;
import utils.jks.JKSReader;

public class DatabaseManager {
    private static Connection connection;

    public static void connect() {
        try {
            String dbUrl = ConfigReader.getProperty("db.url");
            String dbDriver = ConfigReader.getProperty("db.driver");

            // Load credentials from JKS
            JKSReader jksReader = new JKSReader();
            String[] credentials = jksReader.getSecret("db-connection").split(",");
            String dbUser = credentials[0];
            String dbPassword = credentials[1];

            Class.forName(dbDriver); // Load JDBC Driver
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("✅ Database Connected Successfully!");
        } catch (Exception e) {
            throw new RuntimeException("❌ Database Connection Failed: " + e.getMessage(), e);
        }
    }

    public static List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            if (connection == null) {
                connect();
            }
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                resultList.add(row);
            }
            resultSet.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException("❌ Query Execution Failed: " + e.getMessage(), e);
        }
        return resultList;
    }

    public static int executeUpdate(String query) {
        try {
            if (connection == null) {
                connect();
            }
            Statement stmt = connection.createStatement();
            int rowsAffected = stmt.executeUpdate(query);
            stmt.close();
            return rowsAffected;
        } catch (Exception e) {
            throw new RuntimeException("❌ Update Execution Failed: " + e.getMessage(), e);
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