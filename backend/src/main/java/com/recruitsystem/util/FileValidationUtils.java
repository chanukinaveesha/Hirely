package com.recruitsystem.util;

import com.recruitsystem.exception.ValidationException;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public final class FileValidationUtils {

    private FileValidationUtils() {
    }

    public static void validate(MultipartFile file, Set<String> allowedExtensions, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new ValidationException(
                    "File exceeds maximum allowed size of " + maxSizeBytes + " bytes");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (extension.isBlank() || !allowedExtensions.contains(extension.toLowerCase())) {
            throw new ValidationException(
                    "Unsupported file type. Allowed types: " + allowedExtensions);
        }
    }

    public static String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
