package com.my.dto;

import com.my.model.Brand;
import com.my.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {
    private Long id;
    private String name;
    private Category category;
    private Brand brand;
    private Double price;
    private Integer stock;
}
