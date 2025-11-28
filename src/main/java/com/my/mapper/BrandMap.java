package com.my.mapper;

import com.my.model.Brand;
import com.my.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrandMap {
    private final BrandService brandService;

    @Autowired
    public BrandMap(BrandService brandService) {
        this.brandService = brandService;
    }

    public Brand fromId(Long id) {
        return id != null ? brandService.getById(id) : null;
    }
}
