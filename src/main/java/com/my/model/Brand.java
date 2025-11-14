package com.my.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Brand {
    private Long id;
    private String name;

    public Brand(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Brand{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Brand brand)) return false;
        return Objects.equals(id, brand.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
