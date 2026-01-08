package com.example.ippi.controller;

import com.example.ippi.dto.AuthRequest;
import com.example.ippi.dto.AuthResponse;
import com.example.ippi.dto.GoogleLoginRequest;
import com.example.ippi.dto.StatsResponse;
import com.example.ippi.entity.TextData;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.security.GoogleTokenVerifier;
import com.example.ippi.security.JwtTokenProvider;
import com.example.ippi.service.TextDataService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TextDataService textDataService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final Cloudinary cloudinary;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         JwtTokenProvider jwtTokenProvider, TextDataService textDataService,
                         GoogleTokenVerifier googleTokenVerifier, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.textDataService = textDataService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.cloudinary = cloudinary;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        // メール重複チェック
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("このメールアドレスは既に登録されています");
        }

        // customId 重複チェック
        if (request.getCustomId() != null && userRepository.findByCustomId(request.getCustomId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("このIDは既に使用されています");
        }

        // customId が必須
        if (request.getCustomId() == null || request.getCustomId().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("IDは必須です");
        }

        // 新規ユーザーを作成
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getEmail().split("@")[0]);
        user.setCustomId(request.getCustomId().trim());
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        // データベースに保存
        User savedUser = userRepository.save(user);

        // JWT トークン発行
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        // レスポンス返却
        return ResponseEntity.ok(new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getProfileImageUrl(), savedUser.getDescription(), savedUser.getCustomId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // ユーザーを取得
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("メールアドレスまたはパスワードが違います");
        }

        // パスワード検証
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("メールアドレスまたはパスワードが違います");
        }

        // JWT トークン発行
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // レスポンス返却
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable String id) {
        // customId で検索
        User user = userRepository.findByCustomId(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // プロフィール情報を返却
        return ResponseEntity.ok(new AuthResponse(null, user.getId(), null, user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable String id) {
        // customId で検索
        User user = userRepository.findByCustomId(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // プロフィール情報を返却
        return ResponseEntity.ok(new AuthResponse(null, user.getId(), null, user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getStats(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // 過去 365 日間の日別集計を取得
        return ResponseEntity.ok(new StatsResponse(textDataService.getUserStatsForYear(user.getId())));
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            // Google ID Token を検証
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                googleTokenVerifier.getPayload(request.getIdToken());

            // ペイロードからユーザー情報を抽出
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // ユーザーが存在するかチェック
            Optional<User> existingUser = userRepository.findByEmail(email);

            User user;
            if (existingUser.isPresent()) {
                // 既存ユーザーがある場合、Google IDを更新
                user = existingUser.get();
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    user.setGoogleEmail(email);
                    user.setUpdatedAt(System.currentTimeMillis());
                    user = userRepository.save(user);
                }
            } else {
                // 新規ユーザーを作成（Google では customId が必須なので、Google ID から生成する）
                // または、Google ユーザーは後で customId を設定する必要がある
                user = new User(email, name, googleId, email, System.currentTimeMillis(), System.currentTimeMillis());
                user = userRepository.save(user);
            }

            // JWT トークン発行
            String token = jwtTokenProvider.generateToken(user.getEmail());

            // レスポンス返却
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("無効な Google ID Token: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Google トークン検証に失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("予期しないエラーが発生しました: " + e.getMessage());
        }
    }

    @GetMapping("/debug/text-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDebugTextData(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // すべてのテキストデータを取得（デバッグ用）
        List<TextData> textDataList = textDataService.getTextDataByUserId(user.getId());
        return ResponseEntity.ok(textDataList);
    }

    @PostMapping("/upload-profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("ファイルが選択されていません");
            }

            // Cloudinary にアップロード
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "ippi-profiles",
                    "public_id", "user_" + user.getId(),
                    "type", "private",
                    "overwrite", true,
                    "resource_type", "auto"
                ));

            String imageUrl = (String) uploadResult.get("secure_url");

            // ユーザーに画像URL を保存
            user.setProfileImageUrl(imageUrl);
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getEmail(), user.getName(), imageUrl, user.getDescription(), user.getCustomId()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("画像アップロードに失敗しました: " + e.getMessage());
        }
    }

    @PostMapping("/update-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @RequestBody AuthResponse updateData,
            Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
            }

            // customId の更新処理
            if (updateData.getCustomId() != null && !updateData.getCustomId().trim().isEmpty()) {
                String newCustomId = updateData.getCustomId().trim();
                // 新しい customId が現在の customId と異なる場合のみ重複チェック
                if (!newCustomId.equals(user.getCustomId())) {
                    if (userRepository.findByCustomId(newCustomId).isPresent()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("このIDは既に使用されています");
                    }
                }
                user.setCustomId(newCustomId);
            }

            // 名前と説明を更新
            if (updateData.getName() != null && !updateData.getName().isEmpty()) {
                user.setName(updateData.getName());
            }
            if (updateData.getDescription() != null) {
                user.setDescription(updateData.getDescription());
            }

            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl(), user.getDescription(), user.getCustomId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("プロフィール更新に失敗しました: " + e.getMessage());
        }
    }
}
