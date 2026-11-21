package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.SubstitutionRule;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Baseline: the "manual check". Walk the whole rule list for every pair.
 *
 * Kept on purpose so the HashMap version can be compared against it.
 * Cost per check is O(R) where R is the number of rules.
 */
public final class LinearScanRuleLookup implements RuleLookup {

    private final List<SubstitutionRule> rules;

    public LinearScanRuleLookup(Collection<SubstitutionRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public Optional<SubstitutionRule> find(String ingredientA, String ingredientB, LookupStats stats) {
        stats.recordPairCheck();
        long compared = 0;
        try {
            for (SubstitutionRule rule : rules) {
                compared++;
                boolean sameOrder = rule.ingredientA().equals(ingredientA) && rule.ingredientB().equals(ingredientB);
                boolean swapped = rule.ingredientA().equals(ingredientB) && rule.ingredientB().equals(ingredientA);
                if (sameOrder || swapped) {
                    return Optional.of(rule);
                }
            }
            return Optional.empty();
        } finally {
            stats.recordComparisons(compared);
        }
    }

    @Override
    public String name() {
        return "LINEAR_SCAN";
    }

    @Override
    public int size() {
        return rules.size();
    }
}
