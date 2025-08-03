package com.example.motels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.motels.service.RoomService;
import com.example.motels.model.Room;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/motelApi/v1/motels/chains/{motelChainId}/motels/{motelId}/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(@PathVariable UUID motelChainId, @PathVariable UUID motelId) {
        List<Room> rooms = roomService.getAllRooms(motelChainId, motelId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomId) {
        return roomService.getRoomById(motelChainId, motelId, roomId)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @RequestBody Room room) {
        Room createdRoom = roomService.createRoom(motelChainId, motelId, room);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomId, @RequestBody Room room) {
        Room updatedRoom = roomService.updateRoom(motelChainId, motelId, roomId, room);
        return updatedRoom != null ? new ResponseEntity<>(updatedRoom, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID motelChainId, @PathVariable UUID motelId, @PathVariable UUID roomId) {
        if (roomService.deleteRoom(motelChainId, motelId, roomId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
