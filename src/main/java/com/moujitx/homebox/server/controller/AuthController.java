package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.ChangePasswordRequest;
import com.moujitx.homebox.server.dto.request.LoginRequest;
import com.moujitx.homebox.server.dto.response.LoginResponse;
import com.moujitx.homebox.server.dto.response.MessageResponse;
import com.moujitx.homebox.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
