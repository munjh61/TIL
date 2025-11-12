package kuke.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 일별 인기 게시글 목록을 Redis에 저장하고 관리하는 리포지토리입니다.
 * Redis의 Sorted Set을 '리더보드(leaderboard)'처럼 사용하여 점수 순으로 게시글을 정렬합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 날짜별로 키를 생성합니다. 예: "hot-article::list::20241112"
    private static final String KEY_FORMAT = "hot-article::list::%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 인기 게시글 목록(Sorted Set)에 게시글을 추가하거나 점수를 업데이트합니다.
     * @param articleId 게시글 ID
     * @param time 기준 시간 (키 생성용)
     * @param score 인기 점수
     * @param limit 리더보드가 유지할 최대 게시글 수
     * @param ttl 리더보드 키의 만료 시간
     */
    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time);
            // 1. Sorted Set에 member(articleId)와 score(인기 점수)를 추가/업데이트합니다.
            conn.zAdd(key, score, String.valueOf(articleId));
            // 2. 리더보드의 크기를 'limit'으로 유지하기 위해 점수가 낮은 항목들을 제거합니다.
            //    (0부터 -limit-1까지 삭제하면 점수가 높은 상위 limit개의 항목만 남습니다.)
            conn.zRemRange(key, 0, -limit - 1);
            // 3. 키 자체에 만료 시간을 설정하여 오래된 데이터가 자동으로 삭제되도록 합니다.
            conn.expire(key, ttl.toSeconds());
            return null;
        });
    }

    /**
     * 인기 게시글 목록에서 특정 게시글을 제거합니다.
     * @param articleId 제거할 게시글 ID
     * @param time 기준 시간 (키 생성용)
     */
    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    /**
     * 특정 날짜의 인기 게시글 목록 전체를 점수 순으로 조회합니다.
     * @param dateStr 조회할 날짜 (yyyyMMdd 형식)
     * @return 점수 순으로 정렬된 게시글 ID 리스트
     */
    public List<Long> readAll(String dateStr) {
        return redisTemplate.opsForZSet()
                // ZREVRANGEWITHSCORES: 점수가 높은 순(역순)으로 멤버와 점수를 함께 조회합니다.
                .reverseRangeWithScores(generateKey(dateStr), 0, -1).stream()
                // peek을 사용하여 로깅 (디버깅 목적)
                .peek(tuple ->
                        log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore()))
                .map(ZSetOperations.TypedTuple::getValue) // articleId만 추출
                .map(Long::valueOf)
                .toList();
    }
}