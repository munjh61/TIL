package kuke.board.articleread.repository;

import kuke.board.articleread.client.ArticleClient;
import kuke.board.common.event.payload.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * CQRS 패턴의 '읽기 모델(Read Model)'에 해당하는 클래스입니다.
 * 게시글 조회에 필요한 모든 데이터를 비정규화(denormalized)하여 가지고 있습니다.
 * 게시글 원본 데이터뿐만 아니라, 댓글 수, 좋아요 수 등 다른 서비스의 데이터도 포함하여
 * 조회 시 여러 서비스를 호출할 필요 없이 한 번에 완전한 정보를 제공할 수 있도록 최적화되었습니다.
 */
@Getter
public class ArticleQueryModel {
    // 게시글 기본 정보
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 다른 서비스로부터 비정규화된 데이터
    private Long articleCommentCount; // 댓글 수
    private Long articleLikeCount;    // 좋아요 수

    /**
     * '게시글 생성' 이벤트 페이로드로부터 ArticleQueryModel을 생성합니다.
     * 처음 생성 시점에는 댓글과 좋아요 수가 0입니다.
     * @param payload ArticleCreatedEventPayload
     * @return 생성된 ArticleQueryModel
     */
    public static ArticleQueryModel create(ArticleCreatedEventPayload payload) {
        ArticleQueryModel articleQueryModel = new ArticleQueryModel();
        articleQueryModel.articleId = payload.getArticleId();
        articleQueryModel.title = payload.getTitle();
        articleQueryModel.content = payload.getContent();
        articleQueryModel.boardId = payload.getBoardId();
        articleQueryModel.writerId = payload.getWriterId();
        articleQueryModel.createdAt = payload.getCreatedAt();
        articleQueryModel.modifiedAt = payload.getModifiedAt();
        articleQueryModel.articleCommentCount = 0L; // 초기값 0
        articleQueryModel.articleLikeCount = 0L;    // 초기값 0
        return articleQueryModel;
    }

    /**
     * 다른 서비스들을 직접 호출(client call)하여 얻은 정보로 ArticleQueryModel을 생성합니다.
     * 주로 캐시가 만료되어 데이터를 다시 채워야 할 때(cache warming/fallback) 사용됩니다.
     * @param article Article 서비스에서 가져온 게시글 정보
     * @param commentCount Comment 서비스에서 가져온 댓글 수
     * @param likeCount Like 서비스에서 가져온 좋아요 수
     * @return 생성된 ArticleQueryModel
     */
    public static ArticleQueryModel create(ArticleClient.ArticleResponse article, Long commentCount, Long likeCount) {
        ArticleQueryModel articleQueryModel = new ArticleQueryModel();
        articleQueryModel.articleId = article.getArticleId();
        articleQueryModel.title = article.getTitle();
        articleQueryModel.content = article.getContent();
        articleQueryModel.boardId = article.getBoardId();
        articleQueryModel.writerId = article.getWriterId();
        articleQueryModel.createdAt = article.getCreatedAt();
        articleQueryModel.modifiedAt = article.getModifiedAt();
        articleQueryModel.articleCommentCount = commentCount;
        articleQueryModel.articleLikeCount = likeCount;
        return articleQueryModel;
    }

    // 각 이벤트 페이로드에 따라 모델의 특정 필드를 업데이트하는 메서드들 (메서드 오버로딩)

    public void updateBy(CommentCreatedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(CommentDeletedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(ArticleLikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUnlikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUpdatedEventPayload payload) {
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.boardId = payload.getBoardId();
        this.writerId = payload.getWriterId();
        this.createdAt = payload.getCreatedAt();
        this.modifiedAt = payload.getModifiedAt();
    }
}