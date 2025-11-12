package kuke.board.common.outboxmessagerelay;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 메시지 릴레이 시스템에서 사용되는 상수들을 정의하는 클래스입니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 유틸리티 클래스이므로 인스턴스화 방지
public final class MessageRelayConstants {
    /**
     * 전체 샤드(shard)의 개수입니다.
     * Outbox 메시지는 이 개수만큼의 그룹으로 나뉘어 처리됩니다.
     * 이 값은 메시지 릴레이를 처리하는 인스턴스들에 의해 분배됩니다.
     * 현재는 임의의 값으로 4가 설정되어 있습니다.
     */
    public static final int SHARD_COUNT = 4;
}