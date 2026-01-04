package com.example.ippi.dto;

public class FollowStatsDTO {
    private long followersCount;
    private long followingCount;
    private boolean isFollowing; // ログインユーザーがこのユーザーをフォローしているか

    public FollowStatsDTO() {}

    public FollowStatsDTO(long followersCount, long followingCount, boolean isFollowing) {
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isFollowing = isFollowing;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
