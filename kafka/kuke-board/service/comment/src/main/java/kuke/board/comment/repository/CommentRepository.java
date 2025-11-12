package kuke.board.comment.repository;

import kuke.board.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment 엔티티(V1)에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 특정 부모 댓글에 달린 댓글 수를 제한적으로 카운트합니다.
     * UI 페이지네이션 로직을 위해 사용되는 것으로 보입니다.
     */
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment " +
                    "   where article_id = :articleId and parent_comment_id = :parentCommentId " +
                    "   limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long countBy(
            @Param("articleId") Long articleId,
            @Param("parentCommentId") Long parentCommentId,
            @Param("limit") Long limit
    );

    /**
     * 오프셋 기반 페이지네이션으로 댓글 목록을 조회합니다.
     * 'parent_comment_id'로 먼저 정렬하여 같은 부모를 가진 댓글(대댓글)들을 그룹화하고,
     * 그 안에서 'comment_id'로 정렬하여 시간순으로 보여줍니다.
     */
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from (" +
                    "   select comment_id from comment where article_id = :articleId " +
                    "   order by parent_comment_id asc, comment_id asc " +
                    "   limit :limit offset :offset " +
                    ") t left join comment on t.comment_id = comment.comment_id",
            nativeQuery = true
    )
    List<Comment> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    /**
     * 특정 게시글의 댓글 수를 제한적으로 카운트합니다.
     */
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment where article_id = :articleId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    /**
     * 무한 스크롤의 첫 페이지를 조회합니다.
     * 정렬 순서는 오프셋 페이징과 동일합니다.
     */
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId " +
                    "order by parent_comment_id asc, comment_id asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    /**
     * 무한 스크롤의 다음 페이지를 조회합니다.
     * (parent_comment_id, comment_id) 복합 키를 커서로 사용하여 페이지네이션을 수행합니다.
     * 이는 "seek method" 또는 "keyset pagination"이라 불리며, 딥 페이징 성능이 우수합니다.
     */
    @Query(
            value = "select comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "from comment " +
                    "where article_id = :articleId and (" +
                    "   parent_comment_id > :lastParentCommentId or " +
                    "   (parent_comment_id = :lastParentCommentId and comment_id > :lastCommentId) " +
                    ")" +
                    "order by parent_comment_id asc, comment_id asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastParentCommentId") Long lastParentCommentId,
            @Param("lastCommentId") Long lastCommentId,
            @Param("limit") Long limit
    );
}