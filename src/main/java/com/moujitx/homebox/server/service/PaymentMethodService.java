package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.PaymentMethodRequest;
import com.moujitx.homebox.server.dto.response.PaymentMethodResponse;
import com.moujitx.homebox.server.entity.FileRecord;
import com.moujitx.homebox.server.entity.PaymentMethod;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.FileRecordRepository;
import com.moujitx.homebox.server.repository.PaymentMethodRepository;
import com.moujitx.homebox.server.repository.SubscriptionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final FileRecordRepository fileRecordRepository;
    private final SubscriptionRecordRepository subscriptionRecordRepository;

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAll() {
        return paymentMethodRepository.findAll().stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        if (paymentMethodRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Payment method already exists: " + request.getName());
        }

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(request.getName());
        if (request.getLogoFileId() != null) {
            FileRecord logoFile = fileRecordRepository.findById(request.getLogoFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + request.getLogoFileId()));
            paymentMethod.setLogoFile(logoFile);
        }

        return PaymentMethodResponse.from(paymentMethodRepository.save(paymentMethod));
    }

    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(paymentMethod.getName()) && paymentMethodRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Payment method already exists: " + request.getName());
            }
            paymentMethod.setName(request.getName());
        }

        if (request.getLogoFileId() != null) {
            FileRecord logoFile = fileRecordRepository.findById(request.getLogoFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + request.getLogoFileId()));
            paymentMethod.setLogoFile(logoFile);
        }

        return PaymentMethodResponse.from(paymentMethodRepository.save(paymentMethod));
    }

    @Transactional
    public void delete(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment method not found with id: " + id);
        }

        if (subscriptionRecordRepository.existsByPaymentMethodId(id)) {
            throw new OperationNotAllowedException("Cannot delete payment method that is used by subscription records");
        }

        paymentMethodRepository.deleteById(id);
    }
}
