package com.example.motels.controller;

import com.example.motels.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/motelApi/v1/ping")
public class PingController {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> ping() {
        ApiResponse<String> response = new ApiResponse<>("200", "pong");
        return ResponseEntity.ok(response);
    }
}
