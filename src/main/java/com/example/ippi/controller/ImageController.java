package com.example.ippi.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 📚 ImageController - 画像アップロード用コントローラー
 * 
 * キャンバスの画像ウィジェット用にCloudinaryへ画像をアップロードする
 * 
 * 注意: サーバーのコンテキストパスが /api なので、ここでは /images のみ
 */
@RestController
@RequestMapping("/images")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ImageController {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    public ImageController(Cloudinary cloudinary, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.userRepository = userRepository;
    }

    /**
     * 📚 画像をCloudinaryにアップロード
     * 
     * @param file アップロードする画像ファイル
     * @param principal 認証済みユーザー情報
     * @return アップロードされた画像のURL
     */
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

            // ファイルサイズチェック（5MB制限）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "ファイルサイズは5MB以下にしてください"));
            }

            // 画像タイプチェック
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "画像ファイルのみアップロードできます"));
            }

            // ユニークなpublic_idを生成（ユーザーID + タイムスタンプ）
            String publicId = "widget_" + user.getId() + "_" + System.currentTimeMillis();

            // Cloudinaryにアップロード
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ippi-widgets",
                            "public_id", publicId,
                            "resource_type", "auto"
                    ));

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicIdResult = (String) uploadResult.get("public_id");

            // レスポンスを返す
            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("publicId", publicIdResult);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "画像アップロードに失敗しました: " + e.getMessage()));
        }
    }

    /**
     * 📚 Cloudinaryから画像を削除
     * 
     * @param publicId 削除する画像のpublic_id
     * @param principal 認証済みユーザー情報
     * @return 削除結果
     */
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

            // publicIdにユーザーIDが含まれているかチェック（セキュリティ）
            if (!publicId.contains("widget_" + user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "この画像を削除する権限がありません"));
            }

            // Cloudinaryから削除
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            return ResponseEntity.ok(Map.of("result", result.get("result")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "画像削除に失敗しました: " + e.getMessage()));
        }
    }
}
