package com.example.motels.controller;

import com.example.motels.model.ApiResponse;
import com.example.motels.model.Motel;
import com.example.motels.service.MotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/motelApi/v1/allMotels")
public class AllMotelsController {

    @Autowired
    private MotelService motelService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Motel>>> getAllMotels() {
        List<Motel> motels = motelService.getAllMotels();
        ApiResponse<List<Motel>> response = new ApiResponse<>("200", motels);
        return ResponseEntity.ok(response);
    }
}
