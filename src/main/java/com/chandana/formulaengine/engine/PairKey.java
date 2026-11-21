package com.chandana.formulaengine.engine;

import java.util.Objects;

/**
 * HashMap key for an unordered pair of ingredients.
 *
 * "A + B" and "B + A" must find the same rule, so the two codes are stored in
 * sorted order. Because this is a record, equals() and hashCode() are generated
 * from both fields, which is exactly what a HashMap needs.
 */
public record PairKey(String first, String second) {

    public PairKey {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.compareTo(second) > 0) {
            throw new IllegalArgumentException("Use PairKey.of(a, b) so the pair is normalized");
        }
    }

    /** Builds an order-independent key: of("B", "A").equals(of("A", "B")) is true. */
    public static PairKey of(String a, String b) {
        return a.compareTo(b) <= 0 ? new PairKey(a, b) : new PairKey(b, a);
    }

    @Override
    public String toString() {
        return first + " + " + second;
    }
}
