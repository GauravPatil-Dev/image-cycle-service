package com.example.imageservice.controller;

import com.example.imageservice.model.Image;
import com.example.imageservice.model.ImageMetadata;
import com.example.imageservice.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    /**
     * Upload an image file and store it on disk with metadata.
     * @param file Multipart file to upload
     * @return The stored Image object
     */
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }
        try {
            Image img = imageService.saveImage(file);
            return ResponseEntity.ok(img);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save image: " + e.getMessage());
        }
    }

    /**
     * Get a list of all stored images and their metadata.
     * @return List of Image objects
     */
    @GetMapping
    public ResponseEntity<List<Image>> getAllImages() {
        return ResponseEntity.ok(imageService.getAllImages());
    }

    /**
     * Get metadata for a specific image by ID.
     * @param id Image ID
     * @return Image object (metadata only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        Image img = imageService.getImage(id);
        if (img == null) {
            return ResponseEntity.status(404).body("Image not found");
        }
        return ResponseEntity.ok(img);
    }

    /**
     * Delete an image and its metadata by ID.
     * @param id Image ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable String id) {
        Image img = imageService.getImage(id);
        if (img == null) {
            return ResponseEntity.status(404).body("Image not found");
        }
        try {
            imageService.deleteImage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete image: " + e.getMessage());
        }
    }

    /**
     * Get metadata for a specific image by ID.
     * @param id Image ID
     * @return ImageMetadata object
     */
    @GetMapping("/{id}/metadata")
    public ResponseEntity<?> getImageMetadata(@PathVariable String id) {
        ImageMetadata meta = imageService.getImageMetadata(id);
        if (meta == null) {
            return ResponseEntity.status(404).body("Image metadata not found");
        }
        return ResponseEntity.ok(meta);
    }

    /**
     * Set or update metadata for a specific image.
     * @param id Image ID
     * @param metadata Metadata to set
     * @return Updated ImageMetadata object
     */
    @PostMapping("/{id}/metadata")
    public ResponseEntity<ImageMetadata> setImageMetadata(@PathVariable String id, @RequestBody ImageMetadata metadata) {
        return ResponseEntity.ok(imageService.setImageMetadata(id, metadata));
    }
    /**
     * Download the actual image file by ID (for display in frontend).
     * @param id Image ID
     * @return Raw image bytes with correct MIME type
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<?> getImageFile(@PathVariable String id) {
        Image image = imageService.getImage(id);
        if (image == null) {
            return ResponseEntity.status(404).body("Image not found");
        }
        try {
            Path path = Paths.get(image.getPath());
            byte[] data = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .header("Content-Type", mimeType != null ? mimeType : "application/octet-stream")
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to read image file");
        }
    }
}
