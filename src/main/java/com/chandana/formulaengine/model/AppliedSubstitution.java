package com.chandana.formulaengine.model;

/**
 * A record of one rule firing while a formula was optimized.
 */
public record AppliedSubstitution(int step,
                                  String ingredientA,
                                  String ingredientB,
                                  String result,
                                  double combinedPercentage,
                                  String reason,
                                  String note) {
}
