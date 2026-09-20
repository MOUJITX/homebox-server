package com.moujitx.homebox.server.exception;

public class PasswordLoginDisabledException extends RuntimeException {

    public PasswordLoginDisabledException(String message) {
        super(message);
    }
}
