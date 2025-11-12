package kuke.board.common.event.payload;

import kuke.board.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * '게시글 수정' 이벤트 발생 시 전달되는 데이터(페이로드)를 담는 클래스입니다.
 * EventPayload 인터페이스를 구현하여 이 클래스가 이벤트 데이터임을 명시합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdatedEventPayload implements EventPayload {
    private Long articleId; // 수정된 게시글의 ID
    private String title; // 수정된 게시글의 제목
    private String content; // 수정된 게시글의 내용
    private Long boardId; // 게시글이 속한 게시판의 ID
    private Long writerId; // 작성자의 ID
    private LocalDateTime createdAt; // 생성 시각
    private LocalDateTime modifiedAt; // 수정 시각
}