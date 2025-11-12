package kuke.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 게시글별 좋아요 수를 Redis에 저장하고 관리하는 리포지토리입니다.
 * 인기 게시글 점수 계산의 한 요소로 사용됩니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleLikeCountRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "hot-article::article::12345::like-count"
    private static final String KEY_FORMAT = "hot-article::article::%s::like-count";

    /**
     * 특정 게시글의 좋아요 수를 생성하거나 업데이트하고, 만료 시간을 설정합니다.
     * @param articleId 게시글 ID
     * @param likeCount 새로운 좋아요 수
     * @param ttl 데이터 만료 시간
     */
    public void createOrUpdate(Long articleId, Long likeCount, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(articleId), String.valueOf(likeCount), ttl);
    }

    /**
     * 특정 게시글의 좋아요 수를 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 좋아요 수. 데이터가 없으면 0L을 반환합니다.
     */
    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}