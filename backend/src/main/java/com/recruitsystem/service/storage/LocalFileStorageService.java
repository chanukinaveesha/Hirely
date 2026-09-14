package com.recruitsystem.service.storage;

import com.recruitsystem.exception.ResourceNotFoundException;
import com.recruitsystem.exception.ValidationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not initialize upload directory: " + rootLocation, ex);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }
        try {
            Path targetDir = resolveSubDirectory(subDirectory);
            Files.createDirectories(targetDir);

            String extension = getExtension(file.getOriginalFilename());
            String storedFilename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
            Path destination = targetDir.resolve(storedFilename).normalize();

            file.transferTo(destination);
            return storedFilename;
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store file", ex);
        }
    }

    @Override
    public Resource load(String subDirectory, String storedFilename) {
        try {
            Path filePath = resolveSubDirectory(subDirectory).resolve(storedFilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + storedFilename);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + storedFilename);
        }
    }

    @Override
    public void delete(String subDirectory, String storedFilename) {
        try {
            Path filePath = resolveSubDirectory(subDirectory).resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to delete file: " + storedFilename, ex);
        }
    }

    private Path resolveSubDirectory(String subDirectory) {
        Path resolved = rootLocation.resolve(subDirectory).normalize();
        if (!resolved.startsWith(rootLocation)) {
            throw new ValidationException("Invalid storage path");
        }
        return resolved;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
