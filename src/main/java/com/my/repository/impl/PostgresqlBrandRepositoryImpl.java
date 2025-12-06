package com.my.repository.impl;

import com.my.model.Brand;
import com.my.repository.BrandRepository;
import com.my.util.SequenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class PostgresqlBrandRepositoryImpl implements BrandRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SequenceGenerator sequenceGenerator;
    private final BrandRowMapper brandRowMapper = new BrandRowMapper();

    @Value("${datasource.schema}")
    private String schema;

    @Autowired
    public PostgresqlBrandRepositoryImpl(DataSource dataSource, SequenceGenerator sequenceGenerator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sequenceGenerator = sequenceGenerator;
    }

    private static final String BRAND_SEQUENCE = "brand_seq";

    private static final String SELECT_ALL_SQL = "SELECT id, name FROM %s.brand ORDER BY id";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name FROM %s.brand WHERE id = ?";
    private static final String INSERT_SQL = "INSERT INTO %s.brand (id, name) VALUES (?, ?)";
    private static final String UPDATE_SQL = "UPDATE %s.brand SET name = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM %s.brand WHERE id = ?";
    private static final String EXISTS_BY_NAME_SQL = "SELECT COUNT(*) FROM %s.brand WHERE LOWER(name) = LOWER(?)";

    public PostgresqlBrandRepositoryImpl(JdbcTemplate jdbcTemplate, SequenceGenerator sequenceGenerator) {
        super();
        this.jdbcTemplate = jdbcTemplate;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Brand> getAll() {
        String sql = String.format(SELECT_ALL_SQL, schema);
        return jdbcTemplate.query(
                sql,
                brandRowMapper);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Brand> getById(Long id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try {
            Brand brand = jdbcTemplate.queryForObject(
                    sql,
                    brandRowMapper,
                    id);
            return Optional.ofNullable(brand);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public Brand save(Brand brand) {
        String sql = String.format(INSERT_SQL, schema);
        Long nextSequenceValue = sequenceGenerator.getNextSequenceValue(BRAND_SEQUENCE);
        jdbcTemplate.update(sql,
                nextSequenceValue,
                brand.getName()
        );
        brand.setId(nextSequenceValue);
        return brand;
    }

    @Transactional
    @Override
    public Brand update(Brand brand) {
        String sql = String.format(UPDATE_SQL, schema);
        jdbcTemplate.update(sql,
                brand.getName(),
                brand.getId()
        );
        return brand;
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
    public boolean existsByNameIgnoreCase(String brandName) {
        String sql = String.format(EXISTS_BY_NAME_SQL, schema);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, brandName);
        return count != null && count > 0;
    }

    @Transactional(readOnly = true)
    private static class BrandRowMapper implements RowMapper<Brand> {
        @Override
        public Brand mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Brand(
                    rs.getLong("id"),
                    rs.getString("name")
            );
        }
    }
}
