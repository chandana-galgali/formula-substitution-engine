package com.chandana.formulaengine.engine;

/**
 * Counts work done during one optimization so the two strategies can be compared.
 * A new instance is created per request, so no synchronization is needed.
 */
public final class LookupStats {

    private long pairChecks;
    private long comparisons;

    void recordPairCheck() {
        pairChecks++;
    }

    void recordComparisons(long n) {
        comparisons += n;
    }

    public long pairChecks() {
        return pairChecks;
    }

    public long comparisons() {
        return comparisons;
    }
}
