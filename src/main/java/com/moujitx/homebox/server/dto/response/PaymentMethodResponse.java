package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.PaymentMethod;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentMethodResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentMethodResponse from(PaymentMethod paymentMethod) {
        PaymentMethodResponse r = new PaymentMethodResponse();
        r.id = paymentMethod.getId();
        r.name = paymentMethod.getName();
        if (paymentMethod.getLogoFile() != null) {
            r.logoUrl = OssUrlBuilder.build(
                    paymentMethod.getLogoFile().getStoredFilename(),
                    paymentMethod.getLogoFile().getOriginalFilename());
        }
        r.createdAt = paymentMethod.getCreatedAt();
        r.updatedAt = paymentMethod.getUpdatedAt();
        return r;
    }
}
