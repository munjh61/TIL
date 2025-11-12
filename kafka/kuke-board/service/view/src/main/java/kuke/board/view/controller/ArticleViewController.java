package kuke.board.view.controller;

import kuke.board.view.service.ArticleViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시글 조회수 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class ArticleViewController {
    private final ArticleViewService articleViewService;

    /**
     * 특정 게시글의 조회수를 증가시킵니다.
     * 사용자별 분산 락을 통해 일정 시간 내 중복 조회를 방지합니다.
     * @param articleId 조회수를 증가시킬 게시글 ID
     * @param userId 조회수를 증가시키는 사용자 ID
     * @return 증가된 후의 현재 조회수
     */
    @PostMapping("/v1/article-views/articles/{articleId}/users/{userId}")
    public Long increase(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return articleViewService.increase(articleId, userId);
    }

    /**
     * 특정 게시글의 현재 조회수를 조회합니다.
     * 이 엔드포인트는 다른 서비스(예: article-read)가 데이터를 동기화할 때 사용됩니다.
     * @param articleId 조회할 게시글 ID
     * @return 현재 조회수
     */
    @GetMapping("/v1/article-views/articles/{articleId}/count")
    public Long count(@PathVariable("articleId") Long articleId) {
        return articleViewService.count(articleId);
    }
}