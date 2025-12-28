package com.combiphar.core.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Configuration for database connection using HikariCP.
 */
public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        // Debugging to make sure env vars are read
        System.out.println("--- Database Configuration Debug ---");
        System.out.println("DB_URL: " + (dbUrl != null ? dbUrl : "MISSING"));
        System.out.println("DB_USER: " + (dbUser != null ? dbUser : "MISSING"));
        System.out.println("DB_PASS: " + (dbPass != null ? "********" : "MISSING"));
        System.out.println("------------------------------------");

        // Force environment variables (no fallback)
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL: Environment variable DB_URL is not set!");
        }
        if (dbUser == null || dbUser.trim().isEmpty()) {
            throw new IllegalStateException("CRITICAL: Environment variable DB_USER is not set!");
        }

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);

        // Optimization settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private DatabaseConfig() {
        // Prevent instantiation
    }
}
