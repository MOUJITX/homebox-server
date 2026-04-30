package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateRoleRequest;
import com.moujitx.homebox.server.dto.request.UpdateRoleRequest;
import com.moujitx.homebox.server.dto.response.RoleResponse;
import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.RoleRepository;
import com.moujitx.homebox.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .toList();
    }

    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Role already exists: " + request.getName());
        }

        Role role = new Role(request.getName(), request.getDescription());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if ("root".equals(role.getName()) && request.getName() != null && !"root".equals(request.getName())) {
            throw new OperationNotAllowedException("Cannot rename the root role");
        }

        if (request.getName() != null) {
            if (!request.getName().equals(role.getName()) && roleRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Role already exists: " + request.getName());
            }
            role.setName(request.getName());
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if ("root".equals(role.getName())) {
            throw new OperationNotAllowedException("Cannot delete the root role");
        }

        boolean hasUsers = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole().getId().equals(role.getId()));
        if (hasUsers) {
            throw new OperationNotAllowedException("Cannot delete role that is assigned to users");
        }

        roleRepository.delete(role);
    }
}
