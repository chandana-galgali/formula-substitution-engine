package com.chandana.formulaengine.model;

/**
 * One replacement rule: ingredientA + ingredientB -> result.
 * The order of A and B does not matter (see {@link com.chandana.formulaengine.engine.PairKey}).
 *
 * @param id          database id
 * @param ingredientA first ingredient code
 * @param ingredientB second ingredient code
 * @param result      ingredient code that replaces A and B
 * @param reason      why the rule exists: CONSOLIDATION, COST, SUPPLY ...
 * @param note        short explanation shown in the UI
 */
public record SubstitutionRule(long id,
                               String ingredientA,
                               String ingredientB,
                               String result,
                               String reason,
                               String note) {
}
