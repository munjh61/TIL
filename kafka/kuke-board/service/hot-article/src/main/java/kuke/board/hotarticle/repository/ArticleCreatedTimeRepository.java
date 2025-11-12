package kuke.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 게시글별 생성 시간을 Redis에 저장하고 관리하는 리포지토리입니다.
 * 인기 게시글 점수 계산 시 시간 감쇠(time-decay) 요소를 적용하는 데 사용됩니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleCreatedTimeRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "hot-article::article::12345::created-time"
    private static final String KEY_FORMAT = "hot-article::article::%s::created-time";

    /**
     * 특정 게시글의 생성 시간을 생성하거나 업데이트하고, 만료 시간을 설정합니다.
     * @param articleId 게시글 ID
     * @param createdAt 생성 시간
     * @param ttl 데이터 만료 시간
     */
    public void createOrUpdate(Long articleId, LocalDateTime createdAt, Duration ttl) {
        // LocalDateTime을 UTC 기준의 Unix timestamp (epoch milliseconds)로 변환하여 저장합니다.
        // 이는 다른 시스템이나 언어 간에 시간을 일관되게 다루기 위한 일반적인 방법입니다.
        redisTemplate.opsForValue().set(
                generateKey(articleId),
                String.valueOf(createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()),
                ttl
        );
    }

    /**
     * 특정 게시글의 생성 시간 정보를 삭제합니다.
     * @param articleId 삭제할 게시글 ID
     */
    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    /**
     * 특정 게시글의 생성 시간을 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 생성 시간. 데이터가 없으면 null을 반환합니다.
     */
    public LocalDateTime read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        if (result == null) {
            return null;
        }
        // 저장된 epoch milliseconds 문자열을 다시 LocalDateTime 객체로 변환합니다.
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.valueOf(result)), ZoneOffset.UTC
        );
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}