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
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<Image> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.saveImage(file));
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
    public ResponseEntity<Image> getImage(@PathVariable String id) {
        return ResponseEntity.ok(imageService.getImage(id));
    }

    /**
     * Delete an image and its metadata by ID.
     * @param id Image ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get metadata for a specific image by ID.
     * @param id Image ID
     * @return ImageMetadata object
     */
    @GetMapping("/{id}/metadata")
    public ResponseEntity<ImageMetadata> getImageMetadata(@PathVariable String id) {
        return ResponseEntity.ok(imageService.getImageMetadata(id));
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
    public ResponseEntity<byte[]> getImageFile(@PathVariable String id) {
        Image image = imageService.getImage(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path path = Paths.get(image.getPath());
            byte[] data = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .header("Content-Type", mimeType != null ? mimeType : "application/octet-stream")
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
