package com.recruitsystem.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Stores the given file under a subdirectory and returns the generated
     * relative filename it was stored under (not the original filename).
     */
    String store(MultipartFile file, String subDirectory);

    Resource load(String subDirectory, String storedFilename);

    void delete(String subDirectory, String storedFilename);
}
