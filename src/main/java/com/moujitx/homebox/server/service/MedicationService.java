package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateMedicationReminderRequest;
import com.moujitx.homebox.server.dto.request.UpdateMedicationReminderRequest;
import com.moujitx.homebox.server.dto.response.MedicationReminderResponse;
import com.moujitx.homebox.server.entity.Good;
import com.moujitx.homebox.server.entity.MedicationReminder;
import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.enums.NotificationType;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodRepository;
import com.moujitx.homebox.server.repository.MedicationReminderRepository;
import com.moujitx.homebox.server.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationService {

    private static final String SOURCE_TYPE_MEDICATION = "MEDICATION";

    private final MedicationReminderRepository reminderRepository;
    private final GoodRepository goodRepository;
    private final NotificationRepository notificationRepository;
    private final WebhookService webhookService;

    // ────────────────────────── CRUD ──────────────────────────

    @Transactional(readOnly = true)
    public Page<MedicationReminderResponse> list(Pageable pageable, Boolean enabled) {
        Page<MedicationReminder> page = (enabled != null)
                ? reminderRepository.findByEnabled(enabled, pageable)
                : reminderRepository.findAllWithGood(pageable);
        return page.map(MedicationReminderResponse::from);
    }

    @Transactional(readOnly = true)
    public MedicationReminderResponse getById(Long id) {
        MedicationReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication reminder not found with id: " + id));
        return MedicationReminderResponse.from(reminder);
    }

    @Transactional
    public MedicationReminderResponse create(CreateMedicationReminderRequest request) {
        if (reminderRepository.existsByGoodIdAndCourseStartDateAndCourseEndDate(
                request.getGoodId(), request.getCourseStartDate(), request.getCourseEndDate())) {
            throw new ResourceAlreadyExistsException(
                    "A medication reminder already exists for this good and course period");
        }

        Good good = goodRepository.findById(request.getGoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + request.getGoodId()));

        MedicationReminder reminder = new MedicationReminder();
        reminder.setGood(good);
        reminder.setDosageMethod(request.getDosageMethod());
        reminder.setDosageQuantity(request.getDosageQuantity());
        reminder.setDosageUnit(request.getDosageUnit());
        reminder.setDosageNote(request.getDosageNote());
        reminder.setFrequencyHours(request.getFrequencyHours());
        reminder.setCourseStartDate(request.getCourseStartDate());
        reminder.setCourseEndDate(request.getCourseEndDate());
        reminder.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        return MedicationReminderResponse.from(reminderRepository.save(reminder));
    }

    @Transactional
    public MedicationReminderResponse update(Long id, UpdateMedicationReminderRequest request) {
        MedicationReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication reminder not found with id: " + id));

        if (request.getGoodId() != null) {
            Good good = goodRepository.findById(request.getGoodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Good not found with id: " + request.getGoodId()));
            reminder.setGood(good);
        }
        if (request.getDosageMethod() != null) {
            reminder.setDosageMethod(request.getDosageMethod().isEmpty() ? null : request.getDosageMethod());
        }
        if (request.getDosageQuantity() != null) {
            reminder.setDosageQuantity(request.getDosageQuantity().isEmpty() ? null : request.getDosageQuantity());
        }
        if (request.getDosageUnit() != null) {
            reminder.setDosageUnit(request.getDosageUnit().isEmpty() ? null : request.getDosageUnit());
        }
        if (request.getDosageNote() != null) {
            reminder.setDosageNote(request.getDosageNote().isEmpty() ? null : request.getDosageNote());
        }
        if (request.getFrequencyHours() != null) {
            reminder.setFrequencyHours(request.getFrequencyHours());
        }
        if (request.getCourseStartDate() != null) {
            reminder.setCourseStartDate(request.getCourseStartDate());
        }
        if (request.getCourseEndDate() != null) {
            reminder.setCourseEndDate(request.getCourseEndDate());
        }
        if (request.getEnabled() != null) {
            reminder.setEnabled(request.getEnabled());
        }

        return MedicationReminderResponse.from(reminderRepository.save(reminder));
    }

    @Transactional
    public void delete(Long id) {
        MedicationReminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication reminder not found with id: " + id));
        reminderRepository.delete(reminder);
    }

    // ────────────────────────── Scheduled Check ──────────────────────────

    @Transactional
    public void checkAndNotify() {
        LocalDate today = LocalDate.now();
        int currentHour = LocalTime.now().getHour();

        log.info("Running medication reminder check for date={} hour={}", today, currentHour);

        List<MedicationReminder> enabledReminders = reminderRepository.findAllEnabledWithGood();

        List<Notification> newNotifications = new ArrayList<>();

        for (MedicationReminder reminder : enabledReminders) {
            // Auto-disable if past course end date
            if (today.isAfter(reminder.getCourseEndDate())) {
                reminder.setEnabled(false);
                reminderRepository.save(reminder);
                log.info("Auto-disabled medication reminder {} (past end date {})", reminder.getId(), reminder.getCourseEndDate());
                continue;
            }

            // Check if reminder is in course range and current hour matches
            if (today.isBefore(reminder.getCourseStartDate()) || today.isAfter(reminder.getCourseEndDate())) {
                continue;
            }

            if (!hourMatches(reminder.getFrequencyHours(), currentHour)) {
                continue;
            }

            // Create notification with dedup
            Good good = reminder.getGood();
            String title = "用药提醒";
            String content = buildReminderContent(good, reminder);

            notificationRepository.insert(
                    NotificationType.MEDICATION_REMINDER.name(),
                    title,
                    content,
                    SOURCE_TYPE_MEDICATION,
                    reminder.getId(),
                    today
            );

            Notification notification = new Notification();
            notification.setType(NotificationType.MEDICATION_REMINDER);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setSourceType(SOURCE_TYPE_MEDICATION);
            notification.setSourceId(reminder.getId());
            notification.setNotifyDate(today);
            notification.setCreatedAt(LocalDateTime.now());
            newNotifications.add(notification);
        }

        if (!newNotifications.isEmpty()) {
            log.info("Created {} medication reminder notifications", newNotifications.size());
            for (Notification notification : newNotifications) {
                webhookService.send(notification);
            }
        }
    }

    private boolean hourMatches(String frequencyHours, int currentHour) {
        for (String part : frequencyHours.split(",")) {
            try {
                if (Integer.parseInt(part.trim()) == currentHour) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private String buildReminderContent(Good good, MedicationReminder reminder) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(good.getBrand().getBrandName())
                .append("-").append(good.getProductName()).append("】");

        sb.append("服用方式：").append(reminder.getDosageMethod() != null ? reminder.getDosageMethod() : "按需");
        sb.append("，每次 ").append(reminder.getDosageQuantity() != null ? reminder.getDosageQuantity() : "—");
        sb.append(" ").append(reminder.getDosageUnit() != null ? reminder.getDosageUnit() : "");

        if (reminder.getDosageNote() != null && !reminder.getDosageNote().isEmpty()) {
            sb.append("。备注：").append(reminder.getDosageNote());
        }

        return sb.toString();
    }
}
