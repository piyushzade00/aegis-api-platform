package com.aegis.api_platform.util;

public final class PathNormalizer {

    private static final String PATH_REGEX = "^/[a-zA-Z0-9\\-_/]*$";

    private PathNormalizer() {}

    public static String normalize(String rawPath) {

        if (rawPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        String path = rawPath.trim().toLowerCase();

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }

        if (path.contains("?")) {
            throw new IllegalArgumentException("Query parameters not allowed in API path");
        }

        // Remove trailing slash (except root)
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.contains("//")) {
            throw new IllegalArgumentException("Path cannot contain double slashes");
        }

        if (!path.matches(PATH_REGEX)) {
            throw new IllegalArgumentException("Invalid path format");
        }

        return path;
    }
}