package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.request.ChangePasswordRequest;
import com.moujitx.homebox.server.dto.request.UpdateProfileRequest;
import com.moujitx.homebox.server.dto.response.MessageResponse;
import com.moujitx.homebox.server.dto.response.ProfileResponse;
import com.moujitx.homebox.server.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                          Authentication authentication) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
