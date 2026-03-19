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
import com.example.ippi.util.FileValidationUtil;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String DEFAULT_PROFILE_THEME_PRESET = "paper";
    private static final String DEFAULT_PROFILE_THEME_JSON = "{\"mode\":\"gradient\",\"solidColor\":\"#239a3b\",\"gradientFrom\":\"#7bc96f\",\"gradientTo\":\"#196127\",\"gradientAngle\":135}";
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TextDataService textDataService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         JwtTokenProvider jwtTokenProvider, TextDataService textDataService,
                         GoogleTokenVerifier googleTokenVerifier, Cloudinary cloudinary,
                         ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.textDataService = textDataService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.cloudinary = cloudinary;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
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
        user.setProfileThemePreset(DEFAULT_PROFILE_THEME_PRESET);
        user.setProfileThemeJson(DEFAULT_PROFILE_THEME_JSON);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        // データベースに保存
        User savedUser = userRepository.save(user);

        // JWT トークン発行
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        // レスポンス返却
        return ResponseEntity.ok(buildAuthResponse(token, savedUser, savedUser.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
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
        return ResponseEntity.ok(buildAuthResponse(token, user, user.getEmail()));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        return ResponseEntity.ok(buildAuthResponse(null, user, user.getEmail()));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable String id) {
        // customId で検索
        User user = userRepository.findByCustomId(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // プロフィール情報を返却
        return ResponseEntity.ok(buildAuthResponse(null, user, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable String id) {
        // customId で検索
        User user = userRepository.findByCustomId(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        // プロフィール情報を返却
        return ResponseEntity.ok(buildAuthResponse(null, user, null));
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
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
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
                user.setProfileThemePreset(DEFAULT_PROFILE_THEME_PRESET);
                user.setProfileThemeJson(DEFAULT_PROFILE_THEME_JSON);
                user = userRepository.save(user);
            }

            // JWT トークン発行
            String token = jwtTokenProvider.generateToken(user.getEmail());

            // レスポンス返却
            return ResponseEntity.ok(buildAuthResponse(token, user, user.getEmail()));

        } catch (IOException e) {
            logger.error("Googleログインエラー: 無効なIDトークン", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("認証に失敗しました");
        } catch (GeneralSecurityException e) {
            logger.error("Googleログインエラー: トークン検証失敗", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("認証に失敗しました");
        } catch (Exception e) {
            logger.error("Googleログインエラー: 予期しないエラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("サーバーエラーが発生しました");
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

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("ファイルサイズは5MB以下にしてください");
            }

            byte[] fileBytes = file.getBytes();

            if (!FileValidationUtil.isValidImage(fileBytes)) {
                return ResponseEntity.badRequest().body("有効な画像ファイルをアップロードしてください");
            }

            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                ObjectUtils.asMap(
                    "folder", "ippi-profiles",
                    "public_id", "user_" + user.getId(),
                    "type", "private",
                    "overwrite", true,
                    "resource_type", "auto"
                ));

            String imageUrl = (String) uploadResult.get("secure_url");

            user.setProfileImageUrl(imageUrl);
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            return ResponseEntity.ok(buildAuthResponse(null, user, user.getEmail()));
        } catch (IOException e) {
            logger.error("プロフィール画像アップロードエラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("画像アップロードに失敗しました");
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
            if (updateData.getProfileThemePreset() != null && !updateData.getProfileThemePreset().isBlank()) {
                user.setProfileThemePreset(updateData.getProfileThemePreset());
            }
            if (updateData.getProfileTheme() != null) {
                user.setProfileThemeJson(serializeProfileTheme(updateData.getProfileTheme()));
            }
            if (updateData.getProfileBackgroundUrl() != null) {
                String trimmedBackgroundUrl = updateData.getProfileBackgroundUrl().trim();
                user.setProfileBackgroundUrl(trimmedBackgroundUrl.isEmpty() ? null : trimmedBackgroundUrl);
            }

            if (user.getProfileThemeJson() == null || user.getProfileThemeJson().isBlank()) {
                user.setProfileThemeJson(DEFAULT_PROFILE_THEME_JSON);
            }

            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            return ResponseEntity.ok(buildAuthResponse(null, user, user.getEmail()));
        } catch (Exception e) {
            logger.error("プロフィール更新エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("プロフィール更新に失敗しました");
        }
    }

    private AuthResponse buildAuthResponse(String token, User user, String email) {
        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                email,
                user.getName(),
                user.getProfileImageUrl(),
                user.getDescription(),
                user.getCustomId(),
                user.getProfileThemePreset()
        );
        response.setProfileTheme(parseStoredProfileTheme(user.getProfileThemeJson()));
        response.setProfileBackgroundUrl(user.getProfileBackgroundUrl());
        return response;
    }

    @PostMapping("/upload-profile-background")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadProfileBackground(
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

            if (file.getSize() > 8 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("ファイルサイズは8MB以下にしてください");
            }

            byte[] fileBytes = file.getBytes();

            if (!FileValidationUtil.isValidImage(fileBytes)) {
                return ResponseEntity.badRequest().body("有効な画像ファイルをアップロードしてください");
            }

            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                ObjectUtils.asMap(
                    "folder", "ippi-profile-backgrounds",
                    "public_id", "bg_user_" + user.getId(),
                    "type", "private",
                    "overwrite", true,
                    "resource_type", "auto"
                ));

            String imageUrl = (String) uploadResult.get("secure_url");

            user.setProfileBackgroundUrl(imageUrl);
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            return ResponseEntity.ok(buildAuthResponse(null, user, user.getEmail()));
        } catch (IOException e) {
            logger.error("プロフィール背景画像アップロードエラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("背景画像アップロードに失敗しました");
        }
    }

    private Map<String, Object> parseStoredProfileTheme(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return defaultProfileTheme();
        }

        try {
            Map<String, Object> parsed = objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
            return sanitizeProfileTheme(parsed);
        } catch (Exception e) {
            logger.warn("プロフィールテーマJSONのパースに失敗したためデフォルトを返却します", e);
            return defaultProfileTheme();
        }
    }

    private String serializeProfileTheme(Object rawTheme) {
        try {
            Map<String, Object> input = objectMapper.convertValue(rawTheme, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> sanitized = sanitizeProfileTheme(input);
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception e) {
            logger.warn("プロフィールテーマJSONのシリアライズに失敗したためデフォルトを保存します", e);
            return DEFAULT_PROFILE_THEME_JSON;
        }
    }

    private Map<String, Object> defaultProfileTheme() {
        try {
            return objectMapper.readValue(DEFAULT_PROFILE_THEME_JSON, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("mode", "gradient");
            fallback.put("solidColor", "#239a3b");
            fallback.put("gradientFrom", "#7bc96f");
            fallback.put("gradientTo", "#196127");
            fallback.put("gradientAngle", 135);
            return fallback;
        }
    }

    private Map<String, Object> sanitizeProfileTheme(Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> defaults = defaultProfileTheme();

        String mode = asString(input.get("mode"));
        if (!"solid".equals(mode)) {
            mode = "gradient";
        }

        String solidColor = sanitizeHex(asString(input.get("solidColor")), asString(defaults.get("solidColor")));
        String gradientFrom = sanitizeHex(asString(input.get("gradientFrom")), asString(defaults.get("gradientFrom")));
        String gradientTo = sanitizeHex(asString(input.get("gradientTo")), asString(defaults.get("gradientTo")));
        int gradientAngle = sanitizeAngle(input.get("gradientAngle"), 135);

        result.put("mode", mode);
        result.put("solidColor", solidColor);
        result.put("gradientFrom", gradientFrom);
        result.put("gradientTo", gradientTo);
        result.put("gradientAngle", gradientAngle);
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String sanitizeHex(String value, String fallback) {
        if (value != null && HEX_COLOR_PATTERN.matcher(value).matches()) {
            return value;
        }
        return fallback;
    }

    private int sanitizeAngle(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            int angle = (int) Math.round(Double.parseDouble(value.toString()));
            if (angle < 0) {
                return 0;
            }
            if (angle > 360) {
                return 360;
            }
            return angle;
        } catch (Exception e) {
            return fallback;
        }
    }
}
