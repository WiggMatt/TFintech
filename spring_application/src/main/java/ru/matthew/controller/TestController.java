package ru.matthew.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final MeterRegistry meterRegistry;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(@RequestHeader("User-ID") String userId) {
        MDC.put("userId", userId);
        logger.info("Received a test request");
        MDC.clear();
        return ResponseEntity.ok("Log created");
    }

    @GetMapping("/custom")
    public String incrementCustomMetric() {
        meterRegistry.counter("custom_requests_total", "endpoint", "/custom").increment();
        return "Custom metric incremented!";
    }

    @GetMapping("/recursive")
    public void recursiveCall() {
        recursiveCall();
    }

    @GetMapping("/oom")
    public void outOfMemory() {
        List<String> list = new ArrayList<>();
        while (true) {
            list.add("OutOfMemoryError");
        }
    }
}
