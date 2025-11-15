package com.my.repository.impl;

import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresqlUserRepositoryImpl extends PostgresqlBaseRepository implements UserRepository {

    public PostgresqlUserRepositoryImpl() {
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = String.format("SELECT id, email, username, password, role FROM %s.user", schema);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения всех пользователей", e);
        }
        return users;
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = String.format("SELECT id, email, username, password, role FROM %s.user WHERE id = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения пользователя по ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        String sql = String.format("INSERT INTO %s.user (id, email, username, password, role) VALUES (?, ?, ?, ?, ?)", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            Long id = getNextSequenceValue(Sequences.USER.getSequenceName());
            stmt.setLong(1, id);
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.executeUpdate();
            user.setId(id);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления пользователя", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = String.format("UPDATE %s.user SET email = ?, username = ?, password = ?, role = ? WHERE id = ?", schema);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().name());
            stmt.setLong(5, user.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Ошибка поиска пользователя по ID: " + user.getId());
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления пользователя", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = String.format("DELETE FROM %s.user WHERE id = ?", schema);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления пользователя с ID: " + id, e);
        }
    }

    @Override
    public boolean isPresentByEmail(String email) {
        String sql = String.format("SELECT COUNT(*) FROM %s.user WHERE email = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки доступности email", e);
        }
        return false;
    }

    @Override
    public Optional<User> getByEmailAndPassword(String email, String password) {
        String sql = String.format("SELECT id, email, username, password, role FROM %s.user WHERE email = ? AND password = ?", schema);

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения пользователя по email и паролю", e);
        }
        return Optional.empty();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("password"),
                UserRole.valueOf(rs.getString("role"))
        );
    }
}
