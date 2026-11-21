package com.chandana.formulaengine.web;

import com.chandana.formulaengine.engine.Benchmark;
import com.chandana.formulaengine.model.FormulaLine;
import com.chandana.formulaengine.model.Ingredient;
import com.chandana.formulaengine.model.SubstitutionResult;
import com.chandana.formulaengine.model.SubstitutionRule;
import com.chandana.formulaengine.service.FormulaService;
import com.chandana.formulaengine.service.FormulaService.Strategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FormulaController {

    public record OptimizeRequest(List<FormulaLine> lines, String strategy) {
    }

    public record NewRuleRequest(String ingredientA, String ingredientB, String result, String reason, String note) {
    }

    private final FormulaService service;

    public FormulaController(FormulaService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "mode", "spring-boot", "rules", service.ruleCount());
    }

    @GetMapping("/ingredients")
    public List<Ingredient> ingredients() {
        return service.ingredients();
    }

    @GetMapping("/rules")
    public List<SubstitutionRule> rules() {
        return service.rules();
    }

    @PostMapping("/rules")
    public ResponseEntity<SubstitutionRule> addRule(@RequestBody NewRuleRequest r) {
        SubstitutionRule saved = service.addRule(r.ingredientA(), r.ingredientB(), r.result(), r.reason(), r.note());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/formulas/optimize")
    public SubstitutionResult optimize(@RequestBody OptimizeRequest request) {
        Strategy strategy = "LINEAR_SCAN".equalsIgnoreCase(request.strategy()) ? Strategy.LINEAR_SCAN : Strategy.HASHMAP;
        return service.optimize(request.lines(), strategy);
    }

    /** Compare both lookups on a synthetic rule book. Limits keep the slow path under ~1 second. */
    @GetMapping("/benchmark")
    public Benchmark.Result benchmark(@RequestParam(defaultValue = "10000") int rules,
                                      @RequestParam(defaultValue = "2000") int lookups) {
        int r = Math.max(100, Math.min(rules, 50_000));
        int l = Math.max(100, Math.min(lookups, 5_000));
        return Benchmark.run(r, l, 42L);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
