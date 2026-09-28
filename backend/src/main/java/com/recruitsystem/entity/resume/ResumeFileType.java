package com.recruitsystem.entity.resume;

public enum ResumeFileType {
    PDF("application/pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final String contentType;

    ResumeFileType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public static ResumeFileType fromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> PDF;
            case "docx" -> DOCX;
            default -> throw new IllegalArgumentException("Unsupported resume file type: " + extension);
        };
    }
}
