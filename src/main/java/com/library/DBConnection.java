package com.library;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        Properties local = new Properties();
        Path localConfig = Paths.get("database", "local.properties");
        if (Files.isRegularFile(localConfig)) {
            try (InputStream input = Files.newInputStream(localConfig)) {
                local.load(input);
            } catch (IOException e) {
                throw new SQLException("Unable to read local database configuration.", e);
            }
        }

        String url = setting("LIBRARY_DB_URL", local, "library.db.url", "jdbc:mysql://localhost:3306/library_db");
        String user = setting("LIBRARY_DB_USER", local, "library.db.user", "root");
        String password = setting("LIBRARY_DB_PASSWORD", local, "library.db.password", null);
        if (password == null || password.isEmpty()) {
            throw new SQLException("Set LIBRARY_DB_PASSWORD or configure database/local.properties.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is missing.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String envName, Properties local, String property, String fallback) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) value = local.getProperty(property);
        return value == null || value.isEmpty() ? fallback : value;
    }
}
