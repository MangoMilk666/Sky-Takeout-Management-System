package runner;

import config.DBConfig;
import generator.OrderGenerator;
import generator.UserGenerator;
import util.EnvFileLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataGeneratorMain {

    public static void main(String[] args) {
        if (args == null || args.length == 0 || isHelp(args[0])) {
            printUsage();
            return;
        }

        String command = args[0].trim().toLowerCase();
        Map<String, String> options = parseOptions(args, 1);

        Map<String, String> dotEnv = EnvFileLoader.loadDotEnvLocal();

        String url = options.getOrDefault("url", firstNonBlank(dotEnv.get("SKY_DB_URL"), envOrDefault("SKY_DB_URL", "jdbc:mysql://localhost:3306/sky_take_out?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8")));
        String username = options.getOrDefault("username", firstNonBlank(dotEnv.get("SKY_DB_USERNAME"), envOrDefault("SKY_DB_USERNAME", "root")));
        String password = options.getOrDefault("password", firstNonBlank(dotEnv.get("SKY_DB_PASSWORD"), envOrDefault("SKY_DB_PASSWORD", "root")));
        int count = parseInt(options.get("count"), 10000);
        int batchSize = parseInt(options.get("batchSize"), 1000);
        boolean dryRun = options.containsKey("dryRun");
        Locale locale = parseLocale(options.getOrDefault("locale", "zh-CN"));

        try {
            switch (command) {
                case "users" -> runUsers(url, username, password, count, batchSize, dryRun, locale);
                case "orders" -> {
                    int userIdLimit = parseInt(options.get("userIdLimit"), 0);
                    runOrders(url, username, password, count, batchSize, dryRun, locale, userIdLimit);
                }
                case "all" -> {
                    int userCount = parseInt(options.get("userCount"), count);
                    int orderCount = parseInt(options.get("orderCount"), count);
                    runAll(url, username, password, userCount, orderCount, batchSize, dryRun, locale);
                }
                default -> {
                    System.out.println("Unknown command: " + command);
                    printUsage();
                }
            }
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void runUsers(String url, String username, String password, int count, int batchSize, boolean dryRun, Locale locale) throws SQLException {
        UserGenerator userGenerator = new UserGenerator(locale);
        if (dryRun) {
            List<UserGenerator.UserRow> sample = userGenerator.generateSample(Math.min(3, count));
            System.out.println("DryRun sample users: " + sample);
            return;
        }

        try (Connection connection = DBConfig.open(url, username, password)) {
            long start = System.currentTimeMillis();
            List<Long> userIds = userGenerator.generateAndInsert(connection, count, batchSize, false);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Inserted users: " + userIds.size() + ", elapsedMs=" + elapsed);
        }
    }

    private static void runOrders(String url, String username, String password, int count, int batchSize, boolean dryRun, Locale locale, int userIdLimit) throws SQLException {
        OrderGenerator orderGenerator = new OrderGenerator(locale);

        try (Connection connection = DBConfig.open(url, username, password)) {
            List<Long> userIds = loadUserIds(connection, userIdLimit);
            if (dryRun) {
                List<OrderGenerator.OrderRow> sample = orderGenerator.generateSample(Math.min(3, count), userIds);
                System.out.println("DryRun sample orders: " + sample);
                return;
            }

            long start = System.currentTimeMillis();
            long inserted = orderGenerator.generateAndInsert(connection, count, batchSize, userIds, false);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Inserted orders: " + inserted + ", elapsedMs=" + elapsed);
        }
    }

    private static void runAll(String url, String username, String password, int userCount, int orderCount, int batchSize, boolean dryRun, Locale locale) throws SQLException {
        UserGenerator userGenerator = new UserGenerator(locale);
        OrderGenerator orderGenerator = new OrderGenerator(locale);

        if (dryRun) {
            List<UserGenerator.UserRow> users = userGenerator.generateSample(Math.min(3, userCount));
            List<Long> fakeUserIds = List.of(1L, 2L, 3L);
            List<OrderGenerator.OrderRow> orders = orderGenerator.generateSample(Math.min(3, orderCount), fakeUserIds);
            System.out.println("DryRun sample users: " + users);
            System.out.println("DryRun sample orders: " + orders);
            return;
        }

        try (Connection connection = DBConfig.open(url, username, password)) {
            long startUsers = System.currentTimeMillis();
            List<Long> userIds = userGenerator.generateAndInsert(connection, userCount, batchSize, false);
            long usersElapsed = System.currentTimeMillis() - startUsers;
            System.out.println("Inserted users: " + userIds.size() + ", elapsedMs=" + usersElapsed);

            long startOrders = System.currentTimeMillis();
            long insertedOrders = orderGenerator.generateAndInsert(connection, orderCount, batchSize, userIds, false);
            long ordersElapsed = System.currentTimeMillis() - startOrders;
            System.out.println("Inserted orders: " + insertedOrders + ", elapsedMs=" + ordersElapsed);
        }
    }

    private static List<Long> loadUserIds(Connection connection, int limit) throws SQLException {
        String sql = limit > 0 ? "select id from user order by id limit ?" : "select id from user order by id";
        List<Long> userIds = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (limit > 0) {
                ps.setInt(1, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong(1));
                }
            }
        }
        if (userIds.isEmpty()) {
            throw new IllegalStateException("No users found in DB; run users/all first");
        }
        return userIds;
    }

    private static Map<String, String> parseOptions(String[] args, int offset) {
        Map<String, String> map = new HashMap<>();
        for (int i = offset; i < args.length; i++) {
            String token = args[i];
            if (token == null) {
                continue;
            }
            if (!token.startsWith("--")) {
                continue;
            }
            String key = token.substring(2);
            if (key.isBlank()) {
                continue;
            }

            String value = "true";
            if (i + 1 < args.length && args[i + 1] != null && !args[i + 1].startsWith("--")) {
                value = args[i + 1];
                i++;
            }
            map.put(key, value);
        }
        return map;
    }

    private static boolean isHelp(String arg) {
        if (arg == null) {
            return true;
        }
        String a = arg.trim().toLowerCase();
        return a.equals("-h") || a.equals("--help") || a.equals("help");
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Locale parseLocale(String value) {
        if (value == null || value.isBlank()) {
            return Locale.getDefault();
        }
        String normalized = value.trim().replace('_', '-');
        String[] parts = normalized.split("-", 3);
        if (parts.length == 1) {
            return new Locale(parts[0]);
        }
        return new Locale(parts[0], parts[1]);
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }

    private static void printUsage() {
        System.out.println("sky-data-generator\n" +
                "Commands:\n" +
                "  users  --count N [--batchSize N] [--url JDBC] [--username U] [--password P] [--locale zh-CN] [--dryRun]\n" +
                "  orders --count N [--batchSize N] [--userIdLimit N] [--url JDBC] [--username U] [--password P] [--locale zh-CN] [--dryRun]\n" +
                "  all    --userCount N --orderCount N [--batchSize N] [--url JDBC] [--username U] [--password P] [--locale zh-CN] [--dryRun]\n" +
                "Env:\n" +
                "  SKY_DB_URL, SKY_DB_USERNAME, SKY_DB_PASSWORD\n");
    }
}
