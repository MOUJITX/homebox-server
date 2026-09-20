package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.OidcCallbackRequest;
import com.moujitx.homebox.server.dto.response.LoginResponse;
import com.moujitx.homebox.server.dto.response.OidcAuthorizeResponse;
import com.moujitx.homebox.server.dto.response.OidcConfigResponse;
import com.moujitx.homebox.server.service.OidcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oidc")
@RequiredArgsConstructor
public class OidcController {

    private final OidcService oidcService;

    @GetMapping("/config")
    public ResponseEntity<OidcConfigResponse> config() {
        return ResponseEntity.ok(new OidcConfigResponse(oidcService.isEnabled()));
    }

    @GetMapping("/authorize")
    public ResponseEntity<OidcAuthorizeResponse> authorize() {
        return ResponseEntity.ok(new OidcAuthorizeResponse(oidcService.buildAuthorizeUrl()));
    }

    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> callback(@Valid @RequestBody OidcCallbackRequest request) {
        return ResponseEntity.ok(oidcService.handleCallback(request.getCode(), request.getState()));
    }
}
