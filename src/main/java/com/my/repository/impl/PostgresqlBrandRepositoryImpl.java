package com.my.repository.impl;

import com.my.model.Brand;
import com.my.repository.BrandRepository;
import com.my.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresqlBrandRepositoryImpl extends PostgresqlBaseRepository implements BrandRepository {

    public PostgresqlBrandRepositoryImpl() throws SQLException {
        super(DBUtil.getConnection());
    }

    public PostgresqlBrandRepositoryImpl(Connection connection) {
        super(connection);
    }

    @Override
    public List<Brand> getAll() {
        List<Brand> brands = new ArrayList<>();
        String sql = String.format("SELECT id, name FROM %s.brand ORDER BY id", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                brands.add(mapResultSetToBrand(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки брендов", e);
        }
        return brands;
    }

    @Override
    public Optional<Brand> getById(Long id) {
        String sql = String.format("SELECT id, name FROM %s.brand WHERE id = ?", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBrand(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки бренда с id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Brand save(Brand brand) {
        if (brand.getId() == null) {
            return insert(brand);
        } else {
            return update(brand);
        }
    }

    private Brand insert(Brand brand) {
        String sql = String.format("INSERT INTO %s.brand (id, name) VALUES (?, ?)", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Long id = getNextSequenceValue(Sequences.BRAND.getSequenceName());
            stmt.setLong(1, id);
            stmt.setString(2, brand.getName());

            stmt.executeUpdate();
            brand.setId(id);
            return brand;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления бренда", e);
        }
    }

    @Override
    public Brand update(Brand brand) {
        String sql = String.format("UPDATE %s.brand SET name = ? WHERE id = ?", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, brand.getName());
            stmt.setLong(2, brand.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Ошибка поиска бренда с ID: " + brand.getId());
            }
            return brand;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления бренда", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = String.format("DELETE FROM %s.brand WHERE id = ?", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка уделения бренда с ID: " + id, e);
        }
    }

    @Override
    public boolean existsByNameIgnoreCase(String brandName) {
        String sql = String.format("SELECT COUNT(*) FROM %s.brand WHERE LOWER(name) = LOWER(?)", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, brandName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки доступности имени бренда", e);
        }
        return false;
    }

    private Brand mapResultSetToBrand(ResultSet rs) throws SQLException {
        return new Brand(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
