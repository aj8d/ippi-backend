package com.example.ippi.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.util.FileValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    public ImageController(Cloudinary cloudinary, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "ファイルが選択されていません"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "ファイルサイズは5MB以下にしてください"));
            }

            byte[] fileBytes = file.getBytes();

            if (!FileValidationUtil.isValidImage(fileBytes)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "有効な画像ファイルをアップロードしてください"));
            }

            String detectedType = FileValidationUtil.detectImageType(fileBytes);
            String contentType = file.getContentType();
            if (contentType != null && !contentType.startsWith("image/")) {
                logger.warn("Content-Type mismatch: header={}, detected={}", contentType, detectedType);
            }

            String publicId = "widget_" + user.getId() + "_" + System.currentTimeMillis();

            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap(
                            "folder", "ippi-widgets",
                            "public_id", publicId,
                            "resource_type", "auto"
                    ));

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicIdResult = (String) uploadResult.get("public_id");

            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("publicId", publicIdResult);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("画像アップロードエラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "画像アップロードに失敗しました"));
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteImage(
            @RequestParam("publicId") String publicId,
            Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            if (!publicId.contains("widget_" + user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "この画像を削除する権限がありません"));
            }

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            return ResponseEntity.ok(Map.of("result", result.get("result")));

        } catch (Exception e) {
            logger.error("画像削除エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "画像削除に失敗しました"));
        }
    }
}
