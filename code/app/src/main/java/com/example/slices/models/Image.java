package com.example.slices.models;

public class Image {
    private String path;
    private String url;

    public Image(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public Image () {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
