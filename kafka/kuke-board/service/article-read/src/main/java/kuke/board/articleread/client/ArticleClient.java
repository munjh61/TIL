package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * article-service와 통신하기 위한 REST 클라이언트입니다.
 * 주로 캐시 미스(cache miss)가 발생했을 때 원본 데이터를 조회하는 데 사용됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;
    @Value("${endpoints.kuke-board-article-service.url}")
    private String articleServiceUrl; // application.yml에서 주입받는 article-service의 URL

    /**
     * 빈(Bean)이 생성된 후 RestClient 인스턴스를 초기화합니다.
     */
    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(articleServiceUrl);
    }

    /**
     * article-service에서 특정 게시글 정보를 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 조회된 게시글 정보를 담은 Optional 객체. 실패 시 Optional.empty()
     */
    public Optional<ArticleResponse> read(Long articleId) {
        try {
            ArticleResponse articleResponse = restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
            return Optional.ofNullable(articleResponse);
        } catch (Exception e) {
            // API 호출 실패 시 에러 로그를 남기고 비어있는 Optional을 반환하여 장애 전파를 막습니다.
            log.error("[ArticleClient.read] articleId={}", articleId, e);
            return Optional.empty();
        }
    }

    /**
     * article-service에서 게시글 목록을 오프셋 기반으로 페이징하여 조회합니다.
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 페이징된 게시글 정보. 실패 시 비어있는 객체 반환.
     */
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        try {
            return restClient.get()
                    .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(boardId, page, pageSize))
                    .retrieve()
                    .body(ArticlePageResponse.class);
        } catch (Exception e) {
            log.error("[ArticleClient.readAll] boardId={}, page={}, pageSize={}", boardId, page, pageSize, e);
            return ArticlePageResponse.EMPTY;
        }
    }

    /**
     * article-service에서 게시글 목록을 무한 스크롤 방식으로 조회합니다.
     * @param boardId 게시판 ID
     * @param lastArticleId 마지막으로 조회된 게시글 ID
     * @param pageSize 페이지 크기
     * @return 게시글 목록. 실패 시 비어있는 리스트 반환.
     */
    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        try {
            return restClient.get()
                    .uri(
                            lastArticleId != null ?
                                    "/v1/articles/infinite-scroll?boardId=%s&lastArticleId=%s&pageSize=%s"
                                            .formatted(boardId, lastArticleId, pageSize) :
                                    "/v1/articles/infinite-scroll?boardId=%s&pageSize=%s"
                                            .formatted(boardId, pageSize)
                    )
                    .retrieve()
                    // 제네릭 타입(List<...>)의 응답을 받기 위해 ParameterizedTypeReference를 사용합니다.
                    .body(new ParameterizedTypeReference<List<ArticleResponse>>() {});
        } catch (Exception e) {
            log.error("[ArticleClient.readAllInfiniteScroll] boardId={}, lastArticleId={}, pageSize={}",
                    boardId, lastArticleId, pageSize, e);
            return List.of();
        }
    }

    /**
     * article-service에서 특정 게시판의 총 게시글 수를 조회합니다.
     * @param boardId 게시판 ID
     * @return 총 게시글 수. 실패 시 0.
     */
    public long count(Long boardId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/boards/{boardId}/count", boardId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ArticleClient.count] boardId={}", boardId, e);
            return 0;
        }
    }

    // API 응답을 매핑하기 위한 내부 DTO 클래스들

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlePageResponse {
        private List<ArticleResponse> articles;
        private Long articleCount;

        public static ArticlePageResponse EMPTY = new ArticlePageResponse(List.of(), 0L);
    }

    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}