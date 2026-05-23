package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.SubscriptionRequest;
import com.moujitx.homebox.server.dto.response.SubscriptionDetailResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionRecordResponse;
import com.moujitx.homebox.server.dto.response.SubscriptionResponse;
import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.entity.Platform;
import com.moujitx.homebox.server.entity.Subscription;
import com.moujitx.homebox.server.entity.SubscriptionRecord;
import com.moujitx.homebox.server.enums.NotificationType;
import com.moujitx.homebox.server.enums.SubscriptionStatus;
import com.moujitx.homebox.server.enums.SubscriptionType;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.NotificationRepository;
import com.moujitx.homebox.server.repository.PlatformRepository;
import com.moujitx.homebox.server.repository.SubscriptionRecordRepository;
import com.moujitx.homebox.server.repository.SubscriptionRepository;
import com.moujitx.homebox.server.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRecordRepository recordRepository;
    private final PlatformRepository platformRepository;
    private final NotificationRepository notificationRepository;
    private final WebhookService webhookService;

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getSubscriptions(String search, SubscriptionType type,
                                                        SubscriptionStatus status, Long platformId,
                                                        Pageable pageable) {
        String searchParam = StringUtil.normalizeSearch(search);
        Page<Subscription> page = subscriptionRepository.findWithFilters(searchParam, type, status, platformId, pageable);

        List<SubscriptionResponse> responses = page.getContent().stream()
                .map(sub -> {
                    SubscriptionResponse r = SubscriptionResponse.from(sub);
                    SubscriptionRecord latest = recordRepository.findFirstBySubscriptionIdOrderByRecordDateDesc(sub.getId()).orElse(null);
                    if (latest != null) {
                        r.setLatestRecordDate(latest.getRecordDate());
                        r.setLatestRecordAmount(latest.getAmount());
                        r.setLatestRecordEndDate(latest.getEndDate());
                    }
                    return r;
                })
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SubscriptionDetailResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        List<SubscriptionRecordResponse> records = recordRepository.findBySubscriptionIdOrderByRecordDateDesc(id)
                .stream()
                .map(SubscriptionRecordResponse::from)
                .toList();

        return SubscriptionDetailResponse.from(subscription, records);
    }

    @Transactional
    public SubscriptionDetailResponse createSubscription(SubscriptionRequest request) {
        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + request.getPlatformId()));

        Subscription subscription = new Subscription();
        subscription.setName(request.getName());
        subscription.setDescription(request.getDescription());
        subscription.setSubscriptionType(request.getSubscriptionType());
        subscription.setBillingMode(request.getBillingMode());
        subscription.setPlatform(platform);
        subscription.setStatus(request.getStatus() != null ? request.getStatus() : SubscriptionStatus.ACTIVE);
        subscription.setRenewNoticeDays(request.getRenewNoticeDays() != null ? request.getRenewNoticeDays() : 7);
        subscription.setNote(request.getNote());

        Subscription saved = subscriptionRepository.save(subscription);
        return SubscriptionDetailResponse.from(saved, List.of());
    }

    @Transactional
    public SubscriptionDetailResponse updateSubscription(Long id, SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (request.getName() != null) {
            subscription.setName(request.getName());
        }
        if (request.getDescription() != null) {
            subscription.setDescription(request.getDescription().isEmpty() ? null : request.getDescription());
        }
        if (request.getSubscriptionType() != null) {
            subscription.setSubscriptionType(request.getSubscriptionType());
        }
        if (request.getBillingMode() != null) {
            subscription.setBillingMode(request.getBillingMode());
        }
        if (request.getPlatformId() != null) {
            Platform platform = platformRepository.findById(request.getPlatformId())
                    .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + request.getPlatformId()));
            subscription.setPlatform(platform);
        }
        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }
        if (request.getRenewNoticeDays() != null) {
            subscription.setRenewNoticeDays(request.getRenewNoticeDays());
        }
        if (request.getNote() != null) {
            subscription.setNote(request.getNote().isEmpty() ? null : request.getNote());
        }

        Subscription saved = subscriptionRepository.save(subscription);
        List<SubscriptionRecordResponse> records = recordRepository.findBySubscriptionIdOrderByRecordDateDesc(id)
                .stream()
                .map(SubscriptionRecordResponse::from)
                .toList();
        return SubscriptionDetailResponse.from(saved, records);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (!subscription.getRecords().isEmpty()) {
            throw new OperationNotAllowedException("Cannot delete subscription that has records");
        }

        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void checkAndNotify() {
        log.info("Running subscription renewal check");
        List<Subscription> subscriptions = subscriptionRepository.findActivePeriodicSubscriptions();
        LocalDate today = LocalDate.now();
        List<Notification> newNotifications = new ArrayList<>();

        for (Subscription sub : subscriptions) {
            SubscriptionRecord latest = recordRepository.findFirstBySubscriptionIdOrderByRecordDateDesc(sub.getId()).orElse(null);
            if (latest == null || latest.getEndDate() == null) {
                continue;
            }

            int noticeDays = sub.getRenewNoticeDays() != null ? sub.getRenewNoticeDays() : 7;
            LocalDate deadline = today.plusDays(noticeDays);

            String title;
            String content;

            if (!latest.getEndDate().isBefore(today) && !latest.getEndDate().isAfter(deadline)) {
                title = "续费提醒";
                content = "您的订阅「" + sub.getName() + "」将于 " + latest.getEndDate() + " 到期，请及时续费";
            } else if (latest.getEndDate().equals(today.minusDays(1))) {
                title = "续费提醒";
                content = "您的订阅「" + sub.getName() + "」已于昨天（" + latest.getEndDate() + "）到期，请及时续费";
            } else {
                continue;
            }

            notificationRepository.insert(
                    NotificationType.SUBSCRIPTION_RENEWAL.name(),
                    title, content, "SUBSCRIPTION", sub.getId(), today);

            Notification notification = new Notification();
            notification.setType(NotificationType.SUBSCRIPTION_RENEWAL);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setSourceType("SUBSCRIPTION");
            notification.setSourceId(sub.getId());
            notification.setNotifyDate(today);
            notification.setCreatedAt(LocalDateTime.now());
            newNotifications.add(notification);
        }

        if (!newNotifications.isEmpty()) {
            log.info("Created {} subscription renewal notifications", newNotifications.size());
            for (Notification notification : newNotifications) {
                webhookService.send(notification);
            }
        }
    }
}
