package com.example.todo.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final String EICAR_SIGNATURE = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE";

    private final Path storageDir;
    private final Set<String> allowedExt;
    private final Set<String> allowedMime;

    public FileStorageService(
        @Value("${app.file.storage-dir:uploads}") String storageDir,
        @Value("${app.file.allowed-ext:}") String allowedExt,
        @Value("${app.file.allowed-mime:}") String allowedMime
    ) {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        this.allowedExt = toLowerSet(allowedExt);
        this.allowedMime = toLowerSet(allowedMime);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイルを選択してください。");
        }
        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = extractExtension(originalName);
        if (!allowedExt.isEmpty() && (ext == null || !allowedExt.contains(ext))) {
            throw new IllegalArgumentException("許可されていない拡張子です。");
        }
        String contentType = file.getContentType();
        if (!allowedMime.isEmpty() && contentType != null && !allowedMime.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("許可されていないファイル形式です。");
        }
        byte[] bytes = file.getBytes();
        if (containsEicar(bytes)) {
            throw new IllegalArgumentException("ウイルスの可能性があるファイルです。");
        }
        Files.createDirectories(storageDir);
        String storedName = generateStoredName(ext);
        Path target = storageDir.resolve(storedName).normalize();
        if (!target.startsWith(storageDir)) {
            throw new IllegalArgumentException("不正なファイルパスです。");
        }
        Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        return new StoredFile(originalName, storedName, contentType, bytes.length);
    }

    public Resource loadAsResource(String storedName) throws IOException {
        String safeName = sanitizeStoredName(storedName);
        Path target = storageDir.resolve(safeName).normalize();
        if (!target.startsWith(storageDir)) {
            throw new IllegalArgumentException("不正なファイルパスです。");
        }
        Resource resource = new UrlResource(target.toUri());
        if (!resource.exists()) {
            throw new IllegalArgumentException("ファイルが見つかりません。");
        }
        return resource;
    }

    public void delete(String storedName) throws IOException {
        String safeName = sanitizeStoredName(storedName);
        Path target = storageDir.resolve(safeName).normalize();
        if (!target.startsWith(storageDir)) {
            throw new IllegalArgumentException("不正なファイルパスです。");
        }
        Files.deleteIfExists(target);
    }

    private String sanitizeFileName(String original) {
        String clean = StringUtils.hasText(original) ? StringUtils.cleanPath(original) : "file";
        clean = clean.replace("\\", "/");
        if (clean.contains("..")) {
            throw new IllegalArgumentException("不正なファイル名です。");
        }
        int lastSlash = clean.lastIndexOf('/');
        String base = lastSlash >= 0 ? clean.substring(lastSlash + 1) : clean;
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!StringUtils.hasText(base)) {
            base = "file";
        }
        return base;
    }

    private String sanitizeStoredName(String storedName) {
        String clean = StringUtils.hasText(storedName) ? StringUtils.cleanPath(storedName) : "";
        if (!StringUtils.hasText(clean) || clean.contains("..") || clean.contains("/") || clean.contains("\\")) {
            throw new IllegalArgumentException("不正なファイル名です。");
        }
        return clean;
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String generateStoredName(String ext) {
        String base = UUID.randomUUID().toString();
        return ext == null ? base : base + "." + ext;
    }

    private boolean containsEicar(byte[] bytes) {
        String content = new String(bytes, StandardCharsets.US_ASCII);
        return content.contains(EICAR_SIGNATURE);
    }

    private Set<String> toLowerSet(String csv) {
        if (!StringUtils.hasText(csv)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .toList());
    }

    public static class StoredFile {
        private final String originalName;
        private final String storedName;
        private final String contentType;
        private final long size;

        public StoredFile(String originalName, String storedName, String contentType, long size) {
            this.originalName = originalName;
            this.storedName = storedName;
            this.contentType = contentType;
            this.size = size;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getStoredName() {
            return storedName;
        }

        public String getContentType() {
            return contentType;
        }

        public long getSize() {
            return size;
        }
    }
}
