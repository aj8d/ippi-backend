package com.example.ippi.repository;

import com.example.ippi.entity.User;
import com.example.ippi.entity.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📚 WidgetRepository
 * 
 * ウィジェットデータへのアクセスを提供
 * Spring Data JPA が自動的にSQL実装を生成
 */
@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {

    /**
     * 📚 ユーザーの全ウィジェットを取得
     */
    List<Widget> findByUser(User user);

    /**
     * 📚 ユーザーIDで全ウィジェットを取得
     */
    List<Widget> findByUserId(Long userId);

    /**
     * 📚 ユーザーとwidgetIdでウィジェットを検索
     */
    Optional<Widget> findByUserAndWidgetId(User user, String widgetId);

    /**
     * 📚 ユーザーの全ウィジェットを削除
     */
    void deleteByUser(User user);

    /**
     * 📚 ユーザーとwidgetIdでウィジェットを削除
     */
    void deleteByUserAndWidgetId(User user, String widgetId);
}
