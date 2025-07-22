package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.service.MotelChainService;
import com.example.motels.model.MotelChain; // Ensure this path matches the actual location of the MotelChain class
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/motels/chains")
public class MotelChainController {

    @Autowired
    private MotelChainService motelChainService;

    @GetMapping
    public ResponseEntity<List<MotelChain>> getAllMotelChains() {
        List<MotelChain> motelChains = motelChainService.getAllMotelChains();
        return new ResponseEntity<>(motelChains, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotelChain> getMotelChainById(@PathVariable UUID id) {
        return motelChainService.getMotelChainById(id)
                .map(motelChain -> new ResponseEntity<>(motelChain, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<MotelChain> createMotelChain(@RequestBody MotelChain motelChain) {
        MotelChain createdMotelChain = motelChainService.createMotelChain(motelChain);
        return new ResponseEntity<>(createdMotelChain, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotelChain> updateMotelChain(@PathVariable UUID id, @RequestBody MotelChain motelChain) {
        MotelChain updatedMotelChain = motelChainService.updateMotelChain(id, motelChain);
        return updatedMotelChain != null ? new ResponseEntity<>(updatedMotelChain, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMotelChain(@PathVariable UUID id) {
        if (motelChainService.deleteMotelChain(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}