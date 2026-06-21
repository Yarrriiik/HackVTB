package com.orchestra.api.controller.system;

import com.orchestra.api.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("Service is running");
    }
}
