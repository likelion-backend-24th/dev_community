package com.likelion.dev_community.domain.user.repository;

import com.likelion.dev_community.domain.user.entity.OAuthSignupInfo;
import org.springframework.data.repository.CrudRepository;

public interface OAuthSignupInfoRepository extends CrudRepository<OAuthSignupInfo, String> {
}