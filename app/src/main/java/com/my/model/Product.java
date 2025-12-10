package com.my.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Сущность, представляющая товар.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    private Long id;
    private String name;
    private Long categoryId;
    private Long brandId;
    private Double price;
    private Integer stock;

    public Product(String name, Long categoryId, Long brandId, double price, int stock) {
        this.name = name;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", categoryId=" + categoryId +
               ", brandId=" + brandId +
               ", price=" + price +
               ", stock=" + stock +
               '}';
    }
}
