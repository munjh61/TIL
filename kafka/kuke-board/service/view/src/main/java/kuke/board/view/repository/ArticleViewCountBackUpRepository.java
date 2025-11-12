package kuke.board.view.repository;

import kuke.board.view.entity.ArticleViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ArticleViewCount 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * 주로 Redis에 있는 실시간 조회수 데이터를 데이터베이스에 백업(영구 저장)하는 역할을 합니다.
 */
@Repository
public interface ArticleViewCountBackUpRepository extends JpaRepository<ArticleViewCount, Long> {

    /**
     * 특정 게시글의 조회수를 업데이트합니다.
     * 중요한 점은, 새로운 조회수(:viewCount)가 현재 데이터베이스에 저장된 조회수보다 클 경우에만 업데이트를 수행합니다.
     * 이는 Redis에서 가져온 데이터가 순서가 뒤바뀌거나 중복 처리될 경우,
     * 오래된 값으로 최신 값을 덮어쓰는 것을 방지하여 데이터의 일관성을 유지합니다.
     *
     * @param articleId 게시글 ID
     * @param viewCount 업데이트할 새로운 조회수
     * @return 영향을 받은 행(row)의 수
     */
    @Query(
            value = "update article_view_count set view_count = :viewCount " +
                    "where article_id = :articleId and view_count < :viewCount",
            nativeQuery = true
    )
    @Modifying
    int updateViewCount(
            @Param("articleId") Long articleId,
            @Param("viewCount") Long viewCount
    );
}