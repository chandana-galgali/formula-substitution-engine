package com.chandana.formulaengine.engine;

import com.chandana.formulaengine.model.SubstitutionRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Small, honest micro-benchmark: HashMap lookup vs linear scan over the same
 * synthetic rule book. Not a JMH-grade benchmark, but it runs a warm-up pass
 * first and uses the same random pairs for both strategies.
 */
public final class Benchmark {

    public record Result(int rules,
                         int lookups,
                         long hashMapNanos,
                         long linearScanNanos,
                         long hashMapComparisons,
                         long linearScanComparisons,
                         double speedup) {
    }

    private Benchmark() {
    }

    public static Result run(int ruleCount, int lookupCount, long seed) {
        List<SubstitutionRule> rules = syntheticRules(ruleCount);
        HashMapRuleLookup hash = new HashMapRuleLookup(rules);
        LinearScanRuleLookup linear = new LinearScanRuleLookup(rules);

        // Half the queries hit a real rule, half are misses (the worst case for a scan).
        Random random = new Random(seed);
        String[][] queries = new String[lookupCount][];
        for (int q = 0; q < lookupCount; q++) {
            int i = random.nextInt(ruleCount);
            queries[q] = (q % 2 == 0)
                    ? new String[]{code(i + 1), code(i)}          // hit, given in reverse order
                    : new String[]{code(i), "MISS-" + i};          // miss
        }

        // Warm-up so the JIT compiles both paths before timing.
        time(hash, queries, Math.min(lookupCount, 1_000));
        time(linear, queries, Math.min(lookupCount, 50));

        LookupStats hashStats = new LookupStats();
        LookupStats linearStats = new LookupStats();
        long hashNanos = time(hash, queries, lookupCount, hashStats);
        long linearNanos = time(linear, queries, lookupCount, linearStats);

        double speedup = hashNanos == 0 ? 0 : (double) linearNanos / hashNanos;
        return new Result(ruleCount, lookupCount, hashNanos, linearNanos,
                hashStats.comparisons(), linearStats.comparisons(), Math.round(speedup * 10) / 10.0);
    }

    static List<SubstitutionRule> syntheticRules(int count) {
        List<SubstitutionRule> rules = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rules.add(new SubstitutionRule(i, code(i), code(i + 1), "ACC-" + i, "SYNTHETIC", ""));
        }
        return rules;
    }

    private static String code(int i) {
        return String.format("SYN-%06d", i);
    }

    private static long time(RuleLookup lookup, String[][] queries, int n) {
        return time(lookup, queries, n, new LookupStats());
    }

    private static long time(RuleLookup lookup, String[][] queries, int n, LookupStats stats) {
        long found = 0;
        long start = System.nanoTime();
        for (int q = 0; q < n; q++) {
            if (lookup.find(queries[q][0], queries[q][1], stats).isPresent()) {
                found++;
            }
        }
        long elapsed = System.nanoTime() - start;
        if (found < 0) {
            throw new AssertionError(); // keeps the JIT from removing the loop
        }
        return elapsed;
    }
}
