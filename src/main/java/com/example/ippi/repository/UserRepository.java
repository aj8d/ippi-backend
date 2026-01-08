package com.example.ippi.repository;

import com.example.ippi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCustomId(String customId);
    
    // ユーザー検索: name または customId に部分一致
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.customId) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByNameOrCustomId(@Param("query") String query);
}
