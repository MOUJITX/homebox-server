package com.moujitx.homebox.server.dto.response;

public record TestConnectionResponse(
        boolean success,
        String message
) {
}
