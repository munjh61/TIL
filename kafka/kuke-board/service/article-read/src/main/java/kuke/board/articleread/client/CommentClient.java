package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * comment-service와 통신하기 위한 REST 클라이언트입니다.
 * 특정 게시글의 댓글 수를 조회하는 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentClient {
    private RestClient restClient;
    @Value("${endpoints.kuke-board-comment-service.url}")
    private String commentServiceUrl; // application.yml에서 주입받는 comment-service의 URL

    /**
     * 빈(Bean)이 생성된 후 RestClient 인스턴스를 초기화합니다.
     */
    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(commentServiceUrl);
    }

    /**
     * comment-service에서 특정 게시글의 총 댓글 수를 조회합니다.
     * @param articleId 조회할 게시글의 ID
     * @return 총 댓글 수. 실패 시 0을 반환합니다.
     */
    public long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v2/comments/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            // API 호출 실패 시 에러 로그를 남기고 0을 반환하여 장애 전파를 막습니다.
            log.error("[CommentClient.count] articleId={}", articleId, e);
            return 0;
        }
    }

}