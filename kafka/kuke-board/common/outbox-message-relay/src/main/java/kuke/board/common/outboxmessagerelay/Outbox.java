package kuke.board.common.outboxmessagerelay;

import jakarta.persistence.*;
import kuke.board.common.event.EventType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * '아웃박스(Outbox)' 패턴을 위한 JPA 엔티티 클래스입니다.
 * 발행해야 할 이벤트를 데이터베이스 테이블에 임시로 저장하는 역할을 합니다.
 * 이 테이블의 레코드는 별도의 릴레이 프로세스에 의해 Kafka와 같은 메시지 브로커로 전송됩니다.
 */
@Table(name = "outbox") // 'outbox'라는 이름의 데이터베이스 테이블과 매핑됩니다.
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 프레임워크를 위한 기본 생성자
public class Outbox {
    @Id
    private Long outboxId; // 아웃박스 메시지의 고유 ID

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장합니다.
    private EventType eventType; // 발생한 이벤트의 종류

    private String payload; // 이벤트 데이터 (주로 JSON 형태의 문자열)

    private Long shardKey; // 메시지 릴레이 처리를 위한 샤드 키

    private LocalDateTime createdAt; // 메시지 생성 시각

    /**
     * 새로운 Outbox 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param outboxId 메시지 ID
     * @param eventType 이벤트 타입
     * @param payload 이벤트 페이로드
     * @param shardKey 샤드 키
     * @return 생성된 Outbox 객체
     */
    public static Outbox create(Long outboxId, EventType eventType, String payload, Long shardKey) {
        Outbox outbox = new Outbox();
        outbox.outboxId = outboxId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.shardKey = shardKey;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}