package kuke.board.like.repository;

import jakarta.persistence.LockModeType;
import kuke.board.like.entity.ArticleLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ArticleLikeCount 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * 좋아요 수 카운터의 동시성 제어를 위해 비관적 잠금(Pessimistic Locking)을 사용합니다.
 */
@Repository
public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {

    /**
     * 특정 게시글의 좋아요 수 카운터 엔티티를 조회하면서 비관적 쓰기 잠금(PESSIMISTIC_WRITE)을 획득합니다.
     * 이는 해당 엔티티를 읽고 수정하는 동안 다른 트랜잭션이 접근하지 못하도록 하여
     * 동시성 문제를 방지하고 데이터의 일관성을 보장합니다.
     *
     * @param articleId 게시글 ID
     * @return 잠금된 ArticleLikeCount 엔티티를 담은 Optional
     */
    // select ... for update (데이터베이스 레벨에서 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);


    /**
     * 특정 게시글의 좋아요 수를 1 증가시킵니다.
     * 네이티브 쿼리를 사용하여 직접 업데이트하며, JPA 엔티티의 버전 필드(낙관적 잠금)는 이 쿼리에서 직접 처리되지 않습니다.
     * @param articleId 좋아요 수를 증가시킬 게시글의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update article_like_count set like_count = like_count + 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("articleId") Long articleId);

    /**
     * 특정 게시글의 좋아요 수를 1 감소시킵니다.
     * 네이티브 쿼리를 사용하여 직접 업데이트하며, JPA 엔티티의 버전 필드(낙관적 잠금)는 이 쿼리에서 직접 처리되지 않습니다.
     * @param articleId 좋아요 수를 감소시킬 게시글의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update article_like_count set like_count = like_count - 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("articleId") Long articleId);
}