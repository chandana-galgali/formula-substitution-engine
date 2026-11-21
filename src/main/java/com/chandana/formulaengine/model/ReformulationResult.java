package com.chandana.formulaengine.model;

import java.util.List;

/**
 * Full reformulation pipeline result:
 * step 1 replace unwanted ingredients (C -> A + B), then optionally
 * step 2 consolidate pairs (A + B -> C).
 *
 * @param original         input formula (duplicates merged)
 * @param afterReplacement formula after step 1
 * @param replacements     what step 1 did, per ingredient
 * @param unresolved       ingredients that still need a perfumer's review
 * @param consolidation    step 2 result, or null if step 2 was switched off
 * @param finalFormula     the formula to use
 */
public record ReformulationResult(List<FormulaLine> original,
                                  List<FormulaLine> afterReplacement,
                                  List<AppliedReplacement> replacements,
                                  List<String> unresolved,
                                  SubstitutionResult consolidation,
                                  List<FormulaLine> finalFormula) {
}
