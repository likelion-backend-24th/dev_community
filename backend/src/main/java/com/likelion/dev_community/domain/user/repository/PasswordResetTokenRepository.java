package com.likelion.dev_community.domain.user.repository;

import com.likelion.dev_community.domain.user.entity.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}
