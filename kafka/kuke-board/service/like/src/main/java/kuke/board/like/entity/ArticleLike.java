package kuke.board.like.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 게시글에 대한 '좋아요' 정보를 나타내는 JPA 엔티티 클래스입니다.
 */
@Table(name = "article_like") // 'article_like' 테이블과 매핑
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class ArticleLike {
    @Id
    private Long articleLikeId; // 좋아요 정보의 고유 ID

    private Long articleId; // 좋아요를 받은 게시글의 ID (샤드 키로 사용)
    private Long userId; // 좋아요를 누른 사용자의 ID

    private LocalDateTime createdAt; // 좋아요가 생성된 시각

    /**
     * 새로운 ArticleLike 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param articleLikeId 좋아요 ID
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @return 생성된 ArticleLike 객체
     */
    public static ArticleLike create(Long articleLikeId, Long articleId, Long userId) {
        ArticleLike articleLike = new ArticleLike();
        articleLike.articleLikeId = articleLikeId;
        articleLike.articleId = articleId;
        articleLike.userId = userId;
        articleLike.createdAt = LocalDateTime.now();
        return articleLike;
    }
}