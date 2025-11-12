package kuke.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 각 게시판별 게시글 ID 목록을 Redis에 저장하고 관리하는 리포지토리입니다.
 * Redis의 Sorted Set을 활용하여 효율적인 페이징 처리를 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "article-read::board::1::article-list"
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    /**
     * 게시판의 게시글 ID 목록에 새 ID를 추가하고, 목록의 최대 크기를 제한합니다.
     * @param boardId 게시판 ID
     * @param articleId 추가할 게시글 ID
     * @param limit 목록이 유지할 최대 ID 개수
     */
    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            // 1. ID를 0으로 채워진 문자열로 변환하여 Sorted Set에 추가합니다. 점수(score)는 모두 0으로 동일하게 설정합니다.
            conn.zAdd(key, 0, toPaddedString(articleId));
            // 2. Sorted Set의 크기를 limit으로 유지하기 위해 오래된 항목을 삭제합니다.
            //    (0부터 -limit-1까지 삭제하면 최신 limit개의 항목만 남게 됩니다.)
            conn.zRemRange(key, 0, -limit - 1);
            return null;
        });
    }

    /**
     * 게시판의 게시글 ID 목록에서 특정 ID를 삭제합니다.
     * @param boardId 게시판 ID
     * @param articleId 삭제할 게시글 ID
     */
    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    /**
     * 오프셋 기반 페이지네이션으로 게시글 ID 목록을 조회합니다.
     * @param boardId 게시판 ID
     * @param offset 조회 시작 위치
     * @param limit 조회할 개수
     * @return 게시글 ID 리스트
     */
    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return redisTemplate.opsForZSet()
                // ZREVRANGE를 사용하여 인덱스 기반으로 역순 조회
                .reverseRange(generateKey(boardId), offset, offset + limit - 1)
                .stream().map(Long::valueOf).toList();
    }

    /**
     * 커서 기반(무한 스크롤) 페이지네이션으로 게시글 ID 목록을 조회합니다.
     * @param boardId 게시판 ID
     * @param lastArticleId 마지막으로 조회된 게시글 ID (커서 역할)
     * @param limit 조회할 개수
     * @return 게시글 ID 리스트
     */
    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex(
                generateKey(boardId),
                lastArticleId == null ?
                        Range.unbounded() : // 첫 페이지인 경우, 범위 제한 없음
                        // 다음 페이지인 경우, lastArticleId보다 사전적으로 작은 값들을 조회
                        Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                Limit.limit().count(limit.intValue()) // 조회할 개수 제한
        ).stream().map(Long::valueOf).toList();
    }

    /**
     * Long 타입의 ID를 19자리의 0으로 채워진 문자열로 변환합니다.
     * 예: 1234 -> "0000000000000001234"
     * 이는 Redis Sorted Set에서 점수가 동일할 때 멤버(ID)들을 사전적(lexicographical)으로 올바르게
     * 정렬하기 위해 필수적입니다. (숫자 100이 20보다 뒤에 오도록 보장)
     * @param articleId 변환할 게시글 ID
     * @return 0으로 채워진 문자열 ID
     */
    private String toPaddedString(Long articleId) {
        return "%019d".formatted(articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}