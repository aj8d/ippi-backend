package com.example.ippi.controller;

import com.example.ippi.dto.AchievementDTO;
import com.example.ippi.entity.Achievement;
import com.example.ippi.entity.User;
import com.example.ippi.entity.UserAchievement;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    /**
     * ユーザーのアチーブメント達成状況を取得
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAchievements(Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        
        // 全アチーブメント定義を取得
        List<Achievement> allAchievements = achievementService.getAllAchievements();
        
        // ユーザーが達成したアチーブメントを取得
        List<UserAchievement> userAchievements = achievementService.getUserAchievements(user);
        
        // 達成済みアチーブメントのマップを作成
        Map<Long, UserAchievement> achievedMap = userAchievements.stream()
            .collect(Collectors.toMap(
                ua -> ua.getAchievement().getId(),
                ua -> ua
            ));
        
        // DTOに変換
        List<AchievementDTO> achievementDTOs = allAchievements.stream()
            .map(achievement -> {
                UserAchievement userAch = achievedMap.get(achievement.getId());
                return new AchievementDTO(
                    achievement.getId(),
                    achievement.getType(),
                    achievement.getName(),
                    achievement.getDescription(),
                    achievement.getThreshold(),
                    userAch != null,
                    userAch != null ? userAch.getAchievedAt() : null
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "achievements", achievementDTOs,
            "totalCount", allAchievements.size(),
            "achievedCount", userAchievements.size()
        ));
    }

    /**
     * 全アチーブメント定義を取得
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAchievements() {
        List<Achievement> achievements = achievementService.getAllAchievements();
        
        List<Map<String, Object>> achievementData = achievements.stream()
            .map(achievement -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", achievement.getId());
                map.put("type", achievement.getType());
                map.put("name", achievement.getName());
                map.put("description", achievement.getDescription());
                map.put("threshold", achievement.getThreshold());
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("achievements", achievementData));
    }
}
