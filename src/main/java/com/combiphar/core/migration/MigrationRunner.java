package com.combiphar.core.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Simple migration runner to execute SQL files in `database/` folder. -
 * Defensive: logs errors but does not crash the app on non-critical failures -
 * Idempotent: migration SQL should be written safely (CREATE IF NOT EXISTS)
 */
public final class MigrationRunner {

    private MigrationRunner() {
    }

    public static void runMigrations() {
        Path migrationFile = Path.of(System.getProperty("user.dir"), "database", "migration_add_cart.sql");
        if (!Files.exists(migrationFile)) {
            System.out.println("[MigrationRunner] No migration file found at: " + migrationFile);
            return;
        }

        System.out.println("[MigrationRunner] Running migration: " + migrationFile);

        try {
            String sql = Files.lines(migrationFile)
                    .map(line -> {
                        // Remove single-line comments
                        String trimmed = line.replaceAll("--.*$", "");
                        return trimmed;
                    })
                    .collect(Collectors.joining("\n"));

            // Splitting by semicolon; this is simplistic but works for our migrations
            String[] statements = Arrays.stream(sql.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            try (Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                try (Statement stmt = conn.createStatement()) {
                    for (String s : statements) {
                        try {
                            stmt.execute(s);
                        } catch (SQLException ex) {
                            System.err.println("[MigrationRunner] Statement failed: " + ex.getMessage());
                            // continue with the rest
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("[MigrationRunner] Migration failed and was rolled back: " + e.getMessage());
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            System.out.println("[MigrationRunner] Migration finished (best-effort)");
        } catch (IOException e) {
            System.err.println("[MigrationRunner] Failed to read migration file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[MigrationRunner] DB error during migrations: " + e.getMessage());
        }
    }
}
