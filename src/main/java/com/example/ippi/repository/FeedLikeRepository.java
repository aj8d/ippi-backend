package com.example.ippi.repository;

import com.example.ippi.entity.Activity;
import com.example.ippi.entity.FeedLike;
import com.example.ippi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    
    Optional<FeedLike> findByActivityAndUser(Activity activity, User user);
    
    boolean existsByActivityAndUser(Activity activity, User user);
    
    long countByActivity(Activity activity);
    
    @Query("SELECT fl.activity.id FROM FeedLike fl WHERE fl.user = :user")
    List<Long> findLikedActivityIdsByUser(@Param("user") User user);
}
