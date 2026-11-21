package com.chandana.formulaengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Starts the whole app (Spring + H2 + SQL scripts) and calls the REST API. */
@SpringBootTest
@AutoConfigureMockMvc
class FormulaApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void rulesAreLoadedFromTheDatabase() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rules", greaterThan(0)));
    }

    @Test
    void optimizeEndpointAppliesRules() throws Exception {
        String body = """
                {"strategy":"HASHMAP","lines":[
                  {"ingredientId":"LIN","percentage":12},
                  {"ingredientId":"LNA","percentage":10},
                  {"ingredientId":"DPG","percentage":78}]}
                """;
        mvc.perform(post("/api/formulas/optimize").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optimized[0].ingredientId").value("ACC-LAV"))
                .andExpect(jsonPath("$.optimized[0].percentage").value(22.0))
                .andExpect(jsonPath("$.steps.length()").value(1));
    }

    @Test
    void reformulateReplacesBannedIngredientsThenConsolidates() throws Exception {
        // Lilial + Lyral are banned. Lyral's option 1 uses Lilial, so option 2 is used.
        String body = """
                {"consolidate":true,"lines":[
                  {"ingredientId":"LIL","percentage":4},
                  {"ingredientId":"LYR","percentage":6},
                  {"ingredientId":"HED","percentage":10},
                  {"ingredientId":"GER","percentage":5},
                  {"ingredientId":"DPG","percentage":75}]}
                """;
        mvc.perform(post("/api/formulas/reformulate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replacements[0].trigger").value("BANNED"))
                .andExpect(jsonPath("$.replacements[1].optionUsed").value(2))
                .andExpect(jsonPath("$.replacements[1].skipped[0].blockedIngredient").value("LIL"))
                .andExpect(jsonPath("$.unresolved.length()").value(0))
                .andExpect(jsonPath("$.finalFormula[0].ingredientId").value("ACC-FRH"))
                .andExpect(jsonPath("$.finalFormula[0].percentage").value(23.2));
    }

    @Test
    void recipesAreLoadedFromTheDatabase() throws Exception {
        mvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(5)));
    }

    @Test
    void unknownIngredientIsRejected() throws Exception {
        String body = """
                {"lines":[{"ingredientId":"NOPE","percentage":10}]}
                """;
        mvc.perform(post("/api/formulas/optimize").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown ingredient: NOPE"));
    }
}
