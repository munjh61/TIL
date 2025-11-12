package kuke.board.articleread.repository;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * ArticleQueryModel 객체를 Redis에 저장하고 조회하는 리포지토리 클래스입니다.
 * CQRS 패턴의 읽기 모델(Read Model)을 위한 데이터 접근을 담당합니다.
 */
@Repository
@RequiredArgsConstructor
public class ArticleQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    // Redis 키의 형식을 정의합니다. 예: "article-read::article::12345"
    private static final String KEY_FORMAT = "article-read::article::%s";

    /**
     * ArticleQueryModel을 Redis에 생성하고, 만료 시간(TTL)을 설정합니다.
     * @param articleQueryModel 저장할 게시글 조회 모델
     * @param ttl 데이터 만료 시간
     */
    public void create(ArticleQueryModel articleQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel), ttl);
    }

    /**
     * Redis에 이미 존재하는 ArticleQueryModel을 업데이트합니다.
     * 키가 존재하지 않으면 아무 작업도 수행하지 않습니다. (setIfPresent)
     * @param articleQueryModel 업데이트할 게시글 조회 모델
     */
    public void update(ArticleQueryModel articleQueryModel) {
        redisTemplate.opsForValue().setIfPresent(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel));
    }

    /**
     * Redis에서 특정 게시글을 삭제합니다.
     * @param articleId 삭제할 게시글의 ID
     */
    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    /**
     * Redis에서 단일 게시글을 조회합니다.
     * @param articleId 조회할 게시글의 ID
     * @return 조회된 ArticleQueryModel을 담은 Optional 객체. 없으면 Optional.empty()
     */
    public Optional<ArticleQueryModel> read(Long articleId) {
        // Redis에서 JSON 문자열을 가져옵니다.
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(articleId))
        // JSON 문자열을 ArticleQueryModel 객체로 역직렬화합니다.
        ).map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class));
    }

    private String generateKey(ArticleQueryModel articleQueryModel) {
        return generateKey(articleQueryModel.getArticleId());
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }

    /**
     * 여러 개의 게시글을 ID 리스트를 이용해 한 번에 조회합니다. (Bulk Read)
     * Redis의 MGET 명령을 사용하여 효율적으로 데이터를 가져옵니다.
     * @param articleIds 조회할 게시글 ID 리스트
     * @return 조회된 ArticleQueryModel들을 담은 Map (Key: articleId, Value: ArticleQueryModel)
     */
    public Map<Long, ArticleQueryModel> readAll(List<Long> articleIds) {
        List<String> keyList = articleIds.stream().map(this::generateKey).toList();
        // multiGet으로 여러 키를 한 번에 조회합니다.
        return redisTemplate.opsForValue().multiGet(keyList).stream()
                .filter(Objects::nonNull) // 결과 중 null 값(키가 없는 경우)은 제외합니다.
                .map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class)) // 각 JSON을 객체로 역직렬화합니다.
                .collect(toMap(ArticleQueryModel::getArticleId, identity())); // articleId를 키로 하는 Map으로 변환합니다.
    }
}