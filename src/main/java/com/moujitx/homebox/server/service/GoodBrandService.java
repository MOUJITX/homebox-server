package com.moujitx.homebox.server.service;

import com.moujitx.homebox.server.dto.request.CreateGoodBrandRequest;
import com.moujitx.homebox.server.dto.request.UpdateGoodBrandRequest;
import com.moujitx.homebox.server.dto.response.GoodBrandResponse;
import com.moujitx.homebox.server.entity.GoodBrand;
import com.moujitx.homebox.server.exception.OperationNotAllowedException;
import com.moujitx.homebox.server.exception.ResourceAlreadyExistsException;
import com.moujitx.homebox.server.exception.ResourceNotFoundException;
import com.moujitx.homebox.server.repository.GoodBrandRepository;
import com.moujitx.homebox.server.repository.GoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoodBrandService {

    private final GoodBrandRepository brandRepository;
    private final GoodRepository goodRepository;

    public Page<GoodBrandResponse> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable).map(GoodBrandResponse::from);
    }

    public GoodBrandResponse getBrandById(Long id) {
        GoodBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return GoodBrandResponse.from(brand);
    }

    @Transactional
    public GoodBrandResponse createBrand(CreateGoodBrandRequest request) {
        if (brandRepository.existsByBrandName(request.getBrandName())) {
            throw new ResourceAlreadyExistsException("Brand already exists: " + request.getBrandName());
        }

        GoodBrand brand = new GoodBrand(request.getBrandName(), request.getCompanyName());
        return GoodBrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public GoodBrandResponse updateBrand(Long id, UpdateGoodBrandRequest request) {
        GoodBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (request.getBrandName() != null) {
            if (!request.getBrandName().equals(brand.getBrandName()) && brandRepository.existsByBrandName(request.getBrandName())) {
                throw new ResourceAlreadyExistsException("Brand already exists: " + request.getBrandName());
            }
            brand.setBrandName(request.getBrandName());
        }

        if (request.getCompanyName() != null) {
            brand.setCompanyName(request.getCompanyName().isEmpty() ? null : request.getCompanyName());
        }

        return GoodBrandResponse.from(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(Long id) {
        GoodBrand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (goodRepository.existsByBrandId(brand.getId())) {
            throw new OperationNotAllowedException("Cannot delete brand that is used by goods");
        }

        brandRepository.delete(brand);
    }
}
