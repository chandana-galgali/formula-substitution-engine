package com.chandana.formulaengine.model;

import java.util.List;

/**
 * One way to replace an unwanted ingredient: target -> part1 + part2 (+ part3 ...).
 *
 * Example: Lilial -> 60% Hedione + 40% Citronellol.
 * The fractions say how the removed amount is shared out and must add up to 1.
 *
 * @param id       database id
 * @param target   ingredient being replaced (the "C" in C -> A + B)
 * @param priority 1 = preferred option, 2 = fallback, ...
 * @param parts    the substitutes and their share of the removed amount
 * @param note     short explanation shown in the UI
 */
public record ReplacementRecipe(long id,
                                String target,
                                int priority,
                                List<Part> parts,
                                String note) {

    public record Part(String ingredientId, double fraction) {
    }
}
