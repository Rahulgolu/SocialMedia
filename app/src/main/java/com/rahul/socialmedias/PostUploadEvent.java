package com.rahul.socialmedias;

public class PostUploadEvent {
    private String message;

    public PostUploadEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
