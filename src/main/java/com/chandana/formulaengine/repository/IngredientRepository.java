package com.chandana.formulaengine.repository;

import com.chandana.formulaengine.model.Ingredient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class IngredientRepository {

    private static final RowMapper<Ingredient> MAPPER = (rs, rowNum) -> {
        BigDecimal max = rs.getBigDecimal("max_percent");      // NULL means "no limit"
        return new Ingredient(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getBoolean("is_accord"),
                rs.getString("status"),
                max == null ? null : max.doubleValue(),
                rs.getString("regulatory_note"));
    };

    private final JdbcTemplate jdbc;

    public IngredientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Ingredient> findAll() {
        return jdbc.query("""
                SELECT id, name, category, is_accord, status, max_percent, regulatory_note
                FROM ingredient
                ORDER BY is_accord, name
                """, MAPPER);
    }
}
