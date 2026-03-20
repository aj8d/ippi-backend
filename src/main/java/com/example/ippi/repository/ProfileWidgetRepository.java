package com.example.ippi.repository;

import com.example.ippi.entity.ProfileWidget;
import com.example.ippi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileWidgetRepository extends JpaRepository<ProfileWidget, Long> {
    List<ProfileWidget> findByUserOrderByDisplayOrderAsc(User user);

    void deleteByUser(User user);
}