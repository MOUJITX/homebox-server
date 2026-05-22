package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.PlatformRequest;
import com.moujitx.homebox.server.dto.response.PlatformResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.Platform;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import com.moujitx.homebox.server.repository.PlatformRepository;
import com.moujitx.homebox.server.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformService {

    private final PlatformRepository platformRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FileRecordRepository fileRecordRepository;

    @Transactional(readOnly = true)
    public List<PlatformResponse> getAll() {
        return platformRepository.findAll().stream()
                .map(PlatformResponse::from)
                .toList();
    }

    @Transactional
    public PlatformResponse create(PlatformRequest request) {
        if (platformRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Platform already exists: " + request.getName());
        }

        Platform platform = new Platform();
        platform.setName(request.getName());
        platform.setWebsite(request.getWebsite());
        if (request.getLogoFileId() != null) {
            FileRecord logoFile = fileRecordRepository.findById(request.getLogoFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + request.getLogoFileId()));
            platform.setLogoFile(logoFile);
        }

        return PlatformResponse.from(platformRepository.save(platform));
    }

    @Transactional
    public PlatformResponse update(Long id, PlatformRequest request) {
        Platform platform = platformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(platform.getName()) && platformRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Platform already exists: " + request.getName());
            }
            platform.setName(request.getName());
        }

        if (request.getWebsite() != null) {
            platform.setWebsite(request.getWebsite().isEmpty() ? null : request.getWebsite());
        }

        if (request.getLogoFileId() != null) {
            FileRecord logoFile = fileRecordRepository.findById(request.getLogoFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + request.getLogoFileId()));
            platform.setLogoFile(logoFile);
        }

        return PlatformResponse.from(platformRepository.save(platform));
    }

    @Transactional
    public void delete(Long id) {
        if (!platformRepository.existsById(id)) {
            throw new ResourceNotFoundException("Platform not found with id: " + id);
        }

        if (subscriptionRepository.existsByPlatformId(id)) {
            throw new OperationNotAllowedException("Cannot delete platform that is used by subscriptions");
        }

        platformRepository.deleteById(id);
    }
}
