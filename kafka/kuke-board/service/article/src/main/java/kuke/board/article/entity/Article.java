package kuke.board.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 게시글 정보를 나타내는 JPA 엔티티 클래스입니다.
 */
@Table(name = "article") // 'article' 테이블과 매핑
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class Article {
    @Id
    private Long articleId; // 게시글의 고유 ID

    private String title; // 제목
    private String content; // 내용

    private Long boardId; // 게시판 ID. 이 시스템에서는 샤드 키(shard key)로도 사용됩니다.
    private Long writerId; // 작성자 ID

    private LocalDateTime createdAt; // 생성 시각
    private LocalDateTime modifiedAt; // 수정 시각

    /**
     * 새로운 Article 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param articleId 게시글 ID
     * @param title 제목
     * @param content 내용
     * @param boardId 게시판 ID
     * @param writerId 작성자 ID
     * @return 생성된 Article 객체
     */
    public static Article create(Long articleId, String title, String content, Long boardId, Long writerId) {
        Article article = new Article();
        article.articleId = articleId;
        article.title = title;
        article.content = content;
        article.boardId = boardId;
        article.writerId = writerId;
        article.createdAt = LocalDateTime.now();
        article.modifiedAt = article.createdAt; // 생성 시점에는 수정 시각과 동일
        return article;
    }

    /**
     * 게시글의 제목과 내용을 수정합니다.
     * @param title 새로운 제목
     * @param content 새로운 내용
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.modifiedAt = LocalDateTime.now(); // 수정 시각을 현재 시간으로 업데이트
    }

}