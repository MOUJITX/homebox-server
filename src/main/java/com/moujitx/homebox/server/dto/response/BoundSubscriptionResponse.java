package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.SubscriptionRecordInvoice;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

@Getter
public class BoundSubscriptionResponse {

    private Long id;
    private Long subscriptionId;
    private String subscriptionName;
    private Long platformId;
    private String platformName;
    private String platformLogoUrl;

    public static BoundSubscriptionResponse from(SubscriptionRecordInvoice binding) {
        BoundSubscriptionResponse response = new BoundSubscriptionResponse();
        response.id = binding.getId();
        response.subscriptionId = binding.getRecord().getSubscription().getId();
        response.subscriptionName = binding.getRecord().getSubscription().getName();
        response.platformId = binding.getRecord().getSubscription().getPlatform().getId();
        response.platformName = binding.getRecord().getSubscription().getPlatform().getName();
        if (binding.getRecord().getSubscription().getPlatform().getLogoFile() != null) {
            response.platformLogoUrl = OssUrlBuilder.build(
                    binding.getRecord().getSubscription().getPlatform().getLogoFile().getStoredFilename(),
                    binding.getRecord().getSubscription().getPlatform().getLogoFile().getOriginalFilename());
        }
        return response;
    }
}
