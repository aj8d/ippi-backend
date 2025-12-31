package com.example.ippi.controller;

import com.example.ippi.dto.UserSearchResult;
import com.example.ippi.entity.User;
import com.example.ippi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー検索API
     * @param query 検索クエリ（name または customId に部分一致）
     * @return 検索結果のリスト
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<User> users = userRepository.searchByNameOrCustomId(query.trim());
        
        List<UserSearchResult> results = users.stream()
                .map(user -> new UserSearchResult(
                        user.getId(),
                        user.getName(),
                        user.getCustomId(),
                        user.getProfileImageUrl(),
                        user.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
