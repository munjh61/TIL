package kuke.board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 분산 환경에서 여러 애플리케이션 인스턴스 간의 메시지 릴레이 작업을 조율하는 코디네이터입니다.
 * Redis를 사용하여 현재 실행 중인 인스턴스 목록을 유지하고, 각 인스턴스에 처리할 샤드를 할당합니다.
 */
@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    // 각 애플리케이션 인스턴스를 식별하기 위한 고유 ID
    private final String APP_ID = UUID.randomUUID().toString();

    private final int PING_INTERVAL_SECONDS = 3; // Redis에 heartbeat(ping)을 보내는 주기 (초)
    private final int PING_FAILURE_THRESHOLD = 3; // ping 실패로 간주하기까지의 임계 횟수

    /**
     * 현재 인스턴스에 할당된 샤드 정보를 반환합니다.
     * @return 할당된 샤드 정보를 담은 AssignedShard 객체
     */
    public AssignedShard assignShards() {
        // Redis에서 현재 활성화된 모든 앱 ID를 찾아, 이를 기반으로 샤드를 할당합니다.
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    /**
     * Redis에서 활성화된 모든 애플리케이션 인스턴스의 ID 목록을 조회합니다.
     * @return 정렬된 앱 ID 리스트
     */
    private List<String> findAppIds() {
        // Redis Sorted Set에서 모든 멤버를 점수(시간)의 내림차순으로 가져온 후, ID 기준 오름차순으로 정렬합니다.
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1).stream()
                .sorted()
                .toList();
    }

    /**
     * 주기적으로 Redis에 'ping'을 보내 현재 인스턴스가 활성 상태임을 알립니다 (heartbeat).
     * 또한 오랫동안 응답이 없는(죽은 것으로 간주되는) 인스턴스를 목록에서 제거합니다.
     */
    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            // 1. 현재 인스턴스의 APP_ID를 현재 시간(epoch milliseconds)을 점수로 하여 Sorted Set에 추가/업데이트합니다.
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            // 2. 일정 시간(ping 주기 * 임계 횟수) 이상 응답이 없었던 오래된 인스턴스들을 제거합니다.
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    Instant.now().minusSeconds((long) PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    /**
     * 애플리케이션이 종료될 때 호출되어, Redis의 활성 인스턴스 목록에서 자신을 제거합니다.
     */
    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    /**
     * Redis 키를 생성합니다. 키는 애플리케이션 이름을 포함하여 다른 서비스와 충돌하지 않도록 합니다.
     * @return 생성된 Redis 키
     */
    private String generateKey() {
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}