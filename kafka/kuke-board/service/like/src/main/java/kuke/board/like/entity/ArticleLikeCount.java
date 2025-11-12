package kuke.board.like.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 각 게시글(Article)별 좋아요(Like)의 총 개수를 저장하는 엔티티입니다.
 * 이는 좋아요 수를 조회할 때마다 전체 테이블을 카운트하는 비효율을 피하기 위한
 * 일종의 카운터 캐시(counter cache) 역할을 합니다. (비정규화)
 */
@Table(name = "article_like_count") // 'article_like_count' 테이블과 매핑
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class ArticleLikeCount {
    @Id
    private Long articleId; // 게시글 ID. 이 테이블의 기본 키(Primary Key)이자 샤드 키(shard key)입니다.

    private Long likeCount; // 해당 게시글의 총 좋아요 수

    /**
     * 낙관적 잠금(Optimistic Locking)을 위한 버전 필드입니다.
     * 동시성 문제를 해결하고 데이터 무결성을 보장하는 데 사용됩니다.
     * 엔티티가 업데이트될 때마다 자동으로 증가합니다.
     */
    @Version
    private Long version;

    /**
     * 새로운 ArticleLikeCount 객체를 생성(초기화)하는 정적 팩토리 메서드입니다.
     * @param articleId 게시글 ID
     * @param likeCount 좋아요 수
     * @return 생성된 ArticleLikeCount 객체
     */
    public static ArticleLikeCount init(Long articleId, Long likeCount) {
        ArticleLikeCount articleLikeCount = new ArticleLikeCount();
        articleLikeCount.articleId = articleId;
        articleLikeCount.likeCount = likeCount;
        articleLikeCount.version = 0L; // 초기 버전은 0
        return articleLikeCount;
    }

    /**
     * 좋아요 수를 1 증가시킵니다.
     */
    public void increase() {
        this.likeCount++;
    }

    /**
     * 좋아요 수를 1 감소시킵니다.
     */
    public void decrease() {
        this.likeCount--;
    }
}