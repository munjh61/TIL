package kuke.board.common.event.payload;

import kuke.board.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * '게시글 조회' 이벤트 발생 시 전달되는 데이터(페이로드)를 담는 클래스입니다.
 * 이 이벤트는 게시글의 조회수를 증가시키는 데 사용될 수 있습니다.
 * EventPayload 인터페이스를 구현하여 이 클래스가 이벤트 데이터임을 명시합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleViewedEventPayload implements EventPayload {
    private Long articleId; // 조회된 게시글의 ID
    private Long articleViewCount; // 해당 게시글의 총 조회수
}