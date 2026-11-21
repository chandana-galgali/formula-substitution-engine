package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.AppliedSubstitution;
import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.SubstitutionResult;
import com.chandana.formulaengine.model.SubstitutionRule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Applies A + B -> C rules to a formula until no rule matches any more.
 *
 * Algorithm
 * <ol>
 *   <li>Check every pair (i, j) of ingredients currently in the formula.</li>
 *   <li>On the first match, replace A and B with C. C gets A% + B%,
 *       so the formula still adds up to the same total.</li>
 *   <li>Start again from the top, because C may now pair with something else
 *       (chained rules, e.g. Lavender Accord + Coumarin -> Fougere Base).</li>
 * </ol>
 * Every substitution removes two lines and adds at most one, so the formula
 * shrinks each step. That guarantees the loop finishes (no infinite cycles).
 */
public final class SubstitutionEngine {

    private final RuleLookup lookup;

    public SubstitutionEngine(RuleLookup lookup) {
        this.lookup = lookup;
    }

    public SubstitutionResult optimize(List<FormulaLine> formula) {
        return optimize(formula, rule -> true);
    }

    /**
     * Same as {@link #optimize(List)}, but a rule only fires if {@code allowed} says yes.
     * Reformulation uses this so a banned or avoided ingredient is never folded into an accord.
     */
    public SubstitutionResult optimize(List<FormulaLine> formula, Predicate<SubstitutionRule> allowed) {
        LookupStats stats = new LookupStats();
        long start = System.nanoTime();

        List<FormulaLine> original = mergeDuplicates(formula);
        List<FormulaLine> current = new ArrayList<>(original);
        List<AppliedSubstitution> steps = new ArrayList<>();

        boolean changed = true;
        while (changed) {
            changed = false;
            search:
            for (int i = 0; i < current.size(); i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    FormulaLine a = current.get(i);
                    FormulaLine b = current.get(j);
                    Optional<SubstitutionRule> hit = lookup.find(a.ingredientId(), b.ingredientId(), stats);
                    if (hit.isPresent() && allowed.test(hit.get())) {
                        SubstitutionRule rule = hit.get();
                        double combined = round(a.percentage() + b.percentage());
                        current = replace(current, i, j, new FormulaLine(rule.result(), combined));
                        steps.add(new AppliedSubstitution(steps.size() + 1, a.ingredientId(), b.ingredientId(),
                                rule.result(), combined, rule.reason(), rule.note()));
                        changed = true;
                        break search;
                    }
                }
            }
        }

        long elapsed = System.nanoTime() - start;
        return new SubstitutionResult(original, List.copyOf(current), List.copyOf(steps), lookup.name(),
                stats.pairChecks(), stats.comparisons(), elapsed);
    }

    /** Puts {@code replacement} where line i was, drops line j, then merges if C was already present. */
    private static List<FormulaLine> replace(List<FormulaLine> lines, int i, int j, FormulaLine replacement) {
        List<FormulaLine> next = new ArrayList<>(lines.size() - 1);
        for (int k = 0; k < lines.size(); k++) {
            if (k == i) {
                next.add(replacement);
            } else if (k != j) {
                next.add(lines.get(k));
            }
        }
        return mergeDuplicates(next);
    }

    /** Same ingredient listed twice -> one line with the summed percentage (keeps first position). */
    public static List<FormulaLine> mergeDuplicates(List<FormulaLine> lines) {
        Map<String, Double> merged = new LinkedHashMap<>();
        for (FormulaLine line : lines) {
            merged.merge(line.ingredientId(), line.percentage(), Double::sum);
        }
        List<FormulaLine> out = new ArrayList<>(merged.size());
        merged.forEach((id, pct) -> out.add(new FormulaLine(id, round(pct))));
        return out;
    }

    /** Avoids floating point noise like 21.999999999 in the output. */
    public static double round(double value) {
        return Math.round(value * 10_000d) / 10_000d;
    }
}
