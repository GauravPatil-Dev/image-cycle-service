package com.example.imageservice.model;

public class ImageMetadata {
    private String id;
    private String name;
    private String mimeType;
    private long size;

    public ImageMetadata() {}

    public ImageMetadata(String id, String name, String mimeType, long size) {
        this.id = id;
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
