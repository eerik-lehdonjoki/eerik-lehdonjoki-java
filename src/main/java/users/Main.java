package users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reads users.csv located in this Java/ directory.
 * Operations: summary|filter|group|avg|top|region
 */
public class Main {

    public record UserRecord(String name, String age, String country) {}

    private static final Path CSV_PATH = Paths.get("users.csv");

    public static List<UserRecord> loadUsers(Path path) {
        if (!Files.exists(path)) {
            System.err.println("Could not read CSV at " + path.toAbsolutePath());
            return List.of();
        }
        List<UserRecord> users = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) return List.of();
            String[] header = Arrays.stream(headerLine.split(",")).map(String::trim).toArray(String[]::new);
            int idxName = indexOf(header, "name");
            int idxAge = indexOf(header, "age");
            int idxCountry = indexOf(header, "country");
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
                users.add(new UserRecord(col(cols, idxName), col(cols, idxAge), col(cols, idxCountry)));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }
        return users;
    }

    private static int indexOf(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equalsIgnoreCase(key)) return i;
        return -1;
    }

    private static String col(String[] cols, int i) {
        return (i >= 0 && i < cols.length) ? cols[i] : "";
    }

    public static List<UserRecord> filterUsersByMinimumAge(List<UserRecord> users, int threshold) {
        return users.stream()
                .filter(u -> parseInt(u.age) >= threshold)
                .collect(Collectors.toList());
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    public static Map<String, Long> countUsersByCountry(List<UserRecord> users) {
        return users.stream().collect(Collectors.groupingBy(UserRecord::country, TreeMap::new, Collectors.counting()));
    }

    public static double calculateUsersAverageAge(List<UserRecord> users) {
        IntSummaryStatistics stats = users.stream()
                .map(UserRecord::age)
                .mapToInt(Main::parseInt)
                .filter(v -> v >= 0)
                .summaryStatistics();
        if (stats.getCount() == 0) return 0.0;
        double avg = stats.getAverage();
        return Math.round(avg * 10) / 10.0;
    }

    public static List<UserRecord> getTopNOldestUsers(List<UserRecord> users, int n) {
        return users.stream()
                .sorted(Comparator.comparingInt((UserRecord u) -> parseInt(u.age)).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public static String getRegionForCountry(String country) {
        return switch (country) {
            case "Finland", "Germany", "France", "UK" -> "Europe";
            case "USA", "Canada" -> "North America";
            case "Brazil" -> "South America";
            case "India", "Japan" -> "Asia";
            case "Australia" -> "Oceania";
            default -> "Other";
        };
    }

    public static Map<String, Long> usersByRegion(List<UserRecord> users) {
        return users.stream().collect(Collectors.groupingBy(u -> getRegionForCountry(u.country), TreeMap::new, Collectors.counting()));
    }

    private static void logKeyValueLines(Map<String, ? extends Number> m) {
        m.forEach((k, v) -> System.out.println("  " + k + ": " + v));
    }

    private static void doSummary(List<UserRecord> users) {
        int total = users.size();
        List<UserRecord> filtered = filterUsersByMinimumAge(users, 30);
        Map<String, Long> grouped = countUsersByCountry(users);
        double avgAge = calculateUsersAverageAge(users);
        List<UserRecord> oldest = getTopNOldestUsers(users, 3);
        Map<String, Long> regionCounts = usersByRegion(users);

        System.out.println("Total users: " + total);
        System.out.println("Filtered count: " + filtered.size());
        System.out.println("Users per country:");
        logKeyValueLines(grouped);
        System.out.println("Average age: " + avgAge);
        System.out.println("Top 3 oldest users:");
        oldest.forEach(u -> System.out.println("  " + u.name + " (" + u.age + ")"));
        System.out.println("Users per region:");
        logKeyValueLines(regionCounts);
    }

    public static void main(String[] args) {
        List<UserRecord> users = loadUsers(CSV_PATH);
        if (users.isEmpty()) return;
        String op = (args.length > 0) ? args[0] : "summary";
        switch (op) {
            case "summary" -> doSummary(users);
            case "filter" -> System.out.println("Filtered count: " + filterUsersByMinimumAge(users, 30).size());
            case "group" -> { System.out.println("Users per country:"); logKeyValueLines(countUsersByCountry(users)); }
            case "avg" -> System.out.println("Average age: " + calculateUsersAverageAge(users));
            case "top" -> getTopNOldestUsers(users, 3).forEach(u -> System.out.println(u.name + " (" + u.age + ")"));
            case "region" -> { System.out.println("Users per region:"); logKeyValueLines(usersByRegion(users)); }
            default -> System.out.println("Unknown operation '" + op + "'. Use summary|filter|group|avg|top|region.");
        }
    }
}
