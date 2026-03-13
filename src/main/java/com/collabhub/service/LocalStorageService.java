package com.collabhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Profile("!prod")  // Active for dev and default profiles
public class LocalStorageService implements StorageService {

    private final Path uploadDir;

    public LocalStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String extension = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String storedName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    @Override
    public InputStream load(String storedFileName) throws IOException {
        Path file = uploadDir.resolve(storedFileName).normalize();
        return Files.newInputStream(file);
    }

    @Override
    public void delete(String storedFileName) throws IOException {
        Path file = uploadDir.resolve(storedFileName).normalize();
        Files.deleteIfExists(file);
    }
}
