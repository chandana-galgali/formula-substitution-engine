package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.SubstitutionResult;
import com.chandana.formulaengine.model.SubstitutionRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubstitutionEngineTest {

    private static final List<SubstitutionRule> RULES = List.of(
            new SubstitutionRule(1, "LIN", "LNA", "ACC-LAV", "CONSOLIDATION", ""),
            new SubstitutionRule(2, "ISO", "AMB", "ACC-WOD", "COST", ""),
            new SubstitutionRule(3, "ACC-LAV", "COU", "ACC-FOU", "CONSOLIDATION", ""),
            new SubstitutionRule(4, "PEA", "GER", "ACC-ROS", "CONSOLIDATION", ""));

    private static final List<FormulaLine> LAVENDER_FOUGERE = List.of(
            new FormulaLine("LIN", 12), new FormulaLine("LNA", 10), new FormulaLine("COU", 6),
            new FormulaLine("BGM", 15), new FormulaLine("ISO", 20), new FormulaLine("AMB", 2),
            new FormulaLine("DPG", 35));

    @Test
    void pairKeyIgnoresOrder() {
        assertEquals(PairKey.of("A", "B"), PairKey.of("B", "A"));
        assertEquals(PairKey.of("A", "B").hashCode(), PairKey.of("B", "A").hashCode());
    }

    @Test
    void appliesChainedRules() {
        SubstitutionResult r = new SubstitutionEngine(new HashMapRuleLookup(RULES)).optimize(LAVENDER_FOUGERE);

        assertEquals(List.of(
                new FormulaLine("ACC-FOU", 28), new FormulaLine("BGM", 15),
                new FormulaLine("ACC-WOD", 22), new FormulaLine("DPG", 35)), r.optimized());
        assertEquals(3, r.steps().size());
        assertEquals("ACC-LAV", r.steps().get(0).result());
        assertEquals("ACC-FOU", r.steps().get(1).result()); // chained: uses the accord made in step 1
    }

    @Test
    void totalPercentageIsPreserved() {
        SubstitutionResult r = new SubstitutionEngine(new HashMapRuleLookup(RULES)).optimize(LAVENDER_FOUGERE);
        double total = r.optimized().stream().mapToDouble(FormulaLine::percentage).sum();
        assertEquals(100.0, total, 1e-9);
    }

    @Test
    void hashMapAndLinearScanGiveTheSameAnswer() {
        SubstitutionResult fast = new SubstitutionEngine(new HashMapRuleLookup(RULES)).optimize(LAVENDER_FOUGERE);
        SubstitutionResult slow = new SubstitutionEngine(new LinearScanRuleLookup(RULES)).optimize(LAVENDER_FOUGERE);

        assertEquals(fast.optimized(), slow.optimized());
        assertEquals(fast.steps(), slow.steps());
        assertEquals(fast.pairChecks(), slow.pairChecks());
        // HashMap: exactly one comparison per pair check. Linear: usually many more.
        assertEquals(fast.pairChecks(), fast.comparisons());
        assertTrue(slow.comparisons() > fast.comparisons());
    }

    @Test
    void formulaWithoutMatchesIsUnchanged() {
        List<FormulaLine> f = List.of(new FormulaLine("BGM", 40), new FormulaLine("DPG", 60));
        SubstitutionResult r = new SubstitutionEngine(new HashMapRuleLookup(RULES)).optimize(f);
        assertEquals(f, r.optimized());
        assertTrue(r.steps().isEmpty());
    }

    @Test
    void resultMergesWithExistingLineOfSameIngredient() {
        List<FormulaLine> f = List.of(new FormulaLine("ACC-ROS", 5), new FormulaLine("PEA", 10),
                new FormulaLine("GER", 5), new FormulaLine("DPG", 80));
        SubstitutionResult r = new SubstitutionEngine(new HashMapRuleLookup(RULES)).optimize(f);
        assertEquals(List.of(new FormulaLine("ACC-ROS", 20), new FormulaLine("DPG", 80)), r.optimized());
    }

    @Test
    void duplicateRuleKeepsFirstAndWarns() {
        HashMapRuleLookup lookup = new HashMapRuleLookup(List.of(
                new SubstitutionRule(1, "A", "B", "C", "COST", ""),
                new SubstitutionRule(2, "B", "A", "D", "COST", "")));
        assertEquals(1, lookup.size());
        assertEquals(1, lookup.warnings().size());
        assertEquals("C", lookup.find("A", "B", new LookupStats()).orElseThrow().result());
    }

    @Test
    void benchmarkCountsMatchComplexity() {
        Benchmark.Result r = Benchmark.run(1_000, 200, 7L);
        assertEquals(200, r.hashMapComparisons());          // O(1): one per lookup
        assertTrue(r.linearScanComparisons() > 200 * 100);   // O(R): hundreds per lookup
    }
}
