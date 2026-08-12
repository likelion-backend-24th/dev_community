package com.likelion.dev_community.domain.like.repository;

import com.likelion.dev_community.domain.like.entity.LikeHistory;
import com.likelion.dev_community.domain.like.entity.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeHistoryRepository extends JpaRepository<LikeHistory, Long> {

    Optional<LikeHistory> findByUserIdAndTargetTypeAndTargetId(
            Long userId, LikeTargetType targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(
            Long userId, LikeTargetType targetType, Long targetId);

    // 여러 대상(targetIds) 중 사용자가 이미 추천한 대상 id만 조회 (N+1 방지용 일괄 조회)
    @Query("select l.targetId from LikeHistory l " +
            "where l.user.id = :userId and l.targetType = :targetType and l.targetId in :targetIds")
    List<Long> findLikedTargetIds(
            @Param("userId") Long userId,
            @Param("targetType") LikeTargetType targetType,
            @Param("targetIds") List<Long> targetIds);
}
