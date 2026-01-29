package com.example.ippi.controller;

import com.example.ippi.dto.FollowStatsDTO;
import com.example.ippi.dto.FollowUserDTO;
import com.example.ippi.entity.Follow;
import com.example.ippi.entity.User;
import com.example.ippi.repository.FollowRepository;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityService activityService;

    // フォローする
    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (currentUserOpt.isEmpty() || targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();

        // 自分自身をフォローできない
        if (currentUser.getId().equals(targetUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot follow yourself"));
        }

        // 既にフォローしているか確認
        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already following this user"));
        }

        // フォロー作成
        Follow follow = new Follow(currentUser, targetUser, System.currentTimeMillis());
        followRepository.save(follow);

        // フォローされたユーザーのフィードにアクティビティを作成
        activityService.createFollowedActivity(targetUser, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully followed user");
        response.put("followersCount", followRepository.countByFollowing(targetUser));

        return ResponseEntity.ok(response);
    }

    // フォロー解除
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (currentUserOpt.isEmpty() || targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();

        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        if (followOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not following this user"));
        }

        followRepository.delete(followOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully unfollowed user");
        response.put("followersCount", followRepository.countByFollowing(targetUser));

        return ResponseEntity.ok(response);
    }

    // フォロー統計を取得
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getFollowStats(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User targetUser = targetUserOpt.get();
        long followersCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);

        boolean isFollowing = false;
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        }

        return ResponseEntity.ok(new FollowStatsDTO(followersCount, followingCount, isFollowing));
    }

    // フォロワー一覧を取得
    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId, @org.springframework.lang.Nullable Authentication authentication) {
        final Optional<User> currentUserOpt;
        if (authentication != null) {
            String email = authentication.getName();
            currentUserOpt = userRepository.findByEmail(email);
        } else {
            currentUserOpt = Optional.empty();
        }
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User targetUser = targetUserOpt.get();
        List<User> followers = followRepository.findFollowersByUser(targetUser);

        List<FollowUserDTO> followerDTOs = followers.stream().map(user -> {
            boolean isFollowing = false;
            if (currentUserOpt.isPresent()) {
                isFollowing = followRepository.existsByFollowerAndFollowing(currentUserOpt.get(), user);
            }
            return new FollowUserDTO(
                user.getId(),
                user.getName(),
                user.getCustomId(),
                user.getProfileImageUrl(),
                user.getDescription(),
                isFollowing
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(followerDTOs);
    }

    // フォロー中一覧を取得
    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId, @org.springframework.lang.Nullable Authentication authentication) {
        final Optional<User> currentUserOpt;
        if (authentication != null) {
            String email = authentication.getName();
            currentUserOpt = userRepository.findByEmail(email);
        } else {
            currentUserOpt = Optional.empty();
        }
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User targetUser = targetUserOpt.get();
        List<User> following = followRepository.findFollowingByUser(targetUser);

        List<FollowUserDTO> followingDTOs = following.stream().map(user -> {
            boolean isFollowingUser = false;
            if (currentUserOpt.isPresent()) {
                isFollowingUser = followRepository.existsByFollowerAndFollowing(currentUserOpt.get(), user);
            }
            return new FollowUserDTO(
                user.getId(),
                user.getName(),
                user.getCustomId(),
                user.getProfileImageUrl(),
                user.getDescription(),
                isFollowingUser
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(followingDTOs);
    }

    // フォロー状態を確認
    @GetMapping("/check/{userId}")
    public ResponseEntity<?> checkFollowStatus(@PathVariable Long userId, Authentication authentication) {
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (currentUserOpt.isEmpty() || targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);

        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}
