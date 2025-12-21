package com.example.ippi.controller;

import com.example.ippi.entity.TextData;
import com.example.ippi.service.TextDataService;
import com.example.ippi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/text-data")
public class TextDataController {

    @Autowired
    private TextDataService textDataService;
    
    @Autowired
    private UserRepository userRepository;

    // GET: ログインユーザーのテキストデータを取得
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TextData>> getAllTextData(Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<TextData> data = textDataService.getTextDataByUserId(userId);
        return ResponseEntity.ok(data);
    }

    // GET: ID でテキストデータを取得（所有者確認）
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> getTextDataById(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> data = textDataService.getTextDataById(id);
        if (data.isPresent() && data.get().getUserId().equals(userId)) {
            return ResponseEntity.ok(data.get());
        }
        return ResponseEntity.notFound().build();
    }

    // POST: 新しいテキストデータを作成（ユーザー ID を自動設定）
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> createTextData(@RequestBody TextData textData, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        textData.setUserId(userId);
        TextData savedData = textDataService.saveTextData(textData);
        return ResponseEntity.ok(savedData);
    }

    // PUT: テキストデータを更新（所有者確認）
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> updateTextData(
            @PathVariable Long id,
            @RequestBody TextData textData,
            Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            TextData updatedData = textDataService.updateTextData(id, textData);
            return ResponseEntity.ok(updatedData);
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE: テキストデータを削除（所有者確認）
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTextData(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            textDataService.deleteTextData(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
