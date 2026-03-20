package com.example.ippi.controller;

import com.example.ippi.dto.ProfileWidgetDTO;
import com.example.ippi.entity.ProfileWidget;
import com.example.ippi.entity.User;
import com.example.ippi.repository.ProfileWidgetRepository;
import com.example.ippi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile-widgets")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ProfileWidgetController {

    private final ProfileWidgetRepository profileWidgetRepository;
    private final UserRepository userRepository;

    public ProfileWidgetController(ProfileWidgetRepository profileWidgetRepository, UserRepository userRepository) {
        this.profileWidgetRepository = profileWidgetRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{customId}")
    public ResponseEntity<?> getProfileWidgets(@PathVariable String customId) {
        try {
            User user = userRepository.findByCustomId(customId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            List<ProfileWidgetDTO> widgets = profileWidgetRepository.findByUserOrderByDisplayOrderAsc(user)
                    .stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(widgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "プロフィールウィジェット取得に失敗: " + e.getMessage()));
        }
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> saveProfileWidgets(@RequestBody List<ProfileWidgetDTO> widgetDTOs, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            profileWidgetRepository.deleteByUser(user);

            long now = System.currentTimeMillis();
            List<ProfileWidget> widgets = java.util.stream.IntStream.range(0, widgetDTOs.size())
                    .mapToObj(index -> convertToEntity(widgetDTOs.get(index), user, index, now))
                    .toList();

            profileWidgetRepository.saveAll(widgets);

            return ResponseEntity.ok(Map.of(
                    "message", "保存しました",
                    "count", widgets.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "プロフィールウィジェット保存に失敗: " + e.getMessage()));
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private ProfileWidgetDTO convertToDTO(ProfileWidget widget) {
        return new ProfileWidgetDTO(
                widget.getWidgetId(),
                widget.getType(),
                widget.getWidth(),
                widget.getCustomText(),
                widget.getImageUrl(),
                widget.getLinkUrl()
        );
    }

    private ProfileWidget convertToEntity(ProfileWidgetDTO dto, User user, int displayOrder, long now) {
        return new ProfileWidget(
                user,
                dto.getId(),
                dto.getType(),
                dto.getWidth() == null || dto.getWidth().isBlank() ? "full" : dto.getWidth(),
                dto.getCustomText(),
                dto.getImageUrl(),
                dto.getLinkUrl(),
                displayOrder,
                now,
                now
        );
    }
}