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

    // 作業完了アクティビティを作成
    public void createWorkCompletedActivity(User user, int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        String timeDisplay = hours + "時間" + mins + "分";
        String message = user.getName() + "さんが" + timeDisplay + "の作業を完了しました";
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

    // アチーブメントアクティビティを作成
    public void createAchievementActivity(User user, String achievementName, String achievementDescription) {
        String message = user.getName() + "さんが「" + achievementName + "」を達成しました！";
        String relatedData = "{\"achievementName\":\"" + achievementName + "\",\"description\":\"" + achievementDescription + "\"}";
        
        Activity activity = new Activity(
            user,
            Activity.TYPE_ACHIEVEMENT,
            message,
            relatedData,
            System.currentTimeMillis()
        );
        activityRepository.save(activity);
    }

    // フォローアクティビティを作成
    public void createFollowActivity(User follower, User following) {
        String message = follower.getName() + "さんが" + following.getName() + "さんをフォローしました";
        String relatedData = "{\"targetUserId\":" + following.getId() + "}";
        
        Activity activity = new Activity(
            follower,
            Activity.TYPE_FOLLOW,
            message,
            relatedData,
            System.currentTimeMillis()
        );
        activityRepository.save(activity);
    }
}
