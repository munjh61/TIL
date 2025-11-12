package kuke.board.like.controller;

import kuke.board.like.service.ArticleLikeService;
import kuke.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 좋아요 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * 다양한 동시성 제어 전략을 시연하기 위한 여러 엔드포인트를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
public class ArticleLikeController {
    private final ArticleLikeService articleLikeService;

    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 여부를 조회합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @return 좋아요 정보
     */
    @GetMapping("/v1/article-likes/articles/{articleId}/users/{userId}")
    public ArticleLikeResponse read(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return articleLikeService.read(articleId, userId);
    }

    /**
     * 특정 게시글의 총 좋아요 수를 조회합니다.
     * 이 엔드포인트는 다른 서비스(예: article-read)가 데이터를 동기화할 때 사용됩니다.
     * @param articleId 게시글 ID
     * @return 총 좋아요 수
     */
    @GetMapping("/v1/article-likes/articles/{articleId}/count")
    public Long count(
            @PathVariable("articleId") Long articleId
    ) {
        return articleLikeService.count(articleId);
    }

    /**
     * [동시성 제어 전략 1: 네이티브 쿼리 (UPDATE) 사용]
     * 게시글에 좋아요를 추가합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void likePessimisticLock1(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likePessimisticLock1(articleId, userId);
    }

    /**
     * [동시성 제어 전략 1: 네이티브 쿼리 (UPDATE) 사용]
     * 게시글의 좋아요를 취소합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void unlikePessimisticLock1(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikePessimisticLock1(articleId, userId);
    }

    /**
     * [동시성 제어 전략 2: 비관적 잠금 (PESSIMISTIC_WRITE) 사용]
     * 게시글에 좋아요를 추가합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void likePessimisticLock2(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likePessimisticLock2(articleId, userId);
    }

    /**
     * [동시성 제어 전략 2: 비관적 잠금 (PESSIMISTIC_WRITE) 사용]
     * 게시글의 좋아요를 취소합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void unlikePessimisticLock2(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikePessimisticLock2(articleId, userId);
    }

    /**
     * [동시성 제어 전략 3: 낙관적 잠금 (Optimistic Locking) 사용]
     * 게시글에 좋아요를 추가합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void likeOptimisticLock(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.likeOptimisticLock(articleId, userId);
    }

    /**
     * [동시성 제어 전략 3: 낙관적 잠금 (Optimistic Locking) 사용]
     * 게시글의 좋아요를 취소합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void unlikeOptimisticLock(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleLikeService.unlikeOptimisticLock(articleId, userId);
    }

}