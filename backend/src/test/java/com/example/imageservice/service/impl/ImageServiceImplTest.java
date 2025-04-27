package com.example.imageservice.service.impl;

import com.example.imageservice.model.Image;
import com.example.imageservice.model.ImageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceImplTest {
    private ImageServiceImpl imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl();
    }

    // This test is now obsolete since saveImage is implemented. Consider updating or removing.
    // @Test
    // void testSaveAndGetImage() {
    //     MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1,2,3});
    //     Image image = imageService.saveImage(file);
    //     assertNull(image, "saveImage should return null as not implemented");
    // }

    @Test
    void testGetAllImagesInitiallyEmpty() {
        List<Image> images = imageService.getAllImages();
        assertTrue(images.isEmpty());
    }

    @Test
    void testDeleteImage() {
        imageService.deleteImage("nonexistent"); // Should not throw
    }

    @Test
    void testSetAndGetImageMetadata() {
        ImageMetadata metadata = new ImageMetadata("id","name","mime",123);
        imageService.setImageMetadata("id", metadata);
        ImageMetadata found = imageService.getImageMetadata("id");
        assertEquals(metadata, found);
    }
}
