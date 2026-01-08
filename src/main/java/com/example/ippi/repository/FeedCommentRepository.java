package com.example.ippi.repository;

import com.example.ippi.entity.Activity;
import com.example.ippi.entity.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    
    List<FeedComment> findByActivityOrderByCreatedAtAsc(Activity activity);
    
    long countByActivity(Activity activity);
}
