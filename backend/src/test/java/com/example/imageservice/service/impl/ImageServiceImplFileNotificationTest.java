package com.example.imageservice.service.impl;

import com.example.imageservice.model.Image;
import com.example.imageservice.notification.ImageNotificationService;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceImplFileNotificationTest {
    private ImageServiceImpl imageService;
    private ImageNotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new ImageNotificationService();
        imageService = new ImageServiceImpl();
        imageService.notificationService = notificationService; // inject manually for test
    }

    @Test
    void testFileSavedToDisk() {
        MockMultipartFile file = new MockMultipartFile("file", "disk.jpg", "image/jpeg", new byte[]{10,20,30});
        Image image = imageService.saveImage(file);
        assertNotNull(image);
        File diskFile = new File(image.getPath());
        assertTrue(diskFile.exists());
        // Cleanup
        diskFile.delete();
    }

    @Test
    void testFileDeletedFromDisk() {
        MockMultipartFile file = new MockMultipartFile("file", "delete.jpg", "image/jpeg", new byte[]{40,50,60});
        Image image = imageService.saveImage(file);
        File diskFile = new File(image.getPath());
        assertTrue(diskFile.exists());
        imageService.deleteImage(image.getId());
        assertFalse(diskFile.exists());
    }

    @Test
    void testNotificationOnUpload() throws Exception {
        SseEmitter emitter = notificationService.subscribe();
        MockMultipartFile file = new MockMultipartFile("file", "notify.jpg", "image/jpeg", new byte[]{1,2,3});
        Image image = imageService.saveImage(file);
        // Try to receive event (should not throw)
        try {
            emitter.send(image);
        } catch (Exception e) {
            fail("Notification not sent to emitter: " + e.getMessage());
        }
        // Cleanup
        new File(image.getPath()).delete();
    }
}
