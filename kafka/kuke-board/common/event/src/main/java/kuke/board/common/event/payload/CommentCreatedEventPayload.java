package kuke.board.common.event.payload;

import kuke.board.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * '댓글 생성' 이벤트 발생 시 전달되는 데이터(페이로드)를 담는 클래스입니다.
 * EventPayload 인터페이스를 구현하여 이 클래스가 이벤트 데이터임을 명시합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEventPayload implements EventPayload {
    private Long commentId; // 생성된 댓글의 ID
    private String content; // 댓글 내용
    private String path; // 대댓글의 경우, 상위 댓글들의 ID 경로 (예: /1/3/)
    private Long articleId; // 댓글이 달린 게시글의 ID
    private Long writerId; // 작성자의 ID
    private Boolean deleted; // 삭제 여부 (생성 시점에는 항상 false)
    private LocalDateTime createdAt; // 생성 시각
    private Long articleCommentCount; // 해당 게시글의 총 댓글 수
}