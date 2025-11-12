package kuke.board.like.service;

import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleLikedEventPayload;
import kuke.board.common.event.payload.ArticleUnlikedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.common.snowflake.Snowflake;
import kuke.board.like.entity.ArticleLike;
import kuke.board.like.entity.ArticleLikeCount;
import kuke.board.like.repository.ArticleLikeCountRepository;
import kuke.board.like.repository.ArticleLikeRepository;
import kuke.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 좋아요/좋아요 취소 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 좋아요 수 카운터의 동시성 제어를 위해 다양한 전략(네이티브 쿼리, 비관적 잠금, 낙관적 잠금)을 시연합니다.
 */
@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    /**
     * 특정 게시글에 대한 특정 사용자의 좋아요 정보를 조회합니다.
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     * @return 좋아요 정보
     */
    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    /**
     * [동시성 제어 전략 1: 네이티브 쿼리 (UPDATE) 사용]
     * 게시글에 '좋아요'를 추가합니다.
     * ArticleLikeCount의 업데이트는 네이티브 쿼리를 통해 직접 수행됩니다.
     * 이 방식은 데이터베이스의 원자적 UPDATE 문에 의존하여 동시성을 처리합니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        // 1. ArticleLike 엔티티 저장 (좋아요 기록)
        ArticleLike articleLike = articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        // 2. ArticleLikeCount 업데이트 (네이티브 쿼리 사용)
        int result = articleLikeCountRepository.increase(articleId);
        if (result == 0) {
            // 최초 좋아요 시에는 카운터 레코드가 없으므로, 1로 초기화하여 저장합니다.
            // 이 부분은 동시성 문제에 취약할 수 있으므로, 게시글 생성 시점에 미리 0으로 초기화하는 것이 더 안전합니다.
            articleLikeCountRepository.save(
                    ArticleLikeCount.init(articleId, 1L)
            );
        }

        // 3. '좋아요' 이벤트를 Outbox를 통해 발행
        outboxEventPublisher.publish(
                EventType.ARTICLE_LIKED,
                ArticleLikedEventPayload.builder()
                        .articleLikeId(articleLike.getArticleLikeId())
                        .articleId(articleLike.getArticleId())
                        .userId(articleLike.getUserId())
                        .createdAt(articleLike.getCreatedAt())
                        .articleLikeCount(count(articleLike.getArticleId()))
                        .build(),
                articleLike.getArticleId()
        );
    }

    /**
     * [동시성 제어 전략 1: 네이티브 쿼리 (UPDATE) 사용]
     * 게시글의 '좋아요'를 취소합니다.
     * ArticleLikeCount의 업데이트는 네이티브 쿼리를 통해 직접 수행됩니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void unlikePessimisticLock1(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    // 1. ArticleLike 엔티티 삭제 (좋아요 기록 제거)
                    articleLikeRepository.delete(articleLike);
                    // 2. ArticleLikeCount 업데이트 (네이티브 쿼리 사용)
                    articleLikeCountRepository.decrease(articleId);
                    // 3. '좋아요 취소' 이벤트를 Outbox를 통해 발행
                    outboxEventPublisher.publish(
                            EventType.ARTICLE_UNLIKED,
                            ArticleUnlikedEventPayload.builder()
                                    .articleLikeId(articleLike.getArticleLikeId())
                                    .articleId(articleLike.getArticleId())
                                    .userId(articleLike.getUserId())
                                    .createdAt(articleLike.getCreatedAt())
                                    .articleLikeCount(count(articleLike.getArticleId()))
                                    .build(),
                            articleLike.getArticleId()
                    );
                });
    }

    /**
     * [동시성 제어 전략 2: 비관적 잠금 (PESSIMISTIC_WRITE) 사용]
     * 게시글에 '좋아요'를 추가합니다.
     * ArticleLikeCount 엔티티를 조회할 때 데이터베이스 레벨에서 잠금을 걸어
     * 다른 트랜잭션의 동시 수정을 방지합니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        // 1. ArticleLike 엔티티 저장 (좋아요 기록)
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );
        // 2. ArticleLikeCount 엔티티를 비관적 잠금으로 조회하거나 초기화
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        // 3. 좋아요 수 증가
        articleLikeCount.increase();
        // 4. 변경된 ArticleLikeCount 엔티티 저장 (잠금 해제)
        articleLikeCountRepository.save(articleLikeCount);
        // 참고: 이 메서드에서는 이벤트 발행 로직이 생략되어 있습니다.
    }

    /**
     * [동시성 제어 전략 2: 비관적 잠금 (PESSIMISTIC_WRITE) 사용]
     * 게시글의 '좋아요'를 취소합니다.
     * ArticleLikeCount 엔티티를 조회할 때 데이터베이스 레벨에서 잠금을 걸어
     * 다른 트랜잭션의 동시 수정을 방지합니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void unlikePessimisticLock2(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    // 1. ArticleLike 엔티티 삭제 (좋아요 기록 제거)
                    articleLikeRepository.delete(articleLike);
                    // 2. ArticleLikeCount 엔티티를 비관적 잠금으로 조회
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    // 3. 좋아요 수 감소
                    articleLikeCount.decrease();
                    // 4. 변경된 ArticleLikeCount 엔티티 저장 (잠금 해제)
                    articleLikeCountRepository.save(articleLikeCount);
                    // 참고: 이 메서드에서는 이벤트 발행 로직이 생략되어 있습니다.
                });
    }

    /**
     * [동시성 제어 전략 3: 낙관적 잠금 (Optimistic Locking) 사용]
     * 게시글에 '좋아요'를 추가합니다.
     * ArticleLikeCount 엔티티의 @Version 필드를 사용하여 동시성 문제를 해결합니다.
     * 충돌 발생 시 OptimisticLockingFailureException이 발생하며, 재시도 로직이 필요할 수 있습니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        // 1. ArticleLike 엔티티 저장 (좋아요 기록)
        articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        // 2. ArticleLikeCount 엔티티 조회 (잠금 없음)
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        // 3. 좋아요 수 증가
        articleLikeCount.increase();
        // 4. 변경된 ArticleLikeCount 엔티티 저장 (버전 필드 자동 업데이트)
        //    이때 다른 트랜잭션이 같은 엔티티를 먼저 수정했다면 OptimisticLockingFailureException 발생
        articleLikeCountRepository.save(articleLikeCount);
        // 참고: 이 메서드에서는 이벤트 발행 로직이 생략되어 있습니다.
    }

    /**
     * [동시성 제어 전략 3: 낙관적 잠금 (Optimistic Locking) 사용]
     * 게시글의 '좋아요'를 취소합니다.
     * ArticleLikeCount 엔티티의 @Version 필드를 사용하여 동시성 문제를 해결합니다.
     *
     * @param articleId 게시글 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void unlikeOptimisticLock(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    // 1. ArticleLike 엔티티 삭제 (좋아요 기록 제거)
                    articleLikeRepository.delete(articleLike);
                    // 2. ArticleLikeCount 엔티티 조회 (잠금 없음)
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                    // 3. 좋아요 수 감소
                    articleLikeCount.decrease();
                    // 4. 변경된 ArticleLikeCount 엔티티 저장
                    articleLikeCountRepository.save(articleLikeCount);
                    // 참고: 이 메서드에서는 이벤트 발행 로직이 생략되어 있습니다.
                });
    }

    /**
     * 특정 게시글의 현재 좋아요 수를 조회합니다.
     * @param articleId 게시글 ID
     * @return 좋아요 수. 데이터가 없으면 0L을 반환합니다.
     */
    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}