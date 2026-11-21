package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.SubstitutionRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * O(1) rule lookup.
 *
 * Every rule is indexed once by its {@link PairKey}. Checking a pair is then a
 * single hash + bucket read, no matter how many rules there are.
 */
public final class HashMapRuleLookup implements RuleLookup {

    private final Map<PairKey, SubstitutionRule> index;
    private final List<String> warnings = new ArrayList<>();

    public HashMapRuleLookup(Collection<SubstitutionRule> rules) {
        // Pre-size so the map never has to resize while loading (load factor 0.75).
        Map<PairKey, SubstitutionRule> map = new HashMap<>((int) (rules.size() / 0.75f) + 1);
        for (SubstitutionRule rule : rules) {
            PairKey key = PairKey.of(rule.ingredientA(), rule.ingredientB());
            // First rule wins, same as a top-to-bottom manual check would.
            SubstitutionRule existing = map.putIfAbsent(key, rule);
            if (existing != null) {
                warnings.add("Duplicate rule for " + key + ": rule #" + rule.id()
                        + " ignored, rule #" + existing.id() + " kept");
            }
        }
        this.index = Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<SubstitutionRule> find(String ingredientA, String ingredientB, LookupStats stats) {
        stats.recordPairCheck();
        stats.recordComparisons(1);
        return Optional.ofNullable(index.get(PairKey.of(ingredientA, ingredientB)));
    }

    @Override
    public String name() {
        return "HASHMAP";
    }

    @Override
    public int size() {
        return index.size();
    }

    /** Conflicts found while building the index (same pair defined twice). */
    public List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }
}
