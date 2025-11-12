package kuke.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis를 사용하여 간단한 분산 락(Distributed Lock)을 구현하는 클래스입니다.
 * 캐시 스탬피드를 방지하기 위해, 캐시를 갱신하는 동안 다른 스레드나 프로세스의 동시 접근을 막는 데 사용됩니다.
 */
@Component
@RequiredArgsConstructor
public class OptimizedCacheLockProvider {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "optimized-cache-lock::"; // 락 키에 사용할 접두사
    private static final Duration LOCK_TTL = Duration.ofSeconds(3); // 락의 만료 시간 (안전장치)

    /**
     * 분산 락을 획득합니다.
     * @param key 락을 걸 대상 키
     * @return 락 획득에 성공하면 true, 실패하면 false
     */
    public boolean lock(String key) {
        // Redis의 'SET key value NX' 명령을 사용하여 원자적으로 락을 시도합니다.
        // setIfAbsent는 키가 존재하지 않을 때만 값을 설정하고 true를 반환합니다.
        // 이미 키가 존재하면(다른 스레드가 락을 선점했으면) false를 반환합니다.
        // LOCK_TTL을 설정하여, 락을 획득한 스레드가 비정상 종료되어도 락이 자동으로 해제되도록 보장합니다.
        return redisTemplate.opsForValue().setIfAbsent(
                generateLockKey(key),
                "",
                LOCK_TTL
        );
    }

    /**
     * 획득했던 락을 해제합니다.
     * @param key 락을 해제할 대상 키
     */
    public void unlock(String key) {
        redisTemplate.delete(generateLockKey(key));
    }

    /**
     * 캐시 키로부터 락 키를 생성합니다.
     * @param key 원본 캐시 키
     * @return 락을 위한 Redis 키
     */
    private String generateLockKey(String key) {
        return KEY_PREFIX + key;
    }
}