package kuke.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 댓글 정보를 나타내는 엔티티 클래스 (V1).
 * 간단한 인접 리스트(Adjacency List) 모델을 사용하여 댓글과 대댓글 관계를 표현합니다.
 */
@Table(name = "comment")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    private Long commentId; // 댓글의 고유 ID

    private String content; // 내용

    private Long parentCommentId; // 부모 댓글의 ID.

    private Long articleId; // 게시글 ID. 샤드 키(shard key)로 사용됩니다.
    private Long writerId; // 작성자 ID

    private Boolean deleted; // 삭제 여부
    private LocalDateTime createdAt; // 생성 시각

    /**
     * 새로운 Comment 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param commentId 댓글 ID
     * @param content 내용
     * @param parentCommentId 부모 댓글 ID. 최상위 댓글일 경우 null.
     * @param articleId 게시글 ID
     * @param writerId 작성자 ID
     * @return 생성된 Comment 객체
     */
    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        // 부모 ID가 null이면(최상위 댓글), 자신의 ID를 부모 ID로 설정하여 루트 댓글임을 표시합니다.
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    /**
     * 이 댓글이 최상위(루트) 댓글인지 확인합니다.
     * @return 루트 댓글이면 true, 아니면 false
     */
    public boolean isRoot() {
        return parentCommentId.longValue() == commentId;
    }

    /**
     * 댓글을 삭제 상태로 변경합니다.
     * 실제 데이터를 지우지 않고 'deleted' 플래그만 true로 설정하는 논리적 삭제(soft delete)입니다.
     */
    public void delete() {
        deleted = true;
    }
}