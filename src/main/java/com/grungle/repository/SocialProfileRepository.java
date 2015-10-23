package com.grungle.repository;

import com.grungle.domain.SocialProfile;
import com.grungle.social.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by ahmedaly on 2015-10-21.
 */
public interface SocialProfileRepository extends JpaRepository<SocialProfile, Long> {

    SocialProfile findByEmailAndPlatform(String email, SocialPlatform platform);

}