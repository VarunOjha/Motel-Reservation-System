package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.service.RoomCategoryService;
import com.example.motels.model.RoomCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/motels/chains/{motelChainId}/motels/{motelId}/rooms/categories")
public class RoomCategoryController {

    @Autowired
    private RoomCategoryService roomCategoryService;

    @GetMapping
    public ResponseEntity<List<RoomCategory>> getAllRoomCategories(@PathVariable UUID motelChainId, @PathVariable UUID motelId) {
        List<RoomCategory> roomCategories = roomCategoryService.getAllRoomCategories(motelChainId, motelId);
        return new ResponseEntity<>(roomCategories, HttpStatus.OK);
    }

    @GetMapping("/{roomCategoryId}")
    public ResponseEntity<RoomCategory> getRoomCategoryById(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomCategoryId) {
        return roomCategoryService.getRoomCategoryById(motelChainId, motelId, roomCategoryId)
                .map(roomCategory -> new ResponseEntity<>(roomCategory, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<RoomCategory> createRoomCategory(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @RequestBody RoomCategory roomCategory) {
        RoomCategory createdRoomCategory = roomCategoryService.createRoomCategory(motelChainId, motelId, roomCategory);
        return new ResponseEntity<>(createdRoomCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{roomCategoryId}")
    public ResponseEntity<RoomCategory> updateRoomCategory(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomCategoryId, @RequestBody RoomCategory roomCategory) {
        RoomCategory updatedRoomCategory = roomCategoryService.updateRoomCategory(motelChainId, motelId, roomCategoryId, roomCategory);
        return updatedRoomCategory != null ? new ResponseEntity<>(updatedRoomCategory, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{roomCategoryId}")
    public ResponseEntity<Void> deleteRoomCategory(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomCategoryId) {
        if (roomCategoryService.deleteRoomCategory(motelChainId, motelId, roomCategoryId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
