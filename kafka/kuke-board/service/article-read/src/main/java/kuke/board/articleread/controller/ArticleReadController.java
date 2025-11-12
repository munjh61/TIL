package kuke.board.articleread.controller;

import kuke.board.articleread.service.ArticleReadService;
import kuke.board.articleread.service.response.ArticleReadPageResponse;
import kuke.board.articleread.service.response.ArticleReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 게시글 조회 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * CQRS 패턴의 조회(Query) 엔드포인트를 제공하며, 모든 데이터는 최적화된 읽기 모델(Read Model)로부터 제공됩니다.
 */
@RestController
@RequiredArgsConstructor
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    /**
     * 단일 게시글의 모든 정보를 조회합니다.
     * (게시글 내용, 댓글 수, 좋아요 수, 조회수 등)
     * @param articleId 조회할 게시글 ID
     * @return 집계된 게시글 정보
     */
    @GetMapping("/v1/articles/{articleId}")
    public ArticleReadResponse read(@PathVariable("articleId") Long articleId) {
        return articleReadService.read(articleId);
    }

    /**
     * 게시글 목록을 오프셋 기반 페이지네이션으로 조회합니다.
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 페이징된 게시글 목록
     */
    @GetMapping("/v1/articles")
    public ArticleReadPageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAll(boardId, page, pageSize);
    }

    /**
     * 게시글 목록을 무한 스크롤(커서 기반) 방식으로 조회합니다.
     * @param boardId 게시판 ID
     * @param lastArticleId 마지막으로 조회된 게시글 ID (커서)
     * @param pageSize 조회할 개수
     * @return 조회된 게시글 목록
     */
    @GetMapping("/v1/articles/infinite-scroll")
    public List<ArticleReadResponse> readAllInfiniteScroll(
            @RequestParam("boardId") Long boardId,
            @RequestParam(value = "lastArticleId", required = false) Long lastArticleId,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
    }

}