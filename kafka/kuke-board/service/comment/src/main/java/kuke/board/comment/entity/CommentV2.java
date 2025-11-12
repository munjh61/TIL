package kuke.board.comment.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 댓글 정보를 나타내는 엔티티 클래스 (V2).
 * '구체화된 경로(Materialized Path)' 패턴을 사용하여 댓글 계층 구조를 효율적으로 관리합니다.
 * V1의 인접 리스트 모델의 조회 성능 문제를 개선한 버전입니다.
 */
@Table(name = "comment_v2")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentV2 {
    @Id
    private Long commentId; // 댓글의 고유 ID

    private String content; // 내용

    private Long articleId; // 게시글 ID. 샤드 키(shard key)로 사용됩니다.
    private Long writerId; // 작성자 ID

    /**
     * 댓글의 계층 구조 내 위치를 나타내는 경로 정보.
     * @Embedded 어노테이션을 통해 이 객체의 필드들이 CommentV2 테이블의 컬럼으로 포함됩니다.
     */
    @Embedded
    private CommentPath commentPath;

    private Boolean deleted; // 삭제 여부
    private LocalDateTime createdAt; // 생성 시각

    /**
     * 새로운 CommentV2 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param commentId 댓글 ID
     * @param content 내용
     * @param articleId 게시글 ID
     * @param writerId 작성자 ID
     * @param commentPath 계층 구조 경로
     * @return 생성된 CommentV2 객체
     */
    public static CommentV2 create(Long commentId, String content, Long articleId, Long writerId, CommentPath commentPath) {
        CommentV2 comment = new CommentV2();
        comment.commentId = commentId;
        comment.content = content;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.commentPath = commentPath;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    /**
     * 이 댓글이 최상위(루트) 댓글인지 확인합니다.
     * @return 루트 댓글이면 true, 아니면 false
     */
    public boolean isRoot() {
        return commentPath.isRoot();
    }

    /**
     * 댓글을 삭제 상태로 변경합니다. (논리적 삭제)
     */
    public void delete() {
        deleted = true;
    }
}