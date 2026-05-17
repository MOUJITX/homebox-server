package com.moujitx.homebox.server.event;

import lombok.Getter;

@Getter
public class ConfigChangedEvent {

    private final String group;

    public ConfigChangedEvent(String group) {
        this.group = group;
    }
}
