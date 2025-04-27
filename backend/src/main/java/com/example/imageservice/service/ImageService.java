package com.example.imageservice.service;

import com.example.imageservice.model.Image;
import com.example.imageservice.model.ImageMetadata;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageService {
    Image saveImage(MultipartFile file);
    List<Image> getAllImages();
    Image getImage(String id);
    void deleteImage(String id);
    ImageMetadata getImageMetadata(String id);
    ImageMetadata setImageMetadata(String id, ImageMetadata metadata);
}
