package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.SubstitutionRule;

import java.util.Optional;

/**
 * "Is there a rule for A + B?"
 *
 * Two implementations exist so the speed-up can be measured:
 * <ul>
 *   <li>{@link HashMapRuleLookup} - O(1) average per check</li>
 *   <li>{@link LinearScanRuleLookup} - O(R) per check, the "manual" way</li>
 * </ul>
 * The engine depends only on this interface (dependency inversion), so
 * strategies can be swapped without touching engine code.
 */
public interface RuleLookup {

    Optional<SubstitutionRule> find(String ingredientA, String ingredientB, LookupStats stats);

    /** Strategy name shown in results, e.g. "HASHMAP". */
    String name();

    /** Number of rules this lookup knows about. */
    int size();
}
