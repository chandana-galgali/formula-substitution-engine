package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.AppliedReplacement;
import com.chandana.formulaengine.model.AppliedReplacement.SkippedOption;
import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.ReplacementRecipe;
import com.chandana.formulaengine.model.ReplacementResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The reverse of {@link SubstitutionEngine}: C -> A + B (+ ...).
 *
 * When an ingredient is banned, over its restriction limit, or simply not
 * wanted any more (cost, supply, customer request), the engine swaps the
 * unwanted amount for a blend of alternatives that gives a similar effect.
 *
 * Both look-ups are O(1) HashMap reads:
 * <ul>
 *   <li>{@code limits}:  ingredient -> max % allowed</li>
 *   <li>{@code recipes}: ingredient -> its replacement options, best first</li>
 * </ul>
 * A recipe is only used if none of its own parts are banned, restricted or
 * avoided. Otherwise the next option is tried, so the engine never swaps one
 * problem for another.
 */
public final class ReplacementEngine {

    public static final String BANNED = "BANNED";
    public static final String RESTRICTED = "RESTRICTED";
    public static final String AVOIDED = "AVOIDED";

    private final Map<String, Double> limits;
    private final Map<String, List<ReplacementRecipe>> recipesByTarget;

    public ReplacementEngine(Map<String, Double> limits, Map<String, List<ReplacementRecipe>> recipesByTarget) {
        this.limits = Map.copyOf(limits);
        this.recipesByTarget = recipesByTarget;
    }

    /** Builds the target -> options HashMap, sorted so option 1 is tried first. */
    public static Map<String, List<ReplacementRecipe>> index(Collection<ReplacementRecipe> recipes) {
        Map<String, List<ReplacementRecipe>> map = new HashMap<>();
        for (ReplacementRecipe recipe : recipes) {
            validate(recipe);
            map.computeIfAbsent(recipe.target(), k -> new ArrayList<>()).add(recipe);
        }
        map.replaceAll((target, list) -> {
            list.sort(Comparator.comparingInt(ReplacementRecipe::priority).thenComparingLong(ReplacementRecipe::id));
            return List.copyOf(list);
        });
        return Collections.unmodifiableMap(map);
    }

    private static void validate(ReplacementRecipe r) {
        if (r.parts().isEmpty()) {
            throw new IllegalArgumentException("Recipe #" + r.id() + " has no parts");
        }
        double sum = r.parts().stream().mapToDouble(ReplacementRecipe.Part::fraction).sum();
        if (Math.abs(sum - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Recipe #" + r.id() + " fractions add up to " + sum + ", expected 1");
        }
        if (r.parts().stream().anyMatch(p -> p.ingredientId().equals(r.target()))) {
            throw new IllegalArgumentException("Recipe #" + r.id() + " uses its own target");
        }
    }

    /** Highest % allowed for this ingredient. 0 = not allowed, infinity = no limit. */
    public double limitFor(String ingredientId, Set<String> avoid) {
        if (avoid.contains(ingredientId)) {
            return 0;
        }
        Double limit = limits.get(ingredientId);
        return limit == null ? Double.POSITIVE_INFINITY : limit;
    }

    /** True if the ingredient has any limit at all (banned, restricted or avoided). */
    public boolean isLimited(String ingredientId, Set<String> avoid) {
        return limitFor(ingredientId, avoid) != Double.POSITIVE_INFINITY;
    }

    public ReplacementResult replace(List<FormulaLine> formula, Set<String> avoid) {
        List<FormulaLine> out = new ArrayList<>();
        List<AppliedReplacement> applied = new ArrayList<>();
        List<String> unresolved = new ArrayList<>();

        for (FormulaLine line : SubstitutionEngine.mergeDuplicates(formula)) {
            String id = line.ingredientId();
            double limit = limitFor(id, avoid);                       // O(1)
            if (line.percentage() <= limit) {
                out.add(line);                                        // within limits: keep as is
                continue;
            }

            String trigger = avoid.contains(id) ? AVOIDED : (limit == 0 ? BANNED : RESTRICTED);
            double kept = limit;                                      // 0, or the allowed maximum
            double excess = SubstitutionEngine.round(line.percentage() - kept);

            List<SkippedOption> skipped = new ArrayList<>();
            ReplacementRecipe chosen = null;
            for (ReplacementRecipe recipe : recipesByTarget.getOrDefault(id, List.of())) {   // O(1)
                SkippedOption problem = firstProblem(recipe, avoid);
                if (problem == null) {
                    chosen = recipe;
                    break;
                }
                skipped.add(problem);
            }

            if (chosen == null) {
                out.add(line);                                        // leave it, but flag it
                unresolved.add(id);
                applied.add(new AppliedReplacement(id, trigger, line.percentage(), line.percentage(), 0,
                        null, List.of(), List.copyOf(skipped), "No usable replacement: needs a perfumer's review"));
                continue;
            }

            if (kept > 0) {
                out.add(new FormulaLine(id, kept));
            }
            List<FormulaLine> added = split(excess, chosen);
            out.addAll(added);
            applied.add(new AppliedReplacement(id, trigger, line.percentage(), kept, excess,
                    chosen.priority(), added, List.copyOf(skipped), chosen.note()));
        }

        return new ReplacementResult(SubstitutionEngine.mergeDuplicates(out), List.copyOf(applied), List.copyOf(unresolved));
    }

    /** Returns why a recipe can't be used, or null if every part is fine. */
    private SkippedOption firstProblem(ReplacementRecipe recipe, Set<String> avoid) {
        for (ReplacementRecipe.Part part : recipe.parts()) {
            String pid = part.ingredientId();
            if (avoid.contains(pid)) {
                return new SkippedOption(recipe.priority(), pid, AVOIDED);
            }
            Double limit = limits.get(pid);
            if (limit != null) {
                return new SkippedOption(recipe.priority(), pid, limit == 0 ? BANNED : RESTRICTED);
            }
        }
        return null;
    }

    /** Shares {@code amount} between the parts. The last part takes the rounding remainder so nothing is lost. */
    private static List<FormulaLine> split(double amount, ReplacementRecipe recipe) {
        List<FormulaLine> lines = new ArrayList<>();
        double used = 0;
        List<ReplacementRecipe.Part> parts = recipe.parts();
        for (int k = 0; k < parts.size(); k++) {
            double share = (k == parts.size() - 1)
                    ? SubstitutionEngine.round(amount - used)
                    : SubstitutionEngine.round(amount * parts.get(k).fraction());
            used += share;
            lines.add(new FormulaLine(parts.get(k).ingredientId(), share));
        }
        return lines;
    }
}
