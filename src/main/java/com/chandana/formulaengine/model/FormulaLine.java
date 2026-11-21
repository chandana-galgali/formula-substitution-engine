package com.chandana.formulaengine.model;

/**
 * One line of a formula: an ingredient and how much of it (percent by weight).
 */
public record FormulaLine(String ingredientId, double percentage) {
}
