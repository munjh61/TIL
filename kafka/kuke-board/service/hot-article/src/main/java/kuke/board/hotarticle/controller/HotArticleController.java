package kuke.board.hotarticle.controller;

import kuke.board.hotarticle.service.HotArticleService;
import kuke.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 인기 게시글 조회 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class HotArticleController {
    private final HotArticleService hotArticleService;

    /**
     * 특정 날짜의 인기 게시글 목록을 조회합니다.
     * @param dateStr 조회할 날짜 (yyyyMMdd 형식의 문자열)
     * @return 점수 순으로 정렬된 인기 게시글 목록
     */
    @GetMapping("/v1/hot-articles/articles/date/{dateStr}")
    public List<HotArticleResponse> readAll(
            @PathVariable("dateStr") String dateStr
    ) {
        return hotArticleService.readAll(dateStr);
    }
}