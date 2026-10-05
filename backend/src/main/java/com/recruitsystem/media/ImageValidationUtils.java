package com.recruitsystem.media;

import com.recruitsystem.exception.ValidationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Shared image upload validation for avatars and post images. Deliberately
 * standalone from util.FileValidationUtils: that one only checks the file
 * extension, while images need their magic bytes sniffed so a renamed .exe
 * can't masquerade as a .jpg.
 */
public final class ImageValidationUtils {

    public static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;

    private ImageValidationUtils() {
    }

    /**
     * Validates size, declared content type, and magic bytes, and returns
     * the sniffed image type. Throws ValidationException on any mismatch.
     */
    public static ImageContentType validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Image file must not be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ValidationException("Image exceeds the maximum allowed size of 2 MB");
        }

        ImageContentType declaredType = ImageContentType.fromMimeType(file.getContentType());
        if (declaredType == null) {
            throw new ValidationException("Unsupported image type. Allowed types: JPG, PNG, WEBP");
        }

        ImageContentType sniffedType = sniffMagicBytes(file);
        if (sniffedType == null || sniffedType != declaredType) {
            throw new ValidationException("The file's contents do not match a supported image type (JPG, PNG, WEBP)");
        }

        return sniffedType;
    }

    private static ImageContentType sniffMagicBytes(MultipartFile file) {
        byte[] header;
        try {
            header = readHeader(file, 12);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read image file", ex);
        }

        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return ImageContentType.JPEG;
        }

        if (header.length >= 4
                && (header[0] & 0xFF) == 0x89
                && (header[1] & 0xFF) == 0x50
                && (header[2] & 0xFF) == 0x4E
                && (header[3] & 0xFF) == 0x47) {
            return ImageContentType.PNG;
        }

        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return ImageContentType.WEBP;
        }

        return null;
    }

    private static byte[] readHeader(MultipartFile file, int maxBytes) throws IOException {
        try (var inputStream = file.getInputStream()) {
            byte[] buffer = new byte[maxBytes];
            int read = inputStream.read(buffer);
            if (read <= 0) {
                return new byte[0];
            }
            if (read == buffer.length) {
                return buffer;
            }
            byte[] trimmed = new byte[read];
            System.arraycopy(buffer, 0, trimmed, 0, read);
            return trimmed;
        }
    }
}
