package kuke.board.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 각 게시글(Article)별 조회수(View Count)를 영구적으로 저장하는 JPA 엔티티입니다.
 * 이는 조회수 정보를 데이터베이스에 보존하는 역할을 합니다.
 */
@Table(name = "article_view_count") // 'article_view_count' 테이블과 매핑
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class ArticleViewCount {
    @Id
    private Long articleId; // 게시글 ID. 이 테이블의 기본 키(Primary Key)이자 샤드 키(shard key)입니다.

    private Long viewCount; // 해당 게시글의 총 조회수

    /**
     * 새로운 ArticleViewCount 객체를 생성(초기화)하는 정적 팩토리 메서드입니다.
     * @param articleId 게시글 ID
     * @param viewCount 조회수
     * @return 생성된 ArticleViewCount 객체
     */
    public static ArticleViewCount init(Long articleId, Long viewCount) {
        ArticleViewCount articleViewCount = new ArticleViewCount();
        articleViewCount.articleId = articleId;
        articleViewCount.viewCount = viewCount;
        return articleViewCount;
    }
}