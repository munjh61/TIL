package kuke.board.articleread.service.response;

import lombok.Getter;

import java.util.List;

/**
 * 페이징 처리된 게시글 목록을 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
public class ArticleReadPageResponse {
    private List<ArticleReadResponse> articles; // 현재 페이지의 게시글 목록
    private Long articleCount; // 해당 게시판의 전체 게시글 수

    /**
     * ArticleReadPageResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param articles 게시글 목록
     * @param articleCount 전체 게시글 수
     * @return 생성된 ArticleReadPageResponse 객체
     */
    public static ArticleReadPageResponse of(List<ArticleReadResponse> articles, Long articleCount) {
        ArticleReadPageResponse response = new ArticleReadPageResponse();
        response.articles = articles;
        response.articleCount = articleCount;
        return response;
    }
}