package com.example.ippi.controller;

import com.example.ippi.dto.AuthRequest;
import com.example.ippi.dto.AuthResponse;
import com.example.ippi.dto.GoogleLoginRequest;
import com.example.ippi.dto.StatsResponse;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.security.GoogleTokenVerifier;
import com.example.ippi.security.JwtTokenProvider;
import com.example.ippi.service.TextDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TextDataService textDataService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         JwtTokenProvider jwtTokenProvider, TextDataService textDataService,
                         GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.textDataService = textDataService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        // メール重複チェック
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("このメールアドレスは既に登録されています");
        }

        // 新規ユーザーを作成
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getEmail().split("@")[0]);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        // データベースに保存
        User savedUser = userRepository.save(user);

        // JWT トークン発行
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        // レスポンス返却
        return ResponseEntity.ok(new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getName()));
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
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName()));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getEmail(), user.getName()));
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
                // 新規ユーザーを作成
                user = new User(email, name, googleId, email, System.currentTimeMillis(), System.currentTimeMillis());
                user = userRepository.save(user);
            }

            // JWT トークン発行
            String token = jwtTokenProvider.generateToken(user.getEmail());

            // レスポンス返却
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName()));

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
}
