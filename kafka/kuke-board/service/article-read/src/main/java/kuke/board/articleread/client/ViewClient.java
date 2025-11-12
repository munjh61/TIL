package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import kuke.board.articleread.cache.OptimizedCacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * view-service와 통신하여 게시글 조회수를 가져오는 REST 클라이언트입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewClient {
    private RestClient restClient;
    @Value("${endpoints.kuke-board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

    /**
     * 특정 게시글의 조회수를 조회합니다.
     * 이 메서드의 결과는 커스텀 캐싱 어노테이션인 @OptimizedCacheable에 의해 캐시됩니다.
     *
     * @OptimizedCacheable:
     * - type: 캐시의 종류(이름)를 지정합니다.
     * - ttlSeconds: 캐시의 만료 시간(Time-To-Live)을 초 단위로 설정합니다.
     *
     * 이 설정은 view-service에 대한 반복적인 API 호출을 줄여 성능을 향상시킵니다.
     * 예를 들어, 1초 이내에 동일한 articleId에 대한 요청이 여러 번 들어오면,
     * 첫 번째 요청만 실제 API를 호출하고 나머지는 캐시된 값을 반환합니다.
     *
     * @param articleId 조회할 게시글의 ID
     * @return 총 조회수. 실패 시 0을 반환합니다.
     */
//    @Cacheable(key = "#articleId", value = "articleViewCount")
    @OptimizedCacheable(type = "articleViewCount", ttlSeconds = 1)
    public long count(Long articleId) {
        // 이 로그는 캐시 미스(cache miss)가 발생하여 실제 API가 호출될 때만 출력됩니다.
        log.info("[ViewClient.count] articleId={}", articleId);
        try {
            return restClient.get()
                    .uri("/v1/article-views/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ViewClient.count] articleId={}", articleId, e);
            return 0;
        }
    }

}