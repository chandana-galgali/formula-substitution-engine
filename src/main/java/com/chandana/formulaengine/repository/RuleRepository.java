package com.chandana.formulaengine.repository;

import com.chandana.formulaengine.model.SubstitutionRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class RuleRepository {

    private static final RowMapper<SubstitutionRule> MAPPER = (rs, rowNum) -> new SubstitutionRule(
            rs.getLong("id"),
            rs.getString("ingredient_a"),
            rs.getString("ingredient_b"),
            rs.getString("result_ingredient"),
            rs.getString("reason"),
            rs.getString("note"));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    public RuleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc)
                .withTableName("substitution_rule")
                .usingColumns("ingredient_a", "ingredient_b", "result_ingredient", "reason", "note")
                .usingGeneratedKeyColumns("id");
    }

    /** Ordered by id so "first rule wins" is the same in the HashMap and the linear scan. */
    public List<SubstitutionRule> findAll() {
        return jdbc.query("""
                SELECT id, ingredient_a, ingredient_b, result_ingredient, reason, note
                FROM substitution_rule
                ORDER BY id
                """, MAPPER);
    }

    public SubstitutionRule insert(String a, String b, String result, String reason, String note) {
        Number id = insert.executeAndReturnKey(Map.of(
                "ingredient_a", a,
                "ingredient_b", b,
                "result_ingredient", result,
                "reason", reason,
                "note", note == null ? "" : note));
        return new SubstitutionRule(id.longValue(), a, b, result, reason, note);
    }
}
