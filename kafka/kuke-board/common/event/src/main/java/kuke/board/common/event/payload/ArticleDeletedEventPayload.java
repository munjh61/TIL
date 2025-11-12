package kuke.board.common.event.payload;

import kuke.board.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * '게시글 삭제' 이벤트 발생 시 전달되는 데이터(페이로드)를 담는 클래스입니다.
 * EventPayload 인터페이스를 구현하여 이 클래스가 이벤트 데이터임을 명시합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDeletedEventPayload implements EventPayload {
    private Long articleId; // 삭제된 게시글의 ID
    private String title; // 삭제된 게시글의 제목
    private String content; // 삭제된 게시글의 내용
    private Long boardId; // 게시글이 속했던 게시판의 ID
    private Long writerId; // 작성자의 ID
    private LocalDateTime createdAt; // 생성 시각
    private LocalDateTime modifiedAt; // 수정 시각
    private Long boardArticleCount; // 해당 게시판의 총 게시글 수
}