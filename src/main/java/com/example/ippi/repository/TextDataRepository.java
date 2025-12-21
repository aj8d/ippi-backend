package com.example.ippi.repository;

import com.example.ippi.entity.TextData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextDataRepository extends JpaRepository<TextData, Long> {
    // JpaRepository が提供するメソッド:
    // findAll()      - すべてを取得
    // findById(id)   - ID で検索
    // save(entity)   - 保存・更新
    // delete(entity) - 削除
}
