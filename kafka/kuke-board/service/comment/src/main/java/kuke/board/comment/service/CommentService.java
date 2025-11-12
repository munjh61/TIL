package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

/**
 * 댓글(Comment) 관련 비즈니스 로직을 처리하는 서비스 클래스 (V1).
 * 인접 리스트 모델을 기반으로 동작하며, 특히 삭제 로직이 복잡합니다.
 */
@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    /**
     * 새로운 댓글을 생성합니다.
     * V1의 제약: 대댓글은 최상위(루트) 댓글에만 달 수 있습니다. (1레벨 깊이만 허용)
     */
    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    /**
     * 부모 댓글을 조회하고 유효성을 검증합니다.
     * 대댓글을 다는 경우, 부모는 반드시 존재하고, 삭제되지 않았으며, 루트 댓글이어야 합니다.
     */
    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if ( parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    /**
     * 댓글을 삭제합니다.
     * 자식 댓글이 있으면 논리적 삭제(soft delete)를, 없으면 물리적 삭제(hard delete)를 수행합니다.
     */
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        // 자식이 있으면, 스레드 구조 유지를 위해 'deleted' 플래그만 true로 설정
                        comment.delete();
                    } else {
                        // 자식이 없으면, 물리적으로 삭제하고, 부모가 '유령' 상태일 경우 연쇄 삭제를 시도
                        delete(comment);
                    }
                });
    }

    /**
     * 자식 댓글의 존재 여부를 확인합니다.
     * countBy(..., 2L) == 2 로직은 자신(루트 댓글의 경우 parentId=commentId) 외에
     * 다른 자식 댓글이 하나라도 더 있는지 확인하는 방식으로 보입니다.
     */
    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    /**
     * 댓글을 물리적으로 삭제하고, 부모 댓글이 연쇄적으로 삭제될 수 있는지 확인합니다.
     * @param comment 삭제할 댓글
     */
    private void delete(Comment comment) {
        commentRepository.delete(comment);
        // 삭제된 댓글이 루트 댓글이 아닐 경우
        if (!comment.isRoot()) {
            // 부모 댓글을 조회하여
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted) // 부모가 이미 논리적으로 삭제된 '유령' 상태이고,
                    .filter(not(this::hasChildren)) // 다른 자식도 더 이상 없다면,
                    .ifPresent(this::delete); // 부모 댓글도 재귀적으로 삭제합니다.
        }
    }

    /**
     * 오프셋 기반 페이지네이션으로 댓글 목록을 조회합니다.
     */
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    /**
     * 무한 스크롤(커서 기반) 페이지네이션으로 댓글 목록을 조회합니다.
     */
    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
                commentRepository.findAllInfiniteScroll(articleId, limit) :
                commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

}