package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateMemberRequest;
import com.moujitx.homebox.server.dto.request.UpdateMemberRequest;
import com.moujitx.homebox.server.dto.response.MemberResponse;
import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.RoleRepository;
import com.moujitx.homebox.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<MemberResponse> getAllMembers() {
        return userRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse getMemberById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return MemberResponse.from(user);
    }

    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setRole(role);
        user.setForceChangePassword(true);

        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public MemberResponse updateMember(Long id, UpdateMemberRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleName()));
            user.setRole(role);
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setForceChangePassword(true);
        }

        return MemberResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteMember(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        if ("root".equals(user.getRole().getName()) && isLastRootUser()) {
            throw new OperationNotAllowedException("Cannot delete the last root user");
        }

        userRepository.delete(user);
    }

    private boolean isLastRootUser() {
        Role rootRole = roleRepository.findByName("root").orElse(null);
        if (rootRole == null) return false;
        return userRepository.findAll().stream()
                .filter(u -> u.getRole().getId().equals(rootRole.getId()))
                .count() <= 1;
    }
}
