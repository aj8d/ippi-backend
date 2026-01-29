package com.example.ippi.controller;

import com.example.ippi.dto.CommentDTO;
import com.example.ippi.dto.CommentRequest;
import com.example.ippi.dto.FeedItemDTO;
import com.example.ippi.entity.Activity;
import com.example.ippi.entity.FeedComment;
import com.example.ippi.entity.FeedLike;
import com.example.ippi.entity.User;
import com.example.ippi.repository.ActivityRepository;
import com.example.ippi.repository.FeedCommentRepository;
import com.example.ippi.repository.FeedLikeRepository;
import com.example.ippi.repository.FollowRepository;
import com.example.ippi.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feed")
@Validated
public class FeedController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    private FeedCommentRepository feedCommentRepository;

    // フィード取得（フォローしているユーザーのアクティビティ）
    @GetMapping
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = currentUserOpt.get();

        // フォローしているユーザーのIDリストを取得
        List<Long> followingIds = followRepository.findFollowingIdsByUser(currentUser);
        
        // 自分自身のIDも追加
        followingIds.add(currentUser.getId());

        if (followingIds.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "items", new ArrayList<>(),
                "hasMore", false,
                "message", "フォローしているユーザーがいません"
            ));
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Activity> activities = activityRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable);

        // 現在のユーザーがいいねしたアクティビティのIDリスト
        List<Long> likedActivityIds = feedLikeRepository.findLikedActivityIdsByUser(currentUser);

        // アチーブメントアクティビティを除外
        List<FeedItemDTO> feedItems = activities.stream()
            .filter(activity -> !Activity.TYPE_ACHIEVEMENT.equals(activity.getActivityType()))
            .map(activity -> {
            User user = activity.getUser();
            FeedItemDTO dto = new FeedItemDTO(
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
            
            // いいね数とコメント数を設定
            dto.setLikeCount(feedLikeRepository.countByActivity(activity));
            dto.setLiked(likedActivityIds.contains(activity.getId()));
            dto.setCommentCount(feedCommentRepository.countByActivity(activity));
            
            // コメントリストを取得
            List<FeedComment> comments = feedCommentRepository.findByActivityOrderByCreatedAtAsc(activity);
            List<CommentDTO> commentDTOs = comments.stream().map(comment -> 
                new CommentDTO(
                    comment.getId(),
                    comment.getUser().getId(),
                    comment.getUser().getName(),
                    comment.getUser().getProfileImageUrl(),
                    comment.getText(),
                    comment.getCreatedAt()
                )
            ).collect(Collectors.toList());
            dto.setComments(commentDTOs);
            
            return dto;
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
        
        // 現在のユーザー情報を取得
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        List<Long> likedActivityIds = new ArrayList<>();
        if (currentUserOpt.isPresent()) {
            likedActivityIds = feedLikeRepository.findLikedActivityIdsByUser(currentUserOpt.get());
        }
        
        List<Long> finalLikedActivityIds = likedActivityIds;
        List<FeedItemDTO> feedItems = activities.stream().map(activity -> {
            FeedItemDTO dto = new FeedItemDTO(
                activity.getId(),
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getCustomId(),
                targetUser.getProfileImageUrl(),
                activity.getActivityType(),
                activity.getMessage(),
                activity.getRelatedData(),
                activity.getCreatedAt()
            );
            
            // いいね数とコメント数を設定
            dto.setLikeCount(feedLikeRepository.countByActivity(activity));
            dto.setLiked(finalLikedActivityIds.contains(activity.getId()));
            dto.setCommentCount(feedCommentRepository.countByActivity(activity));
            
            // コメントリストを取得
            List<FeedComment> comments = feedCommentRepository.findByActivityOrderByCreatedAtAsc(activity);
            List<CommentDTO> commentDTOs = comments.stream().map(comment -> 
                new CommentDTO(
                    comment.getId(),
                    comment.getUser().getId(),
                    comment.getUser().getName(),
                    comment.getUser().getProfileImageUrl(),
                    comment.getText(),
                    comment.getCreatedAt()
                )
            ).collect(Collectors.toList());
            dto.setComments(commentDTOs);
            
            return dto;
        }).collect(Collectors.toList());

        boolean hasMore = activities.size() == size;

        return ResponseEntity.ok(Map.of(
            "items", feedItems,
            "hasMore", hasMore
        ));
    }

    // いいねを追加
    @PostMapping("/{feedId}/like")
    public ResponseEntity<?> likeActivity(
            @PathVariable Long feedId,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<Activity> activityOpt = activityRepository.findById(feedId);

        if (currentUserOpt.isEmpty() || activityOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Activity not found"));
        }

        User currentUser = currentUserOpt.get();
        Activity activity = activityOpt.get();

        // 既にいいねしている場合は何もしない
        if (feedLikeRepository.existsByActivityAndUser(activity, currentUser)) {
            return ResponseEntity.ok(Map.of("message", "Already liked"));
        }

        FeedLike feedLike = new FeedLike(activity, currentUser, System.currentTimeMillis());
        feedLikeRepository.save(feedLike);

        long likeCount = feedLikeRepository.countByActivity(activity);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "likeCount", likeCount
        ));
    }

    // いいねを削除
    @PostMapping("/{feedId}/unlike")
    public ResponseEntity<?> unlikeActivity(
            @PathVariable Long feedId,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<Activity> activityOpt = activityRepository.findById(feedId);

        if (currentUserOpt.isEmpty() || activityOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Activity not found"));
        }

        User currentUser = currentUserOpt.get();
        Activity activity = activityOpt.get();

        Optional<FeedLike> feedLikeOpt = feedLikeRepository.findByActivityAndUser(activity, currentUser);
        
        if (feedLikeOpt.isPresent()) {
            feedLikeRepository.delete(feedLikeOpt.get());
        }

        long likeCount = feedLikeRepository.countByActivity(activity);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "likeCount", likeCount
        ));
    }

    // コメントを追加
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long feedId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<Activity> activityOpt = activityRepository.findById(feedId);

        if (currentUserOpt.isEmpty() || activityOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Activity not found"));
        }

        String text = request.getText();

        User currentUser = currentUserOpt.get();
        Activity activity = activityOpt.get();

        FeedComment comment = new FeedComment(activity, currentUser, text.trim(), System.currentTimeMillis());
        feedCommentRepository.save(comment);

        CommentDTO commentDTO = new CommentDTO(
            comment.getId(),
            currentUser.getId(),
            currentUser.getName(),
            currentUser.getProfileImageUrl(),
            comment.getText(),
            comment.getCreatedAt()
        );
        
        return ResponseEntity.ok(commentDTO);
    }

    // コメントを削除
    @DeleteMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long feedId,
            @PathVariable Long commentId,
            Authentication authentication) {
        
        String email = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(email);
        Optional<FeedComment> commentOpt = feedCommentRepository.findById(commentId);

        if (currentUserOpt.isEmpty() || commentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User or Comment not found"));
        }

        User currentUser = currentUserOpt.get();
        FeedComment comment = commentOpt.get();

        // 自分のコメントかチェック
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to delete this comment"));
        }

        feedCommentRepository.delete(comment);
        
        return ResponseEntity.ok(Map.of("success", true));
    }
}
