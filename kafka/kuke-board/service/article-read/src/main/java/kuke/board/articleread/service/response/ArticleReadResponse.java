package kuke.board.articleread.service.response;

import kuke.board.articleread.repository.ArticleQueryModel;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 최종적으로 클라이언트에게 반환되는, 완전한 게시글 정보를 담은 응답 DTO입니다.
 * 여러 서비스의 데이터를 조합하여 만들어진 비정규화된 객체입니다.
 */
@Getter
@ToString
public class ArticleReadResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount; // 댓글 수
    private Long articleLikeCount;    // 좋아요 수
    private Long articleViewCount;    // 조회수

    /**
     * ArticleQueryModel과 조회수(viewCount)를 조합하여 최종 응답 DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param articleQueryModel 비정규화된 게시글 읽기 모델
     * @param viewCount 조회수
     * @return 생성된 ArticleReadResponse 객체
     */
    public static ArticleReadResponse from(ArticleQueryModel articleQueryModel, Long viewCount) {
        ArticleReadResponse response = new ArticleReadResponse();
        response.articleId = articleQueryModel.getArticleId();
        response.title = articleQueryModel.getTitle();
        response.content = articleQueryModel.getContent();
        response.boardId = articleQueryModel.getBoardId();
        response.writerId = articleQueryModel.getWriterId();
        response.createdAt = articleQueryModel.getCreatedAt();
        response.modifiedAt = articleQueryModel.getModifiedAt();
        response.articleCommentCount = articleQueryModel.getArticleCommentCount();
        response.articleLikeCount = articleQueryModel.getArticleLikeCount();
        response.articleViewCount = viewCount;
        return response;
    }
}