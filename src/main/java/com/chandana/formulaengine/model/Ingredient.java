package com.chandana.formulaengine.model;

/**
 * A raw material or a pre-blended accord that can appear in a formula.
 *
 * @param id       short code used everywhere as the key, e.g. "LIN"
 * @param name     human-readable name, e.g. "Linalool"
 * @param category olfactive family, e.g. "Floral"
 * @param accord   true if this is a pre-blended base (the "C" in A + B -> C)
 */
public record Ingredient(String id, String name, String category, boolean accord) {
}
