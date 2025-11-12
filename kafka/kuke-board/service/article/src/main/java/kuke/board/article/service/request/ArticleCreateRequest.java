package kuke.board.article.service.request;

import lombok.Getter;
import lombok.ToString;

/**
 * 게시글 생성을 요청할 때 사용하는 데이터 전송 객체(DTO)입니다.
 * Controller에서 클라이언트의 요청 본문(request body)을 이 객체로 매핑합니다.
 */
@Getter
@ToString
public class ArticleCreateRequest {
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private Long writerId; // 작성자 ID
    private Long boardId; // 게시판 ID
}