package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.MedicationReminder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MedicationReminderResponse {

    private Long id;
    private Long goodId;
    private String productName;
    private String categoryName;
    private String brandName;
    private String dosageMethod;
    private String dosageQuantity;
    private String dosageUnit;
    private String dosageNote;
    private String frequencyHours;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MedicationReminderResponse from(MedicationReminder reminder) {
        return new MedicationReminderResponse(
                reminder.getId(),
                reminder.getGood().getId(),
                reminder.getGood().getProductName(),
                reminder.getGood().getCategory().getName(),
                reminder.getGood().getBrand().getBrandName(),
                reminder.getDosageMethod(),
                reminder.getDosageQuantity(),
                reminder.getDosageUnit(),
                reminder.getDosageNote(),
                reminder.getFrequencyHours(),
                reminder.getCourseStartDate(),
                reminder.getCourseEndDate(),
                reminder.isEnabled(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }
}
