package com.recruitsystem.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.recruitsystem.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageValidationUtilsTest {

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00};
    private static final byte[] PNG_BYTES =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
    private static final byte[] WEBP_BYTES =
            {'R', 'I', 'F', 'F', 0x00, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P'};

    @Test
    void validate_acceptsJpegWithMatchingMagicBytes() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        assertThat(ImageValidationUtils.validate(file)).isEqualTo(ImageContentType.JPEG);
    }

    @Test
    void validate_acceptsPngWithMatchingMagicBytes() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", PNG_BYTES);

        assertThat(ImageValidationUtils.validate(file)).isEqualTo(ImageContentType.PNG);
    }

    @Test
    void validate_acceptsWebpWithMatchingMagicBytes() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", WEBP_BYTES);

        assertThat(ImageValidationUtils.validate(file)).isEqualTo(ImageContentType.WEBP);
    }

    @Test
    void validate_rejectsMagicBytesThatDontMatchDeclaredContentType() {
        // Declares PNG but the bytes are actually a JPEG header — a renamed file.
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", JPEG_BYTES);

        assertThatThrownBy(() -> ImageValidationUtils.validate(file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validate_rejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", JPEG_BYTES);

        assertThatThrownBy(() -> ImageValidationUtils.validate(file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validate_rejectsOversizedFile() {
        byte[] tooLarge = new byte[(int) ImageValidationUtils.MAX_IMAGE_SIZE_BYTES + 1];
        System.arraycopy(JPEG_BYTES, 0, tooLarge, 0, JPEG_BYTES.length);
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", tooLarge);

        assertThatThrownBy(() -> ImageValidationUtils.validate(file)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validate_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> ImageValidationUtils.validate(file)).isInstanceOf(ValidationException.class);
    }
}
