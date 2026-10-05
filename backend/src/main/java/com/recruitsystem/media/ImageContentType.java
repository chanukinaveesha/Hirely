package com.recruitsystem.media;

public enum ImageContentType {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    private final String mimeType;

    ImageContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static ImageContentType fromMimeType(String mimeType) {
        for (ImageContentType type : values()) {
            if (type.mimeType.equalsIgnoreCase(mimeType)) {
                return type;
            }
        }
        return null;
    }
}
