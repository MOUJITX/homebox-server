package com.moujitx.homebox.server.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class OssUrlBuilder {

    private OssUrlBuilder() {
    }

    public static String build(String storedFilename, String originalFilename) {
        String encoded = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);
        return "/oss/" + storedFilename + "?attname=" + encoded;
    }
}
