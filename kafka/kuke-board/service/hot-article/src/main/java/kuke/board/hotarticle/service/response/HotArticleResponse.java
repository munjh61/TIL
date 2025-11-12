package kuke.board.hotarticle.service.response;

import kuke.board.hotarticle.client.ArticleClient;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 인기 게시글 정보를 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
@ToString
public class HotArticleResponse {
    private Long articleId;
    private String title;
    private LocalDateTime createdAt;

    /**
     * ArticleClient의 응답 DTO로부터 HotArticleResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     *
     * 참고: 현재 이 DTO는 게시글의 기본 정보만 포함하고 있으며,
     * 사용자가 기대할 수 있는 집계 데이터(조회수, 좋아요 수, 댓글 수 등)는 빠져있습니다.
     *
     * @param articleResponse ArticleClient로부터 받은 응답 객체
     * @return 생성된 HotArticleResponse 객체
     */
    public static HotArticleResponse from(ArticleClient.ArticleResponse articleResponse) {
        HotArticleResponse response = new HotArticleResponse();
        response.articleId = articleResponse.getArticleId();
        response.title = articleResponse.getTitle();
        response.createdAt = articleResponse.getCreatedAt();
        return response;
    }
}