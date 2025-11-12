package kuke.board.common.event.payload;

import kuke.board.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * '게시글 좋아요' 이벤트 발생 시 전달되는 데이터(페이로드)를 담는 클래스입니다.
 * EventPayload 인터페이스를 구현하여 이 클래스가 이벤트 데이터임을 명시합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLikedEventPayload implements EventPayload {
    private Long articleLikeId; // '게시글 좋아요' 정보의 고유 ID
    private Long articleId; // 좋아요를 받은 게시글의 ID
    private Long userId; // 좋아요를 누른 사용자의 ID
    private LocalDateTime createdAt; // 좋아요를 누른 시각
    private Long articleLikeCount; // 해당 게시글의 총 좋아요 수
}