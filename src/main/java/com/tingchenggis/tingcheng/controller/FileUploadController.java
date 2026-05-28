package com.tingchenggis.tingcheng.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final Path UPLOAD_DIR = Paths.get("data", "uploads");

    @PostMapping("/photo")
    public ResponseEntity<Map<String, Object>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            Files.createDirectories(UPLOAD_DIR);
            String ext = getExtension(file.getOriginalFilename());
            String filename = LocalDate.now() + "/" + UUID.randomUUID() + ext;
            Path target = UPLOAD_DIR.resolve(filename);
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
            String url = "/api/upload/file/" + filename.replace("\\", "/");
            logger.info("Photo uploaded: {}", url);
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (IOException e) {
            logger.error("Upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/file/**")
    public ResponseEntity<?> getFile(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String path = request.getRequestURI().substring("/api/upload/file/".length());
            Path file = UPLOAD_DIR.resolve(path).normalize();
            if (!file.startsWith(UPLOAD_DIR.normalize())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid path"));
            }
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            byte[] data = Files.readAllBytes(file);
            String contentType = detectContentType(file.toString());
            return ResponseEntity.ok().header("Content-Type", contentType).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private String getExtension(String name) {
        if (name == null) return ".png";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : ".png";
    }

    private String detectContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        return "application/octet-stream";
    }
}
