package kuke.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis를 사용하여 게시글 조회수 증가에 대한 분산 락(Distributed Lock)을 관리하는 리포지토리입니다.
 * 특정 사용자가 특정 게시글의 조회수를 단기간 내에 여러 번 증가시키는 것을 방지합니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleViewDistributedLockRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "view::article::12345::user::67890::lock"
    // 이 키는 특정 게시글에 대한 특정 사용자의 조회수 증가 락을 나타냅니다.
    private static final String KEY_FORMAT = "view::article::%s::user::%s::lock";

    /**
     * 특정 게시글에 대한 특정 사용자의 조회수 증가 락을 획득합니다.
     * Redis의 `SETNX` (Set if Not Exists) 명령을 사용하여 원자적으로 동작합니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @param ttl 락의 만료 시간 (Duration)
     * @return 락 획득에 성공하면 true, 실패하면 false
     */
    public boolean lock(Long articleId, Long userId, Duration ttl) {
        String key = generateKey(articleId, userId);
        // setIfAbsent는 키가 존재하지 않을 때만 값을 설정하고 true를 반환합니다.
        // 이미 키가 존재하면(다른 요청이 락을 선점했으면) false를 반환합니다.
        // TTL을 설정하여 락이 자동으로 해제되도록 보장합니다.
        return redisTemplate.opsForValue().setIfAbsent(key, "", ttl);
    }

    /**
     * 락 키를 생성합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @return 생성된 락 키 문자열
     */
    private String generateKey(Long articleId, Long userId) {
        return KEY_FORMAT.formatted(articleId, userId);
    }
}