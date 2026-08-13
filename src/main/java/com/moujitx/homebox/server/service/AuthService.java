package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.LoginRequest;
import com.moujitx.homebox.server.dto.response.LoginResponse;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.UserRepository;
import com.moujitx.homebox.server.security.ClientType;
import com.moujitx.homebox.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ClientType clientType = ClientType.fromValue(request.getClientType());
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName(), clientType);
        long expiresIn = jwtTokenProvider.getExpirationMillis(clientType) / 1000;
        return new LoginResponse(token, expiresIn, user.isForceChangePassword());
    }
}
