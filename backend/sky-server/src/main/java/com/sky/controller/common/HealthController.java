package com.sky.controller.common;

import com.sky.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({"/api/health", "/admin/health", "/health"})
    public Result<String> health() {
        return Result.success("ok");
    }
}
