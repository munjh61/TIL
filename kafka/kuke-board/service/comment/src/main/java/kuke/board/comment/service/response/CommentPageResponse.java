package kuke.board.comment.service.response;

import lombok.Getter;

import java.util.List;

/**
 * 페이징 처리된 댓글 목록을 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
public class CommentPageResponse {
    private List<CommentResponse> comments; // 현재 페이지의 댓글 목록
    private Long commentCount; // 댓글 수 (주의: 전체 댓글 수가 아닐 수 있음. 페이지네이션 UI 계산을 위한 값)

    /**
     * CommentPageResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param comments 댓글 목록
     * @param commentCount 댓글 수
     * @return 생성된 CommentPageResponse 객체
     */
    public static CommentPageResponse of(List<CommentResponse> comments, Long commentCount) {
        CommentPageResponse response = new CommentPageResponse();
        response.comments = comments;
        response.commentCount = commentCount;
        return response;
    }
}