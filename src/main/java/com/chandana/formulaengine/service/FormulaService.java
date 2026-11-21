package com.chandana.formulaengine.service;

import com.chandana.formulaengine.engine.HashMapRuleLookup;
import com.chandana.formulaengine.engine.LinearScanRuleLookup;
import com.chandana.formulaengine.engine.LookupStats;
import com.chandana.formulaengine.engine.PairKey;
import com.chandana.formulaengine.engine.RuleLookup;
import com.chandana.formulaengine.engine.SubstitutionEngine;
import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.Ingredient;
import com.chandana.formulaengine.model.SubstitutionResult;
import com.chandana.formulaengine.model.SubstitutionRule;
import com.chandana.formulaengine.repository.IngredientRepository;
import com.chandana.formulaengine.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads the rule book from SQL once, keeps it in memory as a HashMap,
 * and runs formulas through the engine.
 */
@Service
public class FormulaService {

    public enum Strategy { HASHMAP, LINEAR_SCAN }

    private static final Logger log = LoggerFactory.getLogger(FormulaService.class);
    private static final Set<String> REASONS = Set.of("CONSOLIDATION", "COST", "SUPPLY", "COMPLIANCE");

    private final IngredientRepository ingredientRepository;
    private final RuleRepository ruleRepository;

    // Swapped as a whole on reload, so readers always see a consistent snapshot.
    private volatile Snapshot snapshot;

    private record Snapshot(Map<String, Ingredient> ingredients,
                            List<SubstitutionRule> rules,
                            HashMapRuleLookup hashMap,
                            LinearScanRuleLookup linear) {
    }

    public FormulaService(IngredientRepository ingredientRepository, RuleRepository ruleRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ruleRepository = ruleRepository;
        reload();
    }

    public final synchronized void reload() {
        Map<String, Ingredient> ingredients = new LinkedHashMap<>();
        ingredientRepository.findAll().forEach(i -> ingredients.put(i.id(), i));
        List<SubstitutionRule> rules = ruleRepository.findAll();
        HashMapRuleLookup hashMap = new HashMapRuleLookup(rules);
        hashMap.warnings().forEach(log::warn);
        snapshot = new Snapshot(Map.copyOf(ingredients), rules, hashMap, new LinearScanRuleLookup(rules));
        log.info("Loaded {} ingredients and {} substitution rules into the HashMap index",
                ingredients.size(), hashMap.size());
    }

    public List<Ingredient> ingredients() {
        return ingredientRepository.findAll();
    }

    public List<SubstitutionRule> rules() {
        return snapshot.rules();
    }

    public int ruleCount() {
        return snapshot.hashMap().size();
    }

    public SubstitutionResult optimize(List<FormulaLine> lines, Strategy strategy) {
        Snapshot s = snapshot;
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Formula must contain at least one ingredient");
        }
        for (FormulaLine line : lines) {
            if (line.ingredientId() == null || !s.ingredients().containsKey(line.ingredientId())) {
                throw new IllegalArgumentException("Unknown ingredient: " + line.ingredientId());
            }
            if (line.percentage() <= 0 || line.percentage() > 100) {
                throw new IllegalArgumentException("Percentage for " + line.ingredientId() + " must be between 0 and 100");
            }
        }
        RuleLookup lookup = strategy == Strategy.LINEAR_SCAN ? s.linear() : s.hashMap();
        return new SubstitutionEngine(lookup).optimize(lines);
    }

    public synchronized SubstitutionRule addRule(String a, String b, String result, String reason, String note) {
        Snapshot s = snapshot;
        for (String id : new String[]{a, b, result}) {
            if (id == null || !s.ingredients().containsKey(id)) {
                throw new IllegalArgumentException("Unknown ingredient: " + id);
            }
        }
        if (a.equals(b)) {
            throw new IllegalArgumentException("A rule needs two different ingredients");
        }
        if (result.equals(a) || result.equals(b)) {
            throw new IllegalArgumentException("The result must be a different ingredient from A and B");
        }
        String normalizedReason = reason == null ? "CONSOLIDATION" : reason.toUpperCase();
        if (!REASONS.contains(normalizedReason)) {
            throw new IllegalArgumentException("Reason must be one of " + REASONS);
        }
        if (s.hashMap().find(a, b, new LookupStats()).isPresent()) {
            throw new IllegalArgumentException("A rule for " + PairKey.of(a, b) + " already exists");
        }
        SubstitutionRule saved = ruleRepository.insert(a, b, result, normalizedReason, note);
        reload();
        return saved;
    }
}
