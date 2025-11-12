package kuke.board.common.outboxmessagerelay;

import lombok.Getter;
import lombok.ToString;

/**
 * Outbox 엔티티를 감싸는 이벤트 클래스입니다.
 * 이 클래스는 Spring의 ApplicationEventPublisher를 통해 발행되어,
 * Outbox 메시지를 데이터베이스에 저장하는 로직을 수행하는 리스너를 트리거하는 데 사용됩니다.
 */
@Getter
@ToString
public class OutboxEvent {
    private Outbox outbox; // 이벤트에 포함될 Outbox 데이터

    /**
     * Outbox 객체를 포함하는 새로운 OutboxEvent를 생성하는 정적 팩토리 메서드입니다.
     * @param outbox 이벤트로 발행할 Outbox 객체
     * @return 생성된 OutboxEvent 객체
     */
    public static OutboxEvent of(Outbox outbox) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.outbox = outbox;
        return outboxEvent;
    }
}