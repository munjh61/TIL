package kuke.board.comment.controller;

import kuke.board.comment.service.CommentService;
import kuke.board.comment.service.CommentServiceV2;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글(Comment) 관련 HTTP 요청을 처리하는 REST 컨트롤러 (V2).
 * '구체화된 경로' 모델 기반의 CommentServiceV2를 사용합니다.
 */
@RestController
@RequiredArgsConstructor
public class CommentControllerV2 {
    private final CommentServiceV2 commentService;

    @GetMapping("/v2/comments/{commentId}")
    public CommentResponse read(
            @PathVariable("commentId") Long commentId
    ) {
        return commentService.read(commentId);
    }

    @PostMapping("/v2/comments")
    public CommentResponse create(@RequestBody CommentCreateRequestV2 request) {
        return commentService.create(request);
    }

    @DeleteMapping("/v2/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
    }

    @GetMapping("/v2/comments")
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAll(articleId, page, pageSize);
    }

    /**
     * 무한 스크롤 방식으로 댓글 목록을 조회합니다.
     * V2 모델 덕분에, 커서로 마지막 댓글의 'path' 문자열 하나만 사용하여 간단하게 페이징이 가능합니다.
     */
    @GetMapping("/v2/comments/infinite-scroll")
    public List<CommentResponse> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastPath", required = false) String lastPath,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAllInfiniteScroll(articleId, lastPath, pageSize);
    }

    /**
     * 특정 게시글의 총 댓글 수를 조회합니다.
     * 이 엔드포인트는 다른 서비스(예: article-read)가 데이터를 동기화할 때 사용됩니다.
     */
    @GetMapping("/v2/comments/articles/{articleId}/count")
    public Long count(
            @PathVariable("articleId") Long articleId
    ) {
        return commentService.count(articleId);
    }
}