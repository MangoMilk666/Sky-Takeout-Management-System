package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class EnvFileLoader {

    private EnvFileLoader() {
    }

    public static Map<String, String> loadDotEnvLocal() {
        File envFile = resolveDotEnvLocal();
        if (envFile == null || !envFile.isFile()) {
            return Map.of();
        }
        try {
            return loadFile(envFile);
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static File resolveDotEnvLocal() {
        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.isBlank()) {
            File fromUserDir = new File(userDir, ".env.local");
            if (fromUserDir.isFile()) {
                return fromUserDir;
            }
        }

        File fromModuleDir = new File(".env.local");
        if (fromModuleDir.isFile()) {
            return fromModuleDir;
        }

        return null;
    }

    private static Map<String, String> loadFile(File file) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int eqIdx = trimmed.indexOf('=');
                if (eqIdx <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, eqIdx).trim();
                String value = trimmed.substring(eqIdx + 1).trim();
                value = stripOptionalQuotes(value);
                if (!key.isEmpty()) {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    private static String stripOptionalQuotes(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}

