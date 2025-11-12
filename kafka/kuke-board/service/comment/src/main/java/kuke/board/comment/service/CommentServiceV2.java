package kuke.board.comment.service;

import kuke.board.comment.entity.ArticleCommentCount;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.ArticleCommentCountRepository;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.CommentCreatedEventPayload;
import kuke.board.common.event.payload.CommentDeletedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

/**
 * 댓글(Comment) 관련 비즈니스 로직을 처리하는 서비스 클래스 (V2).
 * '구체화된 경로(Materialized Path)' 패턴을 사용하여 V1의 한계(깊이, 성능)를 개선했습니다.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleCommentCountRepository articleCommentCountRepository;

    /**
     * 새로운 댓글을 생성합니다. V1과 달리 여러 깊이의 대댓글을 허용합니다.
     */
    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        // 부모가 없으면 빈 경로, 있으면 부모의 경로를 가져옵니다.
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepository.save(
                CommentV2.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        // 부모 경로를 기반으로 새로운 자식 경로를 생성합니다.
                        parentCommentPath.createChildCommentPath(
                                // 부모의 마지막 후손 경로를 찾아 전달하여, 다음 순서의 경로를 생성하도록 합니다.
                                commentRepository.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        // 게시글의 댓글 수 카운터를 1 증가시킵니다.
        int result = articleCommentCountRepository.increase(request.getArticleId());
        if (result == 0) { // 카운터가 없으면 새로 생성
            articleCommentCountRepository.save(
                    ArticleCommentCount.init(request.getArticleId(), 1L)
            );
        }

        // '댓글 생성' 이벤트를 Outbox를 통해 발행하여 다른 서비스에 알립니다.
        outboxEventPublisher.publish(
                EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
        );

        return CommentResponse.from(comment);
    }

    /**
     * 부모 댓글을 경로(path)를 이용해 조회하고 유효성을 검증합니다.
     */
    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }
        return commentRepository.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    /**
     * 댓글을 삭제하고 '댓글 삭제' 이벤트를 발행합니다.
     */
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)) {
                        comment.delete(); // 자식이 있으면 논리적 삭제
                    } else {
                        delete(comment); // 자식이 없으면 물리적 삭제 및 연쇄 삭제 시도
                    }

                    // '댓글 삭제' 이벤트를 발행합니다.
                    outboxEventPublisher.publish(
                            EventType.COMMENT_DELETED,
                            CommentDeletedEventPayload.builder()
                                    .commentId(comment.getCommentId())
                                    .content(comment.getContent())
                                    .articleId(comment.getArticleId())
                                    .writerId(comment.getWriterId())
                                    .deleted(comment.getDeleted())
                                    .createdAt(comment.getCreatedAt())
                                    .articleCommentCount(count(comment.getArticleId()))
                                    .build(),
                            comment.getArticleId()
                    );
                });
    }

    /**
     * 자식 댓글의 존재 여부를 확인합니다. V1보다 훨씬 명확하고 안정적인 방식입니다.
     */
    private boolean hasChildren(CommentV2 comment) {
        return commentRepository.findDescendantsTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    /**
     * 댓글을 물리적으로 삭제하고, 카운터를 감소시키며, 부모의 연쇄 삭제를 시도합니다.
     */
    private void delete(CommentV2 comment) {
        commentRepository.delete(comment);
        articleCommentCountRepository.decrease(comment.getArticleId());
        if (!comment.isRoot()) {
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

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
     * V1과 달리 마지막 댓글의 'path'만 커서로 사용하므로 더 간단합니다.
     */
    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentV2> comments = lastPath == null ?
                commentRepository.findAllInfiniteScroll(articleId, pageSize) :
                commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public Long count(Long articleId) {
        return articleCommentCountRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }
}