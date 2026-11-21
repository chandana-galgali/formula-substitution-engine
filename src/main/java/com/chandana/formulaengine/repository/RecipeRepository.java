package com.chandana.formulaengine.repository;

import com.chandana.formulaengine.model.ReplacementRecipe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RecipeRepository {

    private final JdbcTemplate jdbc;

    public RecipeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * One JOIN returns a row per (recipe, part). Rows are grouped back into
     * recipes in Java, keeping the SQL ORDER BY.
     */
    public List<ReplacementRecipe> findAll() {
        record Row(long id, String target, int priority, String note, String ingredientId, double fraction) {
        }

        List<Row> rows = jdbc.query("""
                SELECT r.id, r.target, r.priority, r.note, p.ingredient_id, p.fraction
                FROM replacement_recipe r
                JOIN replacement_part p ON p.recipe_id = r.id
                ORDER BY r.target, r.priority, p.position
                """, (rs, n) -> new Row(
                rs.getLong("id"),
                rs.getString("target"),
                rs.getInt("priority"),
                rs.getString("note"),
                rs.getString("ingredient_id"),
                rs.getBigDecimal("fraction").doubleValue()));

        Map<Long, List<ReplacementRecipe.Part>> parts = new LinkedHashMap<>();
        Map<Long, Row> heads = new LinkedHashMap<>();
        for (Row row : rows) {
            heads.putIfAbsent(row.id(), row);
            parts.computeIfAbsent(row.id(), k -> new ArrayList<>())
                    .add(new ReplacementRecipe.Part(row.ingredientId(), row.fraction()));
        }

        List<ReplacementRecipe> recipes = new ArrayList<>();
        heads.forEach((id, h) -> recipes.add(
                new ReplacementRecipe(id, h.target(), h.priority(), List.copyOf(parts.get(id)), h.note())));
        return recipes;
    }
}
