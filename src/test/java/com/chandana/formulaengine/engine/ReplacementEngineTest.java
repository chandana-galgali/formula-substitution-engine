package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.AppliedReplacement;
import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.ReplacementRecipe;
import com.chandana.formulaengine.model.ReplacementRecipe.Part;
import com.chandana.formulaengine.model.ReplacementResult;
import com.chandana.formulaengine.model.SubstitutionRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplacementEngineTest {

    // LIL and LYR banned (0%), OAK restricted to 0.1%
    private static final Map<String, Double> LIMITS = Map.of("LIL", 0.0, "LYR", 0.0, "OAK", 0.1);

    private static final List<ReplacementRecipe> RECIPES = List.of(
            new ReplacementRecipe(1, "LIL", 1, List.of(new Part("HED", 0.6), new Part("CTR", 0.4)), ""),
            new ReplacementRecipe(2, "LIL", 2, List.of(new Part("PEA", 0.5), new Part("LIN", 0.5)), ""),
            new ReplacementRecipe(3, "LYR", 1, List.of(new Part("LIL", 1.0)), ""),
            new ReplacementRecipe(4, "LYR", 2, List.of(new Part("HED", 0.5), new Part("PEA", 0.3), new Part("CTR", 0.2)), ""),
            new ReplacementRecipe(5, "OAK", 1, List.of(new Part("EVR", 0.6), new Part("CED", 0.4)), ""),
            new ReplacementRecipe(6, "GAL", 1, List.of(new Part("EBR", 1.0)), ""));

    private final ReplacementEngine engine = new ReplacementEngine(LIMITS, ReplacementEngine.index(RECIPES));

    private static double total(List<FormulaLine> lines) {
        return lines.stream().mapToDouble(FormulaLine::percentage).sum();
    }

    @Test
    void bannedIngredientIsFullyReplacedByItsPreferredRecipe() {
        ReplacementResult r = engine.replace(List.of(new FormulaLine("LIL", 5), new FormulaLine("DPG", 95)), Set.of());

        assertEquals(List.of(new FormulaLine("HED", 3), new FormulaLine("CTR", 2), new FormulaLine("DPG", 95)), r.lines());
        AppliedReplacement a = r.replacements().get(0);
        assertEquals("BANNED", a.trigger());
        assertEquals(Integer.valueOf(1), a.optionUsed());
        assertEquals(0.0, a.keptPercentage());
    }

    @Test
    void restrictedIngredientKeepsTheAllowedAmountAndReplacesOnlyTheExcess() {
        ReplacementResult r = engine.replace(List.of(new FormulaLine("OAK", 0.5), new FormulaLine("DPG", 99.5)), Set.of());

        assertEquals(List.of(new FormulaLine("OAK", 0.1), new FormulaLine("EVR", 0.24),
                new FormulaLine("CED", 0.16), new FormulaLine("DPG", 99.5)), r.lines());
        assertEquals("RESTRICTED", r.replacements().get(0).trigger());
        assertEquals(0.4, r.replacements().get(0).replacedPercentage(), 1e-9);
    }

    @Test
    void ingredientUnderItsLimitIsLeftAlone() {
        List<FormulaLine> f = List.of(new FormulaLine("OAK", 0.05), new FormulaLine("DPG", 99.95));
        ReplacementResult r = engine.replace(f, Set.of());
        assertEquals(f, r.lines());
        assertTrue(r.replacements().isEmpty());
    }

    @Test
    void recipeThatUsesAnotherBannedIngredientIsSkipped() {
        // Option 1 for Lyral is "use Lilial", but Lilial is banned too, so option 2 must be used.
        ReplacementResult r = engine.replace(List.of(new FormulaLine("LYR", 10), new FormulaLine("DPG", 90)), Set.of());

        AppliedReplacement a = r.replacements().get(0);
        assertEquals(Integer.valueOf(2), a.optionUsed());
        assertEquals(1, a.skipped().size());
        assertEquals("LIL", a.skipped().get(0).blockedIngredient());
        assertEquals("BANNED", a.skipped().get(0).reason());
        assertEquals(List.of(new FormulaLine("HED", 5), new FormulaLine("PEA", 3),
                new FormulaLine("CTR", 2), new FormulaLine("DPG", 90)), r.lines());
    }

    @Test
    void avoidedSubstituteForcesTheFallbackRecipe() {
        // The company also stops using Hedione -> Lilial must use option 2 (PEA + LIN).
        ReplacementResult r = engine.replace(List.of(new FormulaLine("LIL", 4), new FormulaLine("DPG", 96)), Set.of("HED"));
        assertEquals(Integer.valueOf(2), r.replacements().get(0).optionUsed());
        assertEquals("AVOIDED", r.replacements().get(0).skipped().get(0).reason());
        assertEquals(List.of(new FormulaLine("PEA", 2), new FormulaLine("LIN", 2), new FormulaLine("DPG", 96)), r.lines());
    }

    @Test
    void userCanAvoidAnyIngredient() {
        ReplacementResult r = engine.replace(List.of(new FormulaLine("GAL", 15), new FormulaLine("DPG", 85)), Set.of("GAL"));
        assertEquals("AVOIDED", r.replacements().get(0).trigger());
        assertEquals(List.of(new FormulaLine("EBR", 15), new FormulaLine("DPG", 85)), r.lines());
    }

    @Test
    void noUsableRecipeIsFlaggedForReviewAndNothingIsLost() {
        ReplacementResult r = engine.replace(List.of(new FormulaLine("COU", 6), new FormulaLine("DPG", 94)), Set.of("COU"));
        assertEquals(List.of("COU"), r.unresolved());
        assertNull(r.replacements().get(0).optionUsed());
        assertEquals(100.0, total(r.lines()), 1e-9);
    }

    @Test
    void substitutesMergeWithIngredientsAlreadyInTheFormula() {
        ReplacementResult r = engine.replace(List.of(new FormulaLine("HED", 10), new FormulaLine("LIL", 5),
                new FormulaLine("DPG", 85)), Set.of());
        assertEquals(List.of(new FormulaLine("HED", 13), new FormulaLine("CTR", 2), new FormulaLine("DPG", 85)), r.lines());
    }

    @Test
    void totalIsPreservedEvenWithAwkwardFractions() {
        ReplacementEngine e = new ReplacementEngine(Map.of("X", 0.0), ReplacementEngine.index(List.of(
                new ReplacementRecipe(1, "X", 1, List.of(new Part("A", 0.3333), new Part("B", 0.3333), new Part("C", 0.3334)), ""))));
        ReplacementResult r = e.replace(List.of(new FormulaLine("X", 7.77), new FormulaLine("DPG", 92.23)), Set.of());
        assertEquals(100.0, total(r.lines()), 1e-9);
    }

    @Test
    void badRecipesAreRejectedWhenTheIndexIsBuilt() {
        assertThrows(IllegalArgumentException.class, () -> ReplacementEngine.index(List.of(
                new ReplacementRecipe(1, "X", 1, List.of(new Part("A", 0.5)), ""))));          // adds up to 0.5
        assertThrows(IllegalArgumentException.class, () -> ReplacementEngine.index(List.of(
                new ReplacementRecipe(2, "X", 1, List.of(new Part("X", 1.0)), ""))));          // replaces itself
    }

    @Test
    void consolidationNeverFoldsAnAvoidedIngredientIntoAnAccord() {
        // COU is avoided but has no recipe, so it stays. LAV + COU -> FOU must NOT fire.
        Set<String> avoid = Set.of("COU");
        List<FormulaLine> afterStep1 = engine.replace(List.of(new FormulaLine("ACC-LAV", 20),
                new FormulaLine("COU", 6), new FormulaLine("DPG", 74)), avoid).lines();

        var rules = List.of(new SubstitutionRule(1, "ACC-LAV", "COU", "ACC-FOU", "CONSOLIDATION", ""));
        var result = new SubstitutionEngine(new HashMapRuleLookup(rules)).optimize(afterStep1, rule ->
                !engine.isLimited(rule.ingredientA(), avoid) && !engine.isLimited(rule.ingredientB(), avoid)
                        && !engine.isLimited(rule.result(), avoid));
        assertTrue(result.steps().isEmpty());
        assertEquals(afterStep1, result.optimized());
    }
}
