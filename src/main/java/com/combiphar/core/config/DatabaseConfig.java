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

        // Fallback to defaults if env vars are not set
        config.setJdbcUrl(dbUrl != null ? dbUrl : "jdbc:mysql://localhost:3306/combiphar_db");
        config.setUsername(dbUser != null ? dbUser : "root");
        config.setPassword(dbPass != null ? dbPass : "515250");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
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
