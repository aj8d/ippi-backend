package com.example.ippi.controller;

import com.example.ippi.dto.FeedItemDTO;
import com.example.ippi.entity.Activity;
import com.example.ippi.entity.User;
import com.example.ippi.repository.ActivityRepository;
import com.example.ippi.repository.FollowRepository;
import com.example.ippi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    // フィード取得（フォローしているユーザーのアクティビティ）
    @GetMapping
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = currentUserOpt.get();

        // フォローしているユーザーのIDリストを取得
        List<Long> followingIds = followRepository.findFollowingIdsByUser(currentUser);

        if (followingIds.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "items", new ArrayList<>(),
                "hasMore", false,
                "message", "フォローしているユーザーがいません"
            ));
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Activity> activities = activityRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable);

        List<FeedItemDTO> feedItems = activities.stream().map(activity -> {
            User user = activity.getUser();
            return new FeedItemDTO(
                activity.getId(),
                user.getId(),
                user.getName(),
                user.getCustomId(),
                user.getProfileImageUrl(),
                activity.getActivityType(),
                activity.getMessage(),
                activity.getRelatedData(),
                activity.getCreatedAt()
            );
        }).collect(Collectors.toList());

        // 次のページがあるかどうか
        boolean hasMore = activities.size() == size;

        return ResponseEntity.ok(Map.of(
            "items", feedItems,
            "hasMore", hasMore
        ));
    }

    // 特定ユーザーのアクティビティ取得
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Optional<User> targetUserOpt = userRepository.findById(userId);

        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Activity> activities = activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        User targetUser = targetUserOpt.get();
        List<FeedItemDTO> feedItems = activities.stream().map(activity -> 
            new FeedItemDTO(
                activity.getId(),
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getCustomId(),
                targetUser.getProfileImageUrl(),
                activity.getActivityType(),
                activity.getMessage(),
                activity.getRelatedData(),
                activity.getCreatedAt()
            )
        ).collect(Collectors.toList());

        boolean hasMore = activities.size() == size;

        return ResponseEntity.ok(Map.of(
            "items", feedItems,
            "hasMore", hasMore
        ));
    }
}
