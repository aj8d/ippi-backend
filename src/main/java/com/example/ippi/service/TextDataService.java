package com.example.ippi.service;

import com.example.ippi.entity.TextData;
import com.example.ippi.repository.TextDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TextDataService {

    @Autowired
    private TextDataRepository textDataRepository;

    // すべてのテキストデータを取得
    public List<TextData> getAllTextData() {
        return textDataRepository.findAll();
    }

    // ID でテキストデータを取得
    public Optional<TextData> getTextDataById(Long id) {
        return textDataRepository.findById(id);
    }

    // テキストデータを保存
    public TextData saveTextData(TextData textData) {
        return textDataRepository.save(textData);
    }

    // テキストデータを削除
    public void deleteTextData(Long id) {
        textDataRepository.deleteById(id);
    }

    // テキストデータを更新
    public TextData updateTextData(Long id, TextData updatedTextData) {
        Optional<TextData> existingData = textDataRepository.findById(id);
        if (existingData.isPresent()) {
            TextData data = existingData.get();
            data.setText(updatedTextData.getText());
            return textDataRepository.save(data);
        }
        return null;
    }
}
