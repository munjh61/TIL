package kuke.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 게시글별 실시간 조회수를 Redis에 저장하고 관리하는 리포지토리입니다.
 * Redis의 원자적(atomic) 연산을 활용하여 높은 동시성 환경에서 조회수를 효율적으로 증가시킵니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "view::article::12345::view_count"
    private static final String KEY_FORMAT = "view::article::%s::view_count";

    /**
     * 특정 게시글의 현재 조회수를 Redis에서 조회합니다.
     * @param articleId 게시글 ID
     * @return 현재 조회수. 키가 없으면 0L을 반환합니다.
     */
    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.valueOf(result);
    }

    /**
     * 특정 게시글의 조회수를 1 증가시킵니다.
     * Redis의 `INCREMENT` 명령을 사용하여 원자적으로 값을 증가시킵니다.
     * @param articleId 게시글 ID
     * @return 증가된 후의 새로운 조회수
     */
    public Long increase(Long articleId) {
        return redisTemplate.opsForValue().increment(generateKey(articleId));
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}