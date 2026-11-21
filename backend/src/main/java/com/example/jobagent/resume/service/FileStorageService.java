package com.example.jobagent.resume.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(
            @Value("${app.storage.resume-dir}") String storageDirectory) {

        this.storageLocation = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not initialize resume storage directory",
                    exception
            );
        }
    }



    public String store(MultipartFile file, String extension) {

        String storageKey = UUID.randomUUID().toString();

        String filename = storageKey + extension;

        Path destination = storageLocation.resolve(filename).normalize();

        if (!destination.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return storageKey;

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to store resume file",
                    exception
            );
        }
    }

    public void delete(String storageKey, String extension) {

        Path filePath = storageLocation
                .resolve(storageKey + extension)
                .normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to delete resume file",
                    exception
            );
        }
    }
    public Path load(String storageKey, String extension) {

        Path filePath = storageLocation
                .resolve(storageKey + extension)
                .normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("Resume file not found");
        }

        return filePath;
    }

}