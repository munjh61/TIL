package kuke.board.comment.service.request;

import lombok.Getter;

/**
 * 댓글 생성을 요청할 때 사용하는 데이터 전송 객체(DTO) (V2).
 */
@Getter
public class CommentCreateRequestV2 {
    private Long articleId; // 댓글을 달 게시글 ID
    private String content; // 내용
    /**
     * 대댓글일 경우, 부모 댓글의 경로(path).
     * V1의 parentCommentId 대신 경로 문자열을 사용하여 부모를 식별합니다.
     */
    private String parentPath;
    private Long writerId; // 작성자 ID
}