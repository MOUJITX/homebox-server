package com.moujitx.homebox.server.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SubscriptionDashboardResponse {

    private long activeSubscriptionCount;
    private BigDecimal monthlySpending;
    private java.util.List<UpcomingRenewal> upcomingRenewals;

    public SubscriptionDashboardResponse(long activeSubscriptionCount, BigDecimal monthlySpending,
                                          java.util.List<UpcomingRenewal> upcomingRenewals) {
        this.activeSubscriptionCount = activeSubscriptionCount;
        this.monthlySpending = monthlySpending;
        this.upcomingRenewals = upcomingRenewals;
    }

    @Getter
    @NoArgsConstructor
    public static class UpcomingRenewal {
        private Long id;
        private String name;
        private String platformName;
        private String platformLogoUrl;
        private LocalDate endDate;
    }
}
