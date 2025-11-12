package kuke.board.hotarticle.consumer;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.hotarticle.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 인기 게시글 점수에 영향을 주는 모든 이벤트를 수신하는 Kafka 컨슈머입니다.
 * 게시글, 댓글, 좋아요, 조회수 관련 토픽을 모두 구독합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    /**
     * 지정된 Kafka 토픽들로부터 메시지를 리스닝합니다.
     * @param message 수신한 메시지 (JSON 형식의 문자열)
     * @param ack 수동으로 메시지 처리를 확인하기 위한 Acknowledgment 객체
     */
    @KafkaListener(topics = {
            EventType.Topic.KUKE_BOARD_ARTICLE,
            EventType.Topic.KUKE_BOARD_COMMENT,
            EventType.Topic.KUKE_BOARD_LIKE,
            EventType.Topic.KUKE_BOARD_VIEW
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer.listen] received message={}", message);
        // 1. JSON 메시지를 Event 객체로 역직렬화합니다.
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            // 2. 서비스 레이어에 이벤트 처리를 위임하여 인기 게시글 점수를 업데이트합니다.
            hotArticleService.handleEvent(event);
        }
        // 3. 메시지 처리가 성공적으로 완료되었음을 Kafka에 알립니다 (offset commit).
        ack.acknowledge();
    }
}