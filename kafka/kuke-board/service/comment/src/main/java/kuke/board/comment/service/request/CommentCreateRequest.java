package kuke.board.comment.service.request;

import lombok.Getter;

/**
 * 댓글 생성을 요청할 때 사용하는 데이터 전송 객체(DTO) (V1).
 */
@Getter
public class CommentCreateRequest {
    private Long articleId; // 댓글을 달 게시글 ID
    private String content; // 내용
    private Long parentCommentId; // 대댓글일 경우, 부모 댓글의 ID
    private Long writerId; // 작성자 ID
}