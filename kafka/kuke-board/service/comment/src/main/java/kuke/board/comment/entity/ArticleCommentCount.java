package kuke.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 각 게시글(Article)별 댓글(Comment)의 총 개수를 저장하는 엔티티입니다.
 * 이는 댓글 수를 조회할 때마다 전체 테이블을 카운트하는 비효율을 피하기 위한
 * 일종의 카운터 캐시(counter cache) 역할을 합니다. (비정규화)
 */
@Table(name = "article_comment_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleCommentCount {
    @Id
    private Long articleId; // 게시글 ID. 이 테이블의 기본 키(Primary Key)이자 샤드 키(shard key)입니다.

    private Long commentCount; // 해당 게시글의 총 댓글 수

    /**
     * 새로운 ArticleCommentCount 객체를 생성(초기화)하는 정적 팩토리 메서드입니다.
     * @param articleId 게시글 ID
     * @param commentCount 댓글 수
     * @return 생성된 ArticleCommentCount 객체
     */
    public static ArticleCommentCount init(Long articleId, Long commentCount) {
        ArticleCommentCount articleCommentCount = new ArticleCommentCount();
        articleCommentCount.articleId = articleId;
        articleCommentCount.commentCount = commentCount;
        return articleCommentCount;
    }
}