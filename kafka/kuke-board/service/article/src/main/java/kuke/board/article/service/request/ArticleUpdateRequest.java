package kuke.board.article.service.request;

import lombok.Getter;
import lombok.ToString;

/**
 * 게시글 수정을 요청할 때 사용하는 데이터 전송 객체(DTO)입니다.
 * Controller에서 클라이언트의 요청 본문(request body)을 이 객체로 매핑합니다.
 */
@Getter
@ToString
public class ArticleUpdateRequest {
    private String title; // 수정할 게시글 제목
    private String content; // 수정할 게시글 내용
}