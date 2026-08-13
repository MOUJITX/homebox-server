package com.moujitx.homebox.server.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum ClientType {

    CLIENT("client"),
    APP("app");

    private final String value;

    public static ClientType fromValue(String value) {
        if (value == null) {
            return CLIENT;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "app" -> APP;
            default -> CLIENT;
        };
    }
}
