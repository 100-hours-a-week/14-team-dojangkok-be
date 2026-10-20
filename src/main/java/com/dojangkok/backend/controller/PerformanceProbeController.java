package com.dojangkok.backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A profile-restricted, read-only endpoint used to verify the local performance-test workflow.
 */
@Profile({"local", "dev"})
@RestController
@RequestMapping("/api/v1/test/performance")
public class PerformanceProbeController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
                "status", "ok",
                "purpose", "performance-draft-validation"
        );
    }
}
