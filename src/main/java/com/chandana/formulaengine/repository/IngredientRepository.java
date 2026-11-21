package com.chandana.formulaengine.repository;

import com.chandana.formulaengine.model.Ingredient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IngredientRepository {

    private static final RowMapper<Ingredient> MAPPER = (rs, rowNum) -> new Ingredient(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getBoolean("is_accord"));

    private final JdbcTemplate jdbc;

    public IngredientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Ingredient> findAll() {
        return jdbc.query("SELECT id, name, category, is_accord FROM ingredient ORDER BY is_accord, name", MAPPER);
    }
}
