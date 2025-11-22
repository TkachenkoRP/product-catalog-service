package com.my.mapper;

import com.my.model.Brand;
import com.my.service.BrandService;
import com.my.service.impl.BrandServiceImpl;

public class BrandMap {
    private final BrandService brandService;

    public BrandMap() {
        this.brandService = new BrandServiceImpl();
    }

    public Brand fromId(Long id) {
        return id != null ? brandService.getById(id) : null;
    }
}
