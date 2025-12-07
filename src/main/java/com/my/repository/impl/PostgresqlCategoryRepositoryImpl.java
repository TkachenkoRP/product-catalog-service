package com.my.repository.impl;

import com.my.model.Category;
import com.my.repository.CategoryRepository;
import com.my.util.SequenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class PostgresqlCategoryRepositoryImpl implements CategoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SequenceGenerator sequenceGenerator;

    @Value("${spring.datasource.hikari.schema}")
    private String schema;

    @Autowired
    public PostgresqlCategoryRepositoryImpl(DataSource dataSource, SequenceGenerator sequenceGenerator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sequenceGenerator = sequenceGenerator;
    }

    private static final String CATEGORY_SEQUENCE = "category_seq";

    private static final String SELECT_ALL_SQL = "SELECT id, name FROM %s.category ORDER BY id";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name FROM %s.category WHERE id = ?";
    private static final String INSERT_SQL = "INSERT INTO %s.category (id, name) VALUES (?, ?)";
    private static final String UPDATE_SQL = "UPDATE %s.category SET name = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM %s.category WHERE id = ?";
    private static final String EXISTS_BY_NAME_SQL = "SELECT COUNT(*) FROM %s.category WHERE LOWER(name) = LOWER(?)";

    public PostgresqlCategoryRepositoryImpl(JdbcTemplate jdbcTemplate, SequenceGenerator sequenceGenerator) {
        super();
        this.jdbcTemplate = jdbcTemplate;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> findAll() {
        String sql = String.format(SELECT_ALL_SQL, schema);
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToCategory(rs));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findById(Long id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try {
            Category category = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapResultSetToCategory(rs), id);
            return Optional.ofNullable(category);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public Category save(Category category) {
        String sql = String.format(INSERT_SQL, schema);
        Long nextSequenceValue = sequenceGenerator.getNextSequenceValue(CATEGORY_SEQUENCE);
        jdbcTemplate.update(sql,
                nextSequenceValue,
                category.getName()
        );
        category.setId(nextSequenceValue);
        return category;
    }

    @Transactional
    @Override
    public Category update(Category category) {
        String sql = String.format(UPDATE_SQL, schema);
        jdbcTemplate.update(sql,
                category.getName(),
                category.getId()
        );
        return category;
    }

    @Transactional
    @Override
    public boolean deleteById(Long id) {
        String sql = String.format(DELETE_SQL, schema);
        int update = jdbcTemplate.update(sql, id);
        return update > 0;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNameIgnoreCase(String categoryName) {
        String sql = String.format(EXISTS_BY_NAME_SQL, schema);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryName);
        return count != null && count > 0;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
