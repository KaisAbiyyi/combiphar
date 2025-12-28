package com.combiphar.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers for basic CSV parsing and escaping.
 */
public final class CsvUtils {

    private CsvUtils() {
    }

    public static List<List<String>> parse(InputStream inputStream) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static Map<String, Integer> buildHeaderIndex(List<String> headerRow) {
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String key = normalizeHeader(headerRow.get(i));
            if (!key.isEmpty()) {
                headerIndex.put(key, i);
            }
        }
        return headerIndex;
    }

    public static String getCell(List<String> row, Map<String, Integer> headerIndex, String... headerKeys) {
        for (String key : headerKeys) {
            Integer index = headerIndex.get(normalizeHeader(key));
            if (index != null && index < row.size()) {
                return row.get(index);
            }
        }
        return "";
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needsQuoting) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private static String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase().replace(" ", "_");
    }
}
