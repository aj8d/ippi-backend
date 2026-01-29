package com.example.ippi.service;

import com.example.ippi.entity.Activity;
import com.example.ippi.entity.User;
import com.example.ippi.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    public void createWorkCompletedActivity(User user, int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        String timeDisplay = hours + "時間" + mins + "分";
        String message = timeDisplay + "の作業を完了しました";
        String relatedData = "{\"minutes\":" + minutes + "}";
        
        Activity activity = new Activity(
            user,
            Activity.TYPE_WORK_COMPLETED,
            message,
            relatedData,
            System.currentTimeMillis()
        );
        activityRepository.save(activity);
    }

    /**
     * フォローされた時のアクティビティを作成
     * @param followedUser フォローされたユーザー（アクティビティの所有者）
     * @param follower フォローしたユーザー
     */
    public void createFollowedActivity(User followedUser, User follower) {
        String message = follower.getName() + "さんにフォローされました";
        String relatedData = "{\"followerId\":" + follower.getId() + ",\"followerName\":\"" + follower.getName() + "\"}";
        
        Activity activity = new Activity(
            followedUser,
            Activity.TYPE_FOLLOW,
            message,
            relatedData,
            System.currentTimeMillis()
        );
        activityRepository.save(activity);
    }
}
