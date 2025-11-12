package kuke.board.like.service.response;

import jakarta.persistence.Id;
import kuke.board.like.entity.ArticleLike;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 게시글 좋아요 정보를 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
@ToString
public class ArticleLikeResponse {
    private Long articleLikeId; // 좋아요 정보의 고유 ID
    private Long articleId; // 좋아요를 받은 게시글의 ID
    private Long userId; // 좋아요를 누른 사용자의 ID
    private LocalDateTime createdAt; // 좋아요가 생성된 시각

    /**
     * ArticleLike 엔티티로부터 ArticleLikeResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param articleLike 변환할 ArticleLike 엔티티
     * @return 생성된 ArticleLikeResponse 객체
     */
    public static ArticleLikeResponse from(ArticleLike articleLike) {
        ArticleLikeResponse response = new ArticleLikeResponse();
        response.articleLikeId = articleLike.getArticleLikeId();
        response.articleId = articleLike.getArticleId();
        response.userId = articleLike.getUserId();
        response.createdAt = articleLike.getCreatedAt();
        return response;
    }
}