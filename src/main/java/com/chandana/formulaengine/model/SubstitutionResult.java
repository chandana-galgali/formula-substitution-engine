package com.chandana.formulaengine.model;

import java.util.List;

/**
 * Output of {@link com.chandana.formulaengine.engine.SubstitutionEngine#optimize(List)}.
 *
 * @param original     formula as it came in (duplicates merged)
 * @param optimized    formula after every applicable rule has fired
 * @param steps        each substitution, in the order it happened
 * @param strategy     which lookup was used (HASHMAP or LINEAR_SCAN)
 * @param pairChecks   how many ingredient pairs were checked against the rule book
 * @param comparisons  how many rule comparisons those checks cost in total
 * @param elapsedNanos wall-clock time spent inside the engine
 */
public record SubstitutionResult(List<FormulaLine> original,
                                 List<FormulaLine> optimized,
                                 List<AppliedSubstitution> steps,
                                 String strategy,
                                 long pairChecks,
                                 long comparisons,
                                 long elapsedNanos) {
}
