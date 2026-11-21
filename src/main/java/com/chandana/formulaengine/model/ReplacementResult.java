package com.chandana.formulaengine.model;

import java.util.List;

/**
 * Output of {@link com.chandana.formulaengine.engine.ReplacementEngine#replace}.
 *
 * @param lines        formula after replacement
 * @param replacements one entry per ingredient that was over its limit
 * @param unresolved   ingredients with no usable recipe (left in place, flagged for review)
 */
public record ReplacementResult(List<FormulaLine> lines,
                                List<AppliedReplacement> replacements,
                                List<String> unresolved) {
}
