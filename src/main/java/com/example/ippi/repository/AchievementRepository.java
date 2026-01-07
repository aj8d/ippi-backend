package com.example.ippi.repository;

import com.example.ippi.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    // タイプ別にアチーブメント一覧を取得
    List<Achievement> findByType(String type);
    
    // タイプと閾値で検索
    Optional<Achievement> findByTypeAndThreshold(String type, Long threshold);
    
    // 表示順序で並び替え
    List<Achievement> findAllByOrderByDisplayOrderAsc();
}
