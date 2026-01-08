package com.example.ippi.controller;

import com.example.ippi.dto.WidgetDTO;
import com.example.ippi.entity.User;
import com.example.ippi.entity.Widget;
import com.example.ippi.repository.UserRepository;
import com.example.ippi.repository.WidgetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * キャンバス上のウィジェットの保存・読み込み・削除を行うAPI
 */
@RestController
@RequestMapping("/widgets")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class WidgetController {

    private final WidgetRepository widgetRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public WidgetController(WidgetRepository widgetRepository, 
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.widgetRepository = widgetRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * ユーザーの全ウィジェットを取得
     * 
     * GET /api/widgets
     * @return ウィジェット配列
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getWidgets(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            List<Widget> widgets = widgetRepository.findByUser(user);
            
            // Entity を DTO に変換
            List<WidgetDTO> widgetDTOs = widgets.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(widgetDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ウィジェット取得に失敗: " + e.getMessage()));
        }
    }

    /**
     * ウィジェットを一括保存（全置換）
     * 
     * PUT /api/widgets
     * フロントエンドの widgets 配列をそのまま保存
     * 既存のウィジェットは全て削除して新しいものに置き換え
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> saveWidgets(@RequestBody List<WidgetDTO> widgetDTOs, 
                                         Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            // 既存のウィジェットを全削除
            widgetRepository.deleteByUser(user);

            // 新しいウィジェットを保存
            List<Widget> widgets = widgetDTOs.stream()
                    .map(dto -> convertToEntity(dto, user))
                    .collect(Collectors.toList());

            widgetRepository.saveAll(widgets);

            return ResponseEntity.ok(Map.of(
                    "message", "保存しました",
                    "count", widgets.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ウィジェット保存に失敗: " + e.getMessage()));
        }
    }

    /**
     * 単一ウィジェットを追加/更新
     * 
     * POST /api/widgets
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> saveWidget(@RequestBody WidgetDTO widgetDTO, 
                                        Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            // 既存のウィジェットを検索
            Widget existingWidget = widgetRepository
                    .findByUserAndWidgetId(user, widgetDTO.getId())
                    .orElse(null);

            if (existingWidget != null) {
                // 更新
                updateWidgetFromDTO(existingWidget, widgetDTO);
                widgetRepository.save(existingWidget);
            } else {
                // 新規作成
                Widget newWidget = convertToEntity(widgetDTO, user);
                widgetRepository.save(newWidget);
            }

            return ResponseEntity.ok(Map.of("message", "保存しました"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ウィジェット保存に失敗: " + e.getMessage()));
        }
    }

    /**
     * ウィジェットを削除
     * 
     * DELETE /api/widgets/{widgetId}
     */
    @DeleteMapping("/{widgetId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> deleteWidget(@PathVariable String widgetId, 
                                          Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "ユーザーが見つかりません"));
            }

            widgetRepository.deleteByUserAndWidgetId(user, widgetId);

            return ResponseEntity.ok(Map.of("message", "削除しました"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "ウィジェット削除に失敗: " + e.getMessage()));
        }
    }

    // ========== ヘルパーメソッド ==========

    /**
     * Principal からユーザーを取得
     */
    private User getUserFromPrincipal(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Entity → DTO 変換
     */
    private WidgetDTO convertToDTO(Widget widget) {
        Object dataObj = null;
        if (widget.getData() != null && !widget.getData().isEmpty()) {
            try {
                dataObj = objectMapper.readValue(widget.getData(), Object.class);
            } catch (JsonProcessingException e) {
                dataObj = Map.of();
            }
        } else {
            dataObj = Map.of();
        }

        return new WidgetDTO(
                widget.getWidgetId(),
                widget.getType(),
                widget.getX(),
                widget.getY(),
                widget.getWidth(),
                widget.getHeight(),
                dataObj
        );
    }

    /**
     * DTO → Entity 変換
     */
    private Widget convertToEntity(WidgetDTO dto, User user) {
        String dataJson = "{}";
        if (dto.getData() != null) {
            try {
                dataJson = objectMapper.writeValueAsString(dto.getData());
            } catch (JsonProcessingException e) {
                dataJson = "{}";
            }
        }

        return new Widget(
                user,
                dto.getId(),
                dto.getType(),
                dto.getX(),
                dto.getY(),
                dto.getWidth(),
                dto.getHeight(),
                dataJson
        );
    }

    /**
     * 既存の Entity を DTO で更新
     */
    private void updateWidgetFromDTO(Widget widget, WidgetDTO dto) {
        widget.setType(dto.getType());
        widget.setX(dto.getX());
        widget.setY(dto.getY());
        widget.setWidth(dto.getWidth());
        widget.setHeight(dto.getHeight());
        widget.setUpdatedAt(System.currentTimeMillis());

        if (dto.getData() != null) {
            try {
                widget.setData(objectMapper.writeValueAsString(dto.getData()));
            } catch (JsonProcessingException e) {
                widget.setData("{}");
            }
        }
    }
}
