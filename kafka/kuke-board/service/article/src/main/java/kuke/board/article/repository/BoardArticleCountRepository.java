package kuke.board.article.repository;

import kuke.board.article.entity.BoardArticleCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardArticleCount 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * 게시판별 게시글 수를 증가시키거나 감소시키는 기능을 제공합니다.
 */
@Repository
public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {

    /**
     * 특정 게시판의 게시글 수를 1 증가시킵니다.
     * @Modifying 어노테이션은 이 쿼리가 데이터베이스 상태를 변경함을 나타냅니다.
     * @param boardId 게시글 수를 증가시킬 게시판의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update board_article_count set article_count = article_count + 1 where board_id = :boardId",
            nativeQuery = true
    )
    @Modifying
    int increase(@Param("boardId") Long boardId);

    /**
     * 특정 게시판의 게시글 수를 1 감소시킵니다.
     * @Modifying 어노테이션은 이 쿼리가 데이터베이스 상태를 변경함을 나타냅니다.
     * @param boardId 게시글 수를 감소시킬 게시판의 ID
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update board_article_count set article_count = article_count - 1 where board_id = :boardId",
            nativeQuery = true
    )
    @Modifying
    int decrease(@Param("boardId") Long boardId);
}