package com.example.imageservice.controller;

import com.example.imageservice.notification.ImageNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ImageNotificationController {
    @Autowired
    private ImageNotificationService notificationService;

    @GetMapping("/api/images/stream")
    public SseEmitter streamImages() {
        return notificationService.subscribe();
    }
}
