package com.example.motels.controller;

import com.example.motels.model.Motel;
import com.example.motels.service.MotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motels/chain/{motelChainId}/motels")
public class MotelController {

    @Autowired
    private MotelService motelService;

    @GetMapping
    public ResponseEntity<List<Motel>> getAllMotelsByChainId(@PathVariable String motelChainId) {
        List<Motel> motels = motelService.getAllMotelsByChainId(motelChainId);
        return ResponseEntity.ok(motels);
    }

    @GetMapping("/{motelId}")
    public ResponseEntity<Motel> getMotelById(@PathVariable UUID motelId) {
        Optional<Motel> motel = motelService.getMotelById(motelId);
        return motel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Motel> createMotel(@PathVariable String motelChainId, @RequestBody Motel motel) {
        motel.setMotelChainId(motelChainId);
        Motel createdMotel = motelService.createMotel(motel);
        return new ResponseEntity<>(createdMotel, HttpStatus.CREATED);
    }

    @PutMapping("/{motelId}")
    public ResponseEntity<Motel> updateMotel(@PathVariable UUID motelId, @RequestBody Motel motel) {
        motel.setMotelId(motelId);
        Motel updatedMotel = motelService.updateMotel(motel);
        return ResponseEntity.ok(updatedMotel);
    }

    @DeleteMapping("/{motelId}")
    public ResponseEntity<Void> deleteMotel(@PathVariable UUID motelId) {
        motelService.deleteMotel(motelId);
        return ResponseEntity.noContent().build();
    }
}
