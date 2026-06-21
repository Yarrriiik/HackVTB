package com.orchestra.api.controller.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Orchestra API");
        response.put("stage", "Hackathon prototype");
        response.put("focus", "Transform BPMN, sequence diagrams, and OpenAPI specs into normalized testing metadata");
        response.put("capabilities", List.of(
                "BPMN upload and parsing",
                "Sequence diagram upload and parsing",
                "OpenAPI upload and normalization",
                "PostgreSQL persistence",
                "Basic authentication endpoints",
                "Swagger UI and local demo page"
        ));
        response.put("sampleFiles", List.of(
                "data_for_tests/openapi.json",
                "data_for_tests/openapi.yml",
                "data_for_tests/01_bonus_payment.puml"
        ));
        response.put("limitations", List.of(
                "No dedicated frontend application",
                "Scenario execution pipeline is not implemented in the public version",
                "The repository presents a working backend slice rather than a full product"
        ));
        return response;
    }
}
