package com.example.imageservice.service.impl;

import com.example.imageservice.model.Image;
import com.example.imageservice.model.ImageMetadata;
import com.example.imageservice.service.ImageService;
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
    private final Map<String, Image> imageStore = new ConcurrentHashMap<>();
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
