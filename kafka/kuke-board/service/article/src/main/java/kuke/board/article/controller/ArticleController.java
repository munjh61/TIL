package kuke.board.article.controller;

import kuke.board.article.service.ArticleService;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글(Article) 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    /**
     * 특정 게시글을 조회합니다.
     * @param articleId 조회할 게시글의 ID
     * @return 게시글 정보
     */
    @GetMapping("/v1/articles/{articleId}")
    public ArticleResponse read(@PathVariable Long articleId) {
        return articleService.read(articleId);
    }

    /**
     * 특정 게시판의 게시글 목록을 오프셋 기반 페이지네이션으로 조회합니다.
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 당 게시글 수
     * @return 페이징된 게시글 목록
     */
    @GetMapping("/v1/articles")
    public ArticlePageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleService.readAll(boardId, page, pageSize);
    }

    /**
     * 특정 게시판의 게시글 목록을 무한 스크롤 방식으로 조회합니다.
     * @param boardId 게시판 ID
     * @param pageSize 조회할 개수
     * @param lastArticleId 마지막으로 조회된 게시글의 ID (첫 페이지의 경우 생략 가능)
     * @return 조회된 게시글 목록
     */
    @GetMapping("/v1/articles/infinite-scroll")
    public List<ArticleResponse> readAllInfiniteScroll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "lastArticleId", required = false) Long lastArticleId
    ) {
        return articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId);
    }

    /**
     * 새로운 게시글을 생성합니다.
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글 정보
     */
    @PostMapping("/v1/articles")
    public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
        return articleService.create(request);
    }

    /**
     * 기존 게시글을 수정합니다.
     * @param articleId 수정할 게시글의 ID
     * @param request 게시글 수정 요청 정보
     * @return 수정된 게시글 정보
     */
    @PutMapping("/v1/articles/{articleId}")
    public ArticleResponse update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request) {
        return articleService.update(articleId, request);
    }

    /**
     * 특정 게시글을 삭제합니다.
     * @param articleId 삭제할 게시글의 ID
     */
    @DeleteMapping("/v1/articles/{articleId}")
    public void delete(@PathVariable Long articleId) {
        articleService.delete(articleId);
    }

    /**
     * 특정 게시판의 총 게시글 수를 조회합니다.
     * @param boardId 게시판 ID
     * @return 총 게시글 수
     */
    @GetMapping("/v1/articles/boards/{boardId}/count")
    public Long count(@PathVariable Long boardId) {
        return articleService.count(boardId);
    }
}