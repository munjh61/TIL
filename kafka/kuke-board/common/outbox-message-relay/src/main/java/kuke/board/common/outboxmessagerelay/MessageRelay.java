package kuke.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Outbox 메시지를 처리하고 Kafka로 전달(relay)하는 핵심 컴포넌트입니다.
 * 트랜잭셔널 아웃박스 패턴의 구현체입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    /**
     * OutboxEvent를 리스닝하여 Outbox 메시지를 데이터베이스에 저장합니다.
     * 이 메서드는 주 트랜잭션이 커밋되기 직전(BEFORE_COMMIT)에 실행되어,
     * 도메인 로직과 Outbox 메시지 저장을 같은 트랜잭션으로 묶습니다.
     * @param outboxEvent 발행된 OutboxEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    /**
     * 주 트랜잭션이 성공적으로 커밋된 후(AFTER_COMMIT) OutboxEvent를 비동기적으로 처리합니다.
     * 이는 'happy path' 최적화로, 서비스가 정상일 때 메시지를 즉시 발행하기 위함입니다.
     * @param outboxEvent 발행된 OutboxEvent
     */
    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    /**
     * Outbox 메시지를 Kafka로 발행하고, 성공 시 데이터베이스에서 해당 메시지를 삭제합니다.
     * @param outbox 발행할 Outbox 메시지
     */
    private void publishEvent(Outbox outbox) {
        try {
            // Kafka 템플릿을 사용하여 메시지를 해당 토픽으로 전송합니다.
            // shardKey를 Kafka 메시지 키로 사용하여 파티셔닝을 보장합니다.
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS); // 1초 타임아웃으로 동기적으로 결과를 기다립니다.
            // 발행에 성공하면 데이터베이스에서 메시지를 삭제합니다.
            outboxRepository.delete(outbox);
        } catch (Exception e) {
            // 발행 실패 시 오류 로그를 남깁니다. 메시지는 DB에 남아있어 다음 스케줄링에서 재시도됩니다.
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    /**
     * 주기적으로 실행되어 데이터베이스에 남아있는(pending) Outbox 메시지를 발행합니다.
     * AFTER_COMMIT 단계에서 발행이 실패했거나, 서버 장애가 있었을 경우를 대비한 보상 로직입니다.
     */
    @Scheduled(
            fixedDelay = 10, // 10초 간격으로 실행
            initialDelay = 5, // 애플리케이션 시작 후 5초 뒤에 첫 실행
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent() {
        // 1. 코디네이터를 통해 현재 인스턴스가 처리할 샤드를 할당받습니다.
        AssignedShard assignedShard = messageRelayCoordinator.assignShards();
        log.info("[MessageRelay.publishPendingEvent] assignedShard size={}", assignedShard.getShards().size());
        // 2. 할당받은 각 샤드에 대해 처리되지 않은 메시지를 조회합니다.
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10), // 10초 이상 지난 메시지만 조회
                    Pageable.ofSize(100) // 최대 100개씩
            );
            // 3. 조회된 메시지들을 하나씩 발행합니다.
            for (Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
    }
}