package utils.databases;

import java.sql.*;
import java.util.*;

import utils.config.ConfigReader;
import utils.jks.JKSReader;
import static core.TestLogger.LOGGER;

public class DatabaseManager {
    private static Connection connection;

    public static void connect() {
        try {
            String dbUrl = ConfigReader.getProperty("db.url");
            String dbDriver = ConfigReader.getProperty("db.driver");

            // Load credentials from JKS
            JKSReader jksReader = new JKSReader();
            String[] credentials = jksReader.getSecret("db-connection").split(",", 2);
            if (credentials.length < 2) {
                throw new RuntimeException("❌ DB credentials in JKS must be 'user,password'");
            }
            String dbUser = credentials[0];
            String dbPassword = credentials[1];

            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            LOGGER.info("✅ Database Connected Successfully!");
        } catch (Exception e) {
            throw new RuntimeException("❌ Database Connection Failed: " + e.getMessage(), e);
        }
    }

    /** Execute a read query using a PreparedStatement with optional bind parameters. */
    public static List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (connection == null) connect();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    resultList.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Query Execution Failed: " + e.getMessage(), e);
        }
        return resultList;
    }

    /** Execute a write query using a PreparedStatement with optional bind parameters. */
    public static int executeUpdate(String query, Object... params) {
        if (connection == null) connect();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("❌ Update Execution Failed: " + e.getMessage(), e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                LOGGER.info("✅ Database Connection Closed.");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to Close Database Connection: " + e.getMessage(), e);
        }
    }
}
