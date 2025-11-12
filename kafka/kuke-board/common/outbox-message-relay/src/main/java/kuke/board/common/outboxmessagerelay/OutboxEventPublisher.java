package kuke.board.common.outboxmessagerelay;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Outbox 이벤트를 생성하고 발행하는 역할을 담당하는 클래스입니다.
 * 이 클래스는 도메인 서비스에서 호출되어, 실제 이벤트 데이터를 Outbox에 저장하기 위한
 * 첫 단계를 시작합니다.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    // Outbox 메시지의 고유 ID를 생성하기 위한 Snowflake 인스턴스
    private final Snowflake outboxIdSnowflake = new Snowflake();
    // 이벤트 자체의 고유 ID를 생성하기 위한 Snowflake 인스턴스
    private final Snowflake eventIdSnowflake = new Snowflake();
    // Spring의 이벤트 발행 메커니즘을 사용하기 위한 ApplicationEventPublisher
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 주어진 이벤트 타입, 페이로드, 샤드 키를 사용하여 Outbox 이벤트를 발행합니다.
     * @param type 발행할 이벤트의 종류
     * @param payload 이벤트에 담길 실제 데이터
     * @param shardKey 메시지 릴레이 처리를 위한 샤드 키
     */
    public void publish(EventType type, EventPayload payload, Long shardKey) {
        // 1. Outbox 객체를 생성합니다.
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(), // Outbox 메시지 ID 생성
                type,
                // 2. 실제 Event 객체를 생성하고 JSON 문자열로 직렬화하여 payload로 저장합니다.
                Event.of(
                        eventIdSnowflake.nextId(), type, payload // 이벤트 ID 생성
                ).toJson(),
                // 3. 샤드 키를 샤드 개수로 나눈 나머지를 사용하여 메시지를 분산시킵니다.
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        // 4. 생성된 Outbox 객체를 담은 OutboxEvent를 발행합니다.
        // 이 이벤트는 @TransactionalEventListener에 의해 처리되어 데이터베이스에 저장됩니다.
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}