package com.example.ippi.controller;

import com.example.ippi.dto.UserStatsDTO;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.service.UserStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * ユーザー統計コントローラー
 */
@RestController
@RequestMapping("/user-stats")
public class UserStatsController {

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 自分の統計を取得
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        UserStatsDTO stats = userStatsService.getStatsByUserId(userOpt.get().getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * 特定ユーザーの統計を取得
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        UserStatsDTO stats = userStatsService.getStatsByUserId(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * customIdでユーザーの統計を取得
     */
    @GetMapping("/user/{customId}")
    public ResponseEntity<?> getUserStatsByCustomId(@PathVariable String customId) {
        Optional<User> userOpt = userRepository.findByCustomId(customId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        UserStatsDTO stats = userStatsService.getStatsByUserId(userOpt.get().getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * customIdでユーザーの日別アクティビティを取得（カレンダー用）
     */
    @GetMapping("/user/{customId}/daily-activity")
    public ResponseEntity<?> getDailyActivityByCustomId(@PathVariable String customId) {
        Optional<User> userOpt = userRepository.findByCustomId(customId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        var dailyActivity = userStatsService.getDailyActivity(userOpt.get().getId());
        return ResponseEntity.ok(Map.of("stats", dailyActivity));
    }
}
