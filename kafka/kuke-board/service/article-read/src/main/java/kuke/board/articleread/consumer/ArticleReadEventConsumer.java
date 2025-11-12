package kuke.board.articleread.consumer;

import kuke.board.articleread.service.ArticleReadService;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka로부터 이벤트를 수신(consume)하는 컨슈머 클래스입니다.
 * 게시글, 댓글, 좋아요 관련 토픽을 구독합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    /**
     * 지정된 Kafka 토픽들로부터 메시지를 리스닝합니다.
     * @param message 수신한 메시지 (JSON 형식의 문자열)
     * @param ack 수동으로 메시지 처리를 확인하기 위한 Acknowledgment 객체
     */
    @KafkaListener(topics = {
            EventType.Topic.KUKE_BOARD_ARTICLE, // 게시글 관련 이벤트 토픽
            EventType.Topic.KUKE_BOARD_COMMENT, // 댓글 관련 이벤트 토픽
            EventType.Topic.KUKE_BOARD_LIKE     // 좋아요 관련 이벤트 토픽
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[ArticleReadEventConsumer.listen] message={}", message);
        // 1. JSON 문자열 메시지를 Event 객체로 역직렬화합니다.
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            // 2. 서비스 레이어에 이벤트 처리를 위임합니다.
            articleReadService.handleEvent(event);
        }
        // 3. 메시지 처리가 성공적으로 완료되었음을 Kafka에 알립니다 (offset commit).
        // 만약 handleEvent에서 예외가 발생하면 이 라인은 실행되지 않고, 메시지는 재처리됩니다.
        ack.acknowledge();
    }
}