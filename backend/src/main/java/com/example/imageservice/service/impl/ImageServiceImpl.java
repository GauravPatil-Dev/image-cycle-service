package com.example.imageservice.service.impl;

import com.example.imageservice.model.Image;
import com.example.imageservice.model.ImageMetadata;
import com.example.imageservice.service.ImageService;

import jakarta.annotation.PostConstruct;

import com.example.imageservice.notification.ImageNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for handling image storage, metadata, and notifications.
 * Uses in-memory storage for metadata and file system for image files.
 */
@Service
public class ImageServiceImpl implements ImageService {
    // In-memory stores for images and metadata
    private final Map<String, Image> imageStore = new LinkedHashMap<>();
    private final Map<String, ImageMetadata> metadataStore = new ConcurrentHashMap<>();
    private final String imageDir = "images";

    @Autowired
    ImageNotificationService notificationService; // package-private for test injection

    /**
     * Ensure the image directory exists on service initialization.
     */
    public ImageServiceImpl() {
        File dir = new File(imageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    /**
     * Save an uploaded image file to disk and store its metadata in memory.
     * Notifies all clients of the new image.
     */
    public Image saveImage(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        String filename = id + "_" + file.getOriginalFilename();
        Path path = Paths.get(imageDir, filename);
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
        Image image = new Image(id, file.getOriginalFilename(), path.toString());
        imageStore.put(id, image);
        ImageMetadata metadata = new ImageMetadata(id, file.getOriginalFilename(), file.getContentType(), file.getSize());
        metadataStore.put(id, metadata);
        notificationService.notifyClients(image);
        return image;
    }

    @Override
    /**
     * Get a list of all stored images.
     */
    public List<Image> getAllImages() {
        return new ArrayList<>(imageStore.values());
    }

    @Override
    /**
     * Get image metadata by ID.
     */
    public Image getImage(String id) {
        return imageStore.get(id);
    }

    @Override
    /**
     * Delete an image and its metadata by ID. Notifies clients of deletion.
     */
    public void deleteImage(String id) {
        Image image = imageStore.remove(id);
        metadataStore.remove(id);
        if (image != null) {
            Path path = Paths.get(image.getPath());
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Could log error here
            }
            notificationService.notifyClients("deleted:" + id);
        }
    }

    @PostConstruct
    public void loadImagesFromDisk() {
        try {
            Files.list(Paths.get(imageDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String filename = path.getFileName().toString();
                    if (filename.contains("_")) {
                        String[] parts = filename.split("_", 2);
                        String id = parts[0];
                        String originalName = parts[1];
                        System.out.println("[ImageService] Loading image: " + id + " - " + originalName);
                        Image image = new Image(id, originalName, path.toString());
                        imageStore.put(id, image);

                        try {
                            long size = Files.size(path);
                            String mimeType = Files.probeContentType(path);
                            ImageMetadata metadata = new ImageMetadata(id, originalName, mimeType, size);
                            metadataStore.put(id, metadata);
                        } catch (IOException e) {
                            // Could log file metadata reading error here
                        }
                    }
                });
            System.out.println("[ImageService] Reloaded " + imageStore.size() + " images from disk.");
        } catch (IOException e) {
            System.err.println("[ImageService] Failed to reload images from disk: " + e.getMessage());
        }
    }


    @Override
    /**
     * Get metadata for a specific image by ID.
     */
    public ImageMetadata getImageMetadata(String id) {
        return metadataStore.get(id);
    }

    @Override
    /**
     * Set or update metadata for a specific image.
     */
    public ImageMetadata setImageMetadata(String id, ImageMetadata metadata) {
        metadataStore.put(id, metadata);
        return metadata;
    }
}
