package com.my.repository.impl;

import com.my.exception.ProductCreationException;
import com.my.model.Product;
import com.my.model.ProductFilter;
import com.my.repository.ProductRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostgresqlProductRepositoryImpl implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SequenceGenerator sequenceGenerator;

    @Value("${spring.datasource.hikari.schema}")
    private String schema;

    @Autowired
    public PostgresqlProductRepositoryImpl(DataSource dataSource, SequenceGenerator sequenceGenerator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sequenceGenerator = sequenceGenerator;
    }

    private static final String PRODUCT_SEQUENCE = "product_seq";

    private static final String SELECT_ALL_BASE_SQL = "SELECT id, name, category_id, brand_id, price, stock FROM %s.product";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name, category_id, brand_id, price, stock FROM %s.product WHERE id = ?";
    private static final String INSERT_SQL = "INSERT INTO %s.product (id, name, category_id, brand_id, price, stock) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE %s.product SET name = ?, category_id = ?, brand_id = ?, price = ?, stock = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM %s.product WHERE id = ?";
    private static final String EXIST_BY_BRAND_ID = "SELECT EXISTS(SELECT 1 FROM %s.product WHERE brand_id = ?)";
    private static final String EXIST_BY_CATEGORY_ID = "SELECT EXISTS(SELECT 1 FROM %s.product WHERE category_id = ?)";

    private static final String CATEGORY_ID_CONDITION = "category_id = ?";
    private static final String BRAND_ID_CONDITION = "brand_id = ?";
    private static final String PRICE_MIN_CONDITION = "price >= ?";
    private static final String PRICE_MAX_CONDITION = "price < ?";
    private static final String STOCK_MIN_CONDITION = "stock >= ?";
    private static final String WHERE_CLAUSE = " WHERE ";
    private static final String AND_JOIN = " AND ";
    private static final String ORDER_BY_ID = " ORDER BY id";

    @Transactional(readOnly = true)
    @Override
    public List<Product> getAll(ProductFilter filter) {
        StringBuilder sql = new StringBuilder(
                String.format(SELECT_ALL_BASE_SQL, schema));

        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (filter != null) {
            if (filter.categoryId() != null) {
                conditions.add(CATEGORY_ID_CONDITION);
                parameters.add(filter.categoryId());
            }
            if (filter.brandId() != null) {
                conditions.add(BRAND_ID_CONDITION);
                parameters.add(filter.brandId());
            }
            if (filter.minPrice() != null) {
                conditions.add(PRICE_MIN_CONDITION);
                parameters.add(filter.minPrice());
            }
            if (filter.maxPrice() != null) {
                conditions.add(PRICE_MAX_CONDITION);
                parameters.add(filter.maxPrice());
            }
            if (filter.minStock() != null) {
                conditions.add(STOCK_MIN_CONDITION);
                parameters.add(filter.minStock());
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(WHERE_CLAUSE);
            sql.append(String.join(AND_JOIN, conditions));
        }

        sql.append(ORDER_BY_ID);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapResultSetToProduct(rs), parameters.toArray());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Product> getById(Long id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try {
            Product product = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapResultSetToProduct(rs), id);
            return Optional.ofNullable(product);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public Product save(Product product) {
        String sql = String.format(INSERT_SQL, schema);
        Long nextSequenceValue = sequenceGenerator.getNextSequenceValue(PRODUCT_SEQUENCE);
        try {
            jdbcTemplate.update(sql,
                    nextSequenceValue,
                    product.getName(),
                    product.getCategoryId(),
                    product.getBrandId(),
                    product.getPrice(),
                    product.getStock()
            );
        } catch (DataAccessException e) {
            if (e.getMessage().contains("violates foreign key constraint")) {
                if (e.getMessage().contains("fk_product_brand")) {
                    throw new ProductCreationException("Ошибка добавления товара: указан несуществующий бренд");
                } else if (e.getMessage().contains("fk_product_category")) {
                    throw new ProductCreationException("Ошибка добавления товара: указана несуществующая категория");
                }
            }
            throw new ProductCreationException("Ошибка добавления товара", e);
        }
        product.setId(nextSequenceValue);
        return product;
    }

    @Transactional
    @Override
    public Product update(Product product) {
        String sql = String.format(UPDATE_SQL, schema);
        try {
            jdbcTemplate.update(sql,
                    product.getName(),
                    product.getCategoryId(),
                    product.getBrandId(),
                    product.getPrice(),
                    product.getStock(),
                    product.getId()
            );
        } catch (DataAccessException e) {
            if (e.getMessage().contains("violates foreign key constraint")) {
                if (e.getMessage().contains("fk_product_brand")) {
                    throw new ProductCreationException("Ошибка обновления товара: указан несуществующий бренд");
                } else if (e.getMessage().contains("fk_product_category")) {
                    throw new ProductCreationException("Ошибка обновления товара: указана несуществующая категория");
                }
            }
            throw new ProductCreationException("Ошибка обновления товара", e);
        }
        return product;
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
    public boolean existsByBrandId(Long brandId) {
        String sql = String.format(EXIST_BY_BRAND_ID, schema);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, brandId));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCategoryId(Long categoryId) {
        String sql = String.format(EXIST_BY_CATEGORY_ID, schema);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, categoryId));
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
