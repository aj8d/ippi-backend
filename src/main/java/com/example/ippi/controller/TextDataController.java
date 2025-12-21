package com.example.ippi.controller;

import com.example.ippi.entity.TextData;
import com.example.ippi.service.TextDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/text-data")
public class TextDataController {

    @Autowired
    private TextDataService textDataService;

    // GET: すべてのテキストデータを取得
    // フロント: GET http://localhost:8080/api/text-data
    @GetMapping
    public ResponseEntity<List<TextData>> getAllTextData() {
        List<TextData> data = textDataService.getAllTextData();
        return ResponseEntity.ok(data);
    }

    // GET: ID でテキストデータを取得
    // フロント: GET http://localhost:8080/api/text-data/1
    @GetMapping("/{id}")
    public ResponseEntity<TextData> getTextDataById(@PathVariable Long id) {
        Optional<TextData> data = textDataService.getTextDataById(id);
        return data.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: 新しいテキストデータを作成
    // フロント: POST http://localhost:8080/api/text-data
    @PostMapping
    public ResponseEntity<TextData> createTextData(@RequestBody TextData textData) {
        TextData savedData = textDataService.saveTextData(textData);
        return ResponseEntity.ok(savedData);
    }

    // PUT: テキストデータを更新
    // フロント: PUT http://localhost:8080/api/text-data/1
    @PutMapping("/{id}")
    public ResponseEntity<TextData> updateTextData(
            @PathVariable Long id,
            @RequestBody TextData textData) {
        TextData updatedData = textDataService.updateTextData(id, textData);
        if (updatedData != null) {
            return ResponseEntity.ok(updatedData);
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE: テキストデータを削除
    // フロント: DELETE http://localhost:8080/api/text-data/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTextData(@PathVariable Long id) {
        textDataService.deleteTextData(id);
        return ResponseEntity.noContent().build();
    }
}
