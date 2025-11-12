package kuke.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 각 게시판별 총 게시글 수를 Redis에 저장하고 관리하는 리포지토리입니다.
 * 비정규화된 카운터 캐시(counter cache) 역할을 합니다.
 */
@Repository
@RequiredArgsConstructor
public class BoardArticleCountRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키 형식 정의. 예: "article-read::board-article-count::board::1"
    private static final String KEY_FORMAT = "article-read::board-article-count::board::%s";

    /**
     * 특정 게시판의 게시글 수를 생성하거나 업데이트합니다.
     * @param boardId 게시판 ID
     * @param articleCount 새로운 게시글 수
     */
    public void createOrUpdate(Long boardId, Long articleCount) {
        redisTemplate.opsForValue().set(generateKey(boardId), String.valueOf(articleCount));
    }

    /**
     * 특정 게시판의 게시글 수를 조회합니다.
     * @param boardId 조회할 게시판 ID
     * @return 게시글 수. 키가 없으면 0L을 반환합니다.
     */
    public Long read(Long boardId) {
        String result = redisTemplate.opsForValue().get(generateKey(boardId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}