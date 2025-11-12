package kuke.board.like.repository;

import kuke.board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ArticleLike 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 */
@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    /**
     * 특정 게시글에 대한 특정 사용자의 좋아요 정보를 조회합니다.
     * 이 메서드는 사용자가 이미 해당 게시글에 '좋아요'를 눌렀는지 확인하거나,
     * '좋아요 취소' 기능을 구현할 때 사용됩니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @return 조회된 ArticleLike 객체를 담은 Optional. 존재하지 않으면 Optional.empty()
     */
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}