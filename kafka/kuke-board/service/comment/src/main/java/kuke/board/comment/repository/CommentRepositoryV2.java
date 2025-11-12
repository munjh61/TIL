package kuke.board.comment.repository;

import kuke.board.comment.entity.CommentV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CommentV2 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * '구체화된 경로(Materialized Path)' 패턴을 활용하여 계층 구조 쿼리를 효율적으로 수행합니다.
 */
@Repository
public interface CommentRepositoryV2 extends JpaRepository<CommentV2, Long> {

    /**
     * 정확한 경로(path)로 댓글을 조회합니다.
     */
    @Query("select c from CommentV2 c where c.commentPath.path = :path")
    Optional<CommentV2> findByPath(@Param("path") String path);

    /**
     * 특정 댓글(pathPrefix)의 후손들 중 가장 마지막 후손의 경로를 찾습니다.
     * 새로운 대댓글을 추가할 때, 그 대댓글의 경로를 결정하기 위해 사용됩니다.
     *
     * 예: 'A'의 후손으로 'A/B', 'A/C'가 있다면, 'A/C'를 반환합니다.
     *    새로운 대댓글은 이 'A/C'를 기준으로 다음 경로('A/D')를 부여받게 됩니다.
     */
    @Query(
            value = "select path from comment_v2 " +
                    "where article_id = :articleId and path > :pathPrefix and path like :pathPrefix% " +
                    "order by path desc limit 1",
            nativeQuery = true
    )
    Optional<String> findDescendantsTopPath(
            @Param("articleId") Long articleId,
            @Param("pathPrefix") String pathPrefix
    );

    /**
     * 오프셋 기반 페이지네이션으로 댓글 목록을 조회합니다.
     * 'path' 컬럼의 사전적 정렬 순서가 곧 댓글의 계층적 정렬 순서이므로,
     * 'path asc'로 정렬하는 것만으로 올바른 순서의 댓글 목록을 얻을 수 있습니다.
     */
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from (" +
                    "   select comment_id from comment_v2 where article_id = :articleId " +
                    "   order by path asc " +
                    "   limit :limit offset :offset " +
                    ") t left join comment_v2 on t.comment_id = comment_v2.comment_id",
            nativeQuery = true
    )
    List<CommentV2> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    /**
     * 특정 게시글의 댓글 수를 제한적으로 카운트합니다.
     */
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment_v2 where article_id = :articleId limit :limit " +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    /**
     * 무한 스크롤의 첫 페이지를 조회합니다.
     */
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from comment_v2 " +
                    "where article_id = :articleId " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );


    /**
     * 무한 스크롤의 다음 페이지를 조회합니다.
     * V1과 달리, 마지막 댓글의 'path' 문자열 하나만 커서로 사용하여 간단하게 다음 페이지를 조회할 수 있습니다.
     */
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id, " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at " +
                    "from comment_v2 " +
                    "where article_id = :articleId and path > :lastPath " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastPath") String lastPath,
            @Param("limit") Long limit
    );
}