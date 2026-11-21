package com.chandana.formulaengine.model;

import java.util.List;

/**
 * What happened to one unwanted ingredient during reformulation.
 *
 * @param ingredientId       the ingredient that was over its limit
 * @param trigger            BANNED, RESTRICTED or AVOIDED (the user's own choice)
 * @param originalPercentage how much was in the formula
 * @param keptPercentage     how much stayed (the allowed maximum for RESTRICTED, else 0)
 * @param replacedPercentage how much was handed to the substitutes
 * @param optionUsed         priority number of the recipe used; null if nothing usable was found
 * @param added              lines added by the recipe
 * @param skipped            recipes that were checked but could not be used, and why
 * @param note               the recipe note, or why manual review is needed
 */
public record AppliedReplacement(String ingredientId,
                                 String trigger,
                                 double originalPercentage,
                                 double keptPercentage,
                                 double replacedPercentage,
                                 Integer optionUsed,
                                 List<FormulaLine> added,
                                 List<SkippedOption> skipped,
                                 String note) {

    /** A recipe that was not usable because one of its parts is itself banned, restricted or avoided. */
    public record SkippedOption(int option, String blockedIngredient, String reason) {
    }
}
