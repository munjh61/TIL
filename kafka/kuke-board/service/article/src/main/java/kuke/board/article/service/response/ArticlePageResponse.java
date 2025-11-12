package kuke.board.article.service.response;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 페이징 처리된 게시글 목록을 클라이언트에게 응답으로 보낼 때 사용하는 데이터 전송 객체(DTO)입니다.
 */
@Getter
@ToString
public class ArticlePageResponse {
    private List<ArticleResponse> articles; // 현재 페이지의 게시글 목록
    private Long articleCount; // 게시글 수 (주의: 전체 게시글 수가 아닐 수 있음. 페이지네이션 UI 계산을 위한 값)

    /**
     * ArticlePageResponse DTO를 생성하는 정적 팩토리 메서드입니다.
     * @param articles 게시글 목록
     * @param articleCount 게시글 수
     * @return 생성된 ArticlePageResponse 객체
     */
    public static ArticlePageResponse of(List<ArticleResponse> articles, Long articleCount) {
        ArticlePageResponse response = new ArticlePageResponse();
        response.articles = articles;
        response.articleCount = articleCount;
        return response;
    }
}