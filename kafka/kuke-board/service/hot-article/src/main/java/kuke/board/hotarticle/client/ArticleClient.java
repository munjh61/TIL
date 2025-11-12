package kuke.board.hotarticle.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

/**
 * article-service와 통신하기 위한 REST 클라이언트입니다.
 * 인기 게시글 목록을 조회할 때, 각 게시글의 상세 정보를 얻기 위해 사용됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;

    // 참고: 이 클라이언트는 'article-service'를 호출하고 있습니다.
    // 최종 사용자에게 보여줄 집계 데이터(댓글 수, 좋아요 수 등)를 얻으려면
    // 'article-read-service'를 호출하는 것이 더 적합해 보입니다.
    // 현재 구현은 버그이거나 미완성 상태일 수 있습니다.
    @Value("${endpoints.kuke-board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct
    void initRestClient() {
        restClient = RestClient.create(articleServiceUrl);
    }

    /**
     * article-service에서 특정 게시글 정보를 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 조회된 게시글 정보. 실패 시 null을 반환합니다.
     */
    public ArticleResponse read(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
        } catch (Exception e) {
            log.error("[ArticleClient.read] articleId={}", articleId, e);
        }
        return null;
    }


    /**
     * API 응답을 매핑하기 위한 내부 DTO 클래스입니다.
     * 현재는 제목과 생성 시간만 사용하고 있습니다.
     */
    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }
}