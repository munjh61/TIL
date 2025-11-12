package kuke.board.article.service.response;

import jakarta.persistence.Id;
import kuke.board.article.entity.Article;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 게시글 정보를 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
@ToString
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * Article 엔티티 객체로부터 ArticleResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param article 변환할 Article 엔티티
     * @return 생성된 ArticleResponse 객체
     */
    public static ArticleResponse from(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.articleId = article.getArticleId();
        response.title = article.getTitle();
        response.content = article.getContent();
        response.boardId = article.getBoardId();
        response.writerId = article.getWriterId();
        response.createdAt = article.getCreatedAt();
        response.modifiedAt = article.getModifiedAt();
        return response;
    }
}