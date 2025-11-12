package kuke.board.common.event;

import kuke.board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시스템에서 발생하는 이벤트의 종류를 정의하는 열거형(enum)입니다.
 * 각 이벤트 타입은 연관된 페이로드 클래스와 Kafka 토픽을 가집니다.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    // 게시글 관련 이벤트
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE), // 게시글 생성
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE), // 게시글 수정
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.KUKE_BOARD_ARTICLE), // 게시글 삭제

    // 댓글 관련 이벤트
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.KUKE_BOARD_COMMENT), // 댓글 생성
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.KUKE_BOARD_COMMENT), // 댓글 삭제

    // 좋아요 관련 이벤트
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.KUKE_BOARD_LIKE),       // 게시글 좋아요
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.KUKE_BOARD_LIKE),   // 게시글 좋아요 취소

    // 조회수 관련 이벤트
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.KUKE_BOARD_VIEW)      // 게시글 조회
    ;

    private final Class<? extends EventPayload> payloadClass; // 이벤트 페이로드의 클래스 타입
    private final String topic; // 이벤트가 발행될 Kafka 토픽 이름

    /**
     * 문자열로부터 해당하는 EventType을 찾아 반환합니다.
     * @param type EventType의 이름과 일치하는 문자열
     * @return 해당하는 EventType. 찾지 못하면 null을 반환합니다.
     */
    public static EventType from(String type) {
        try {
            // 주어진 문자열을 기반으로 enum 상수를 찾습니다.
            return valueOf(type);
        } catch (Exception e) {
            // 해당하는 enum 상수가 없을 경우 오류 로그를 남깁니다.
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    /**
     * Kafka 토픽 이름을 상수로 정의한 내부 클래스입니다.
     */
    public static class Topic {
        public static final String KUKE_BOARD_ARTICLE = "kuke-board-article";
        public static final String KUKE_BOARD_COMMENT = "kuke-board-comment";
        public static final String KUKE_BOARD_LIKE = "kuke-board-like";
        public static final String KUKE_BOARD_VIEW = "kuke-board-view";
    }
}