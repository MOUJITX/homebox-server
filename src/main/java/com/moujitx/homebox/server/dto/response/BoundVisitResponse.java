package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.VisitInvoice;
import com.moujitx.homebox.server.enums.VisitSourceType;
import lombok.Getter;

@Getter
public class BoundVisitResponse {

    private Long id;
    private Long visitId;
    private String patientName;
    private VisitSourceType sourceType;
    private Long sourceId;

    public static BoundVisitResponse from(VisitInvoice binding) {
        BoundVisitResponse response = new BoundVisitResponse();
        response.id = binding.getId();
        response.visitId = binding.getVisit().getId();
        response.patientName = binding.getVisit().getPatientName();
        response.sourceType = binding.getSourceType();
        response.sourceId = binding.getSourceId();
        return response;
    }
}
