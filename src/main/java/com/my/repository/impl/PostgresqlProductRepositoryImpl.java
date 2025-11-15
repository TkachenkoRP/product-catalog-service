package com.my.repository.impl;

import com.my.model.Product;
import com.my.repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresqlProductRepositoryImpl extends PostgresqlBaseRepository implements ProductRepository {


    public PostgresqlProductRepositoryImpl() {
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        String sql = String.format("SELECT id, name, category_id, brand_id, price, stock FROM %s.product ORDER BY id", schema);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения продуктов", e);
        }
        return products;
    }

    @Override
    public Optional<Product> getById(Long id) {
        String sql = String.format("SELECT id, name, category_id, brand_id, price, stock FROM %s.product WHERE id = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения продукта по ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    private Product insert(Product product) {
        String sql = String.format("INSERT INTO %s.product (id, name, category_id, brand_id, price, stock) VALUES (?, ?, ?, ?, ?, ?)", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            Long id = getNextSequenceValue(Sequences.PRODUCT.getSequenceName());
            stmt.setLong(1, id);
            stmt.setString(2, product.getName());
            stmt.setLong(3, product.getCategoryId());
            stmt.setLong(4, product.getBrandId());
            stmt.setDouble(5, product.getPrice());
            stmt.setInt(6, product.getStock());
            stmt.executeUpdate();
            product.setId(id);
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления продукта", e);
        }
    }

    @Override
    public Product update(Product product) {
        String sql = String.format("UPDATE %s.product SET name = ?, category_id = ?, brand_id = ?, price = ?, stock = ? WHERE id = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setLong(2, product.getCategoryId());
            stmt.setLong(3, product.getBrandId());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setLong(6, product.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Ошибка поиска продукта с ID: " + product.getId());
            }
            return product;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления продукта", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = String.format("DELETE FROM %s.product WHERE id = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления продукта с ID: " + id, e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("category_id"),
                rs.getLong("brand_id"),
                rs.getDouble("price"),
                rs.getInt("stock")
        );
    }
}
