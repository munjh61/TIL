package kuke.board.comment.service.response;

import jakarta.persistence.Id;
import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentV2;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 댓글 정보를 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 * V1과 V2 엔티티를 모두 지원하여 일관된 응답 형식을 제공합니다.
 */
@Getter
@ToString
public class CommentResponse {
    private Long commentId;
    private String content;
    private Long parentCommentId; // V1 필드
    private Long articleId;
    private Long writerId;
    private Boolean deleted;
    private String path; // V2 필드
    private LocalDateTime createdAt;

    /**
     * V1 Comment 엔티티로부터 CommentResponse DTO를 생성합니다.
     * @param comment 변환할 V1 Comment 엔티티
     * @return 생성된 CommentResponse 객체
     */
    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.parentCommentId = comment.getParentCommentId();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.createdAt = comment.getCreatedAt();
        return response;
    }

    /**
     * V2 CommentV2 엔티티로부터 CommentResponse DTO를 생성합니다.
     * @param comment 변환할 V2 CommentV2 엔티티
     * @return 생성된 CommentResponse 객체
     */
    public static CommentResponse from(CommentV2 comment) {
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.content = comment.getContent();
        response.path = comment.getCommentPath().getPath();
        response.articleId = comment.getArticleId();
        response.writerId = comment.getWriterId();
        response.deleted = comment.getDeleted();
        response.createdAt = comment.getCreatedAt();
        return response;
    }
}