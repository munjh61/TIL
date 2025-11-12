package kuke.board.hotarticle.service;

import kuke.board.hotarticle.repository.ArticleCommentCountRepository;
import kuke.board.hotarticle.repository.ArticleLikeCountRepository;
import kuke.board.hotarticle.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 게시글의 '인기 점수(Hot Score)'를 계산하는 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class HotArticleScoreCalculator {
    private final ArticleLikeCountRepository articleLikeCountRepository;
    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;

    // 각 지표별 가중치. 좋아요 > 댓글 > 조회수 순으로 중요도를 부여합니다.
    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    private static final long ARTICLE_COMMENT_COUNT_WEIGHT = 2;
    private static final long ARTICLE_VIEW_COUNT_WEIGHT = 1;

    /**
     * 특정 게시글의 인기 점수를 계산합니다.
     * 점수는 (좋아요 수 * 가중치) + (조회수 * 가중치) + (댓글 수 * 가중치)의 합으로 결정됩니다.
     * @param articleId 점수를 계산할 게시글의 ID
     * @return 계산된 인기 점수
     */
    public long calculate(Long articleId) {
        // 1. Redis에 저장된 각 지표(좋아요, 조회수, 댓글 수)의 현재 값을 읽어옵니다.
        Long articleLikeCount = articleLikeCountRepository.read(articleId);
        Long articleViewCount = articleViewCountRepository.read(articleId);
        Long articleCommentCount = articleCommentCountRepository.read(articleId);

        // 2. 각 값에 가중치를 곱하여 합산합니다.
        return articleLikeCount * ARTICLE_LIKE_COUNT_WEIGHT
                + articleViewCount * ARTICLE_VIEW_COUNT_WEIGHT
                + articleCommentCount * ARTICLE_COMMENT_COUNT_WEIGHT;
    }
}