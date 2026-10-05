package com.recruitsystem.avatar.repository;

import com.recruitsystem.avatar.entity.UserAvatar;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {

    Optional<UserAvatar> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
