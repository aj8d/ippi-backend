package com.example.ippi.repository;

import com.example.ippi.entity.Follow;
import com.example.ippi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // フォロー関係が存在するか確認
    boolean existsByFollowerAndFollowing(User follower, User following);

    // フォロー関係を取得
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // フォロワー一覧を取得（自分をフォローしているユーザー）
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user ORDER BY f.createdAt DESC")
    List<User> findFollowersByUser(@Param("user") User user);

    // フォロー中一覧を取得（自分がフォローしているユーザー）
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user ORDER BY f.createdAt DESC")
    List<User> findFollowingByUser(@Param("user") User user);

    // フォロワー数を取得
    long countByFollowing(User following);

    // フォロー中の数を取得
    long countByFollower(User follower);

    // フォロー中のユーザーIDリストを取得（フィード用）
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower = :user")
    List<Long> findFollowingIdsByUser(@Param("user") User user);
}
