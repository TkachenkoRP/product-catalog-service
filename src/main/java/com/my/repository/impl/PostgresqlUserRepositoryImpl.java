package com.my.repository.impl;

import com.my.model.User;
import com.my.model.UserRole;
import com.my.repository.UserRepository;
import com.my.util.SequenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class PostgresqlUserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SequenceGenerator sequenceGenerator;

    @Value("${datasource.schema}")
    private String schema;

    @Autowired
    public PostgresqlUserRepositoryImpl(DataSource dataSource, SequenceGenerator sequenceGenerator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sequenceGenerator = sequenceGenerator;
    }

    private static final String USER_SEQUENCE = "user_seq";

    private static final String SELECT_ALL_SQL = "SELECT id, email, username, password, role FROM %s.user";
    private static final String SELECT_BY_ID_SQL = "SELECT id, email, username, password, role FROM %s.user WHERE id = ?";
    private static final String INSERT_SQL = "INSERT INTO %s.user (id, email, username, password, role) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE %s.user SET email = ?, username = ?, password = ?, role = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM %s.user WHERE id = ?";
    private static final String EXISTS_BY_EMAIL_SQL = "SELECT COUNT(*) FROM %s.user WHERE email = ?";
    private static final String SELECT_BY_EMAIL_PASSWORD_SQL = "SELECT id, email, username, password, role FROM %s.user WHERE email = ? AND password = ?";
    private static final String SELECT_BY_ROLE_SQL = "SELECT id, email, username, password, role FROM %s.user WHERE role = ?";

    @Override
    public List<User> getAll() {
        String sql = String.format(SELECT_ALL_SQL, schema);
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToUser(rs));
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try {
            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapResultSetToUser(rs), id);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        String sql = String.format(INSERT_SQL, schema);
        Long nextSequenceValue = sequenceGenerator.getNextSequenceValue(USER_SEQUENCE);
        jdbcTemplate.update(sql,
                nextSequenceValue,
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name()
        );
        user.setId(nextSequenceValue);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = String.format(UPDATE_SQL, schema);
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name(),
                user.getId()
        );
        return user;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = String.format(DELETE_SQL, schema);
        int update = jdbcTemplate.update(sql, id);
        return update > 0;
    }

    @Override
    public boolean isPresentByEmail(String email) {
        String sql = String.format(EXISTS_BY_EMAIL_SQL, schema);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> getByEmailAndPassword(String email, String password) {
        String sql = String.format(SELECT_BY_EMAIL_PASSWORD_SQL, schema);
        try {
            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapResultSetToUser(rs), email, password);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String sql = String.format(SELECT_BY_ROLE_SQL, schema);
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToUser(rs), role.name());
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
