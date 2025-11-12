package kuke.board.comment.repository;

import kuke.board.comment.entity.ArticleCommentCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ArticleCommentCount 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * 게시글별 댓글 수를 증가시키거나 감소시키는 기능을 제공합니다.
 */
@Repository
public interface ArticleCommentCountRepository extends JpaRepository<ArticleCommentCount, Long> {

    /**
     * 특정 게시글의 댓글 수를 1 증가시킵니다.
     * @Modifying 어노테이션은 이 쿼리가 데이터베이스 상태를 변경함을 나타냅니다.
     * @param articleId 댓글 수를 증가시킬 게시글의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update article_comment_count set comment_count = comment_count + 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("articleId") Long articleId);

    /**
     * 특정 게시글의 댓글 수를 1 감소시킵니다.
     * @Modifying 어노테이션은 이 쿼리가 데이터베이스 상태를 변경함을 나타냅니다.
     * @param articleId 댓글 수를 감소시킬 게시글의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update article_comment_count set comment_count = comment_count - 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("articleId") Long articleId);
}