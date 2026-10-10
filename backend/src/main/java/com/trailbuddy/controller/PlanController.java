package com.trailbuddy.controller;

import com.trailbuddy.model.PlanRequest;
import com.trailbuddy.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return planService.health();
    }

    @PostMapping("/plan")
    public ResponseEntity<Map<String, Object>> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }
}
