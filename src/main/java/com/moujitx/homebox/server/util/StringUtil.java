package com.moujitx.homebox.server.util;

public class StringUtil {

    private StringUtil() {}

    public static String normalizeSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return search.trim();
    }
}
