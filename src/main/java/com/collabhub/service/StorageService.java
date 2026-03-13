package com.collabhub.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Store a file and return the stored file name/key.
     */
    String store(MultipartFile file) throws IOException;

    /**
     * Load a file as an InputStream.
     */
    InputStream load(String storedFileName) throws IOException;

    /**
     * Delete a stored file.
     */
    void delete(String storedFileName) throws IOException;
}
