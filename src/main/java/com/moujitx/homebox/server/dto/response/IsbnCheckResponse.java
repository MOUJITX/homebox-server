package com.moujitx.homebox.server.dto.response;

import java.util.List;

public record IsbnCheckResponse(long count, List<String> titles) {}
