package com.my.repository.impl;

import com.my.model.Category;
import com.my.repository.CategoryRepository;
import com.my.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresqlCategoryRepositoryImpl extends PostgresqlBaseRepository implements CategoryRepository {

    private final Connection connection;

    public PostgresqlCategoryRepositoryImpl() throws SQLException {
        this(DBUtil.getConnection());
    }

    public PostgresqlCategoryRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = String.format("SELECT id, name FROM %s.category ORDER BY id", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения категорий", e);
        }
        return categories;
    }

    @Override
    public Optional<Category> getById(Long id) {
        String sql = String.format("SELECT id, name FROM %s.category WHERE id = ?", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения категории с ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            return insert(category);
        } else {
            return update(category);
        }
    }

    private Category insert(Category category) {
        String sql = String.format("INSERT INTO %s.category (id, name) VALUES (?, ?)", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Long id = getNextSequenceValue(Sequences.CATEGORY.getSequenceName());
            stmt.setLong(1, id);
            stmt.setString(2, category.getName());
            stmt.executeUpdate();
            category.setId(id);
            return category;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления категории", e);
        }
    }

    @Override
    public Category update(Category category) {
        String sql = String.format("UPDATE %s.category SET name = ? WHERE id = ?", schema);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setLong(2, category.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Ошибка поиска категории с ID: " + category.getId());
            }
            return category;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления категории", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = String.format("DELETE FROM %s.category WHERE id = ?", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка уделения категории с ID: " + id, e);
        }
    }

    @Override
    public boolean existsByNameIgnoreCase(String categoryName) {
        String sql = String.format("SELECT COUNT(*) FROM %s.category WHERE LOWER(name) = LOWER(?)", schema);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки имени категории", e);
        }
        return false;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
