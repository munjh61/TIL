package kuke.board.view.service;

import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleViewedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.view.entity.ArticleViewCount;
import kuke.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis에 있는 실시간 조회수 데이터를 데이터베이스에 백업(영구 저장)하고,
 * 관련 이벤트를 발행하는 프로세서 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    /**
     * 특정 게시글의 조회수를 데이터베이스에 백업하고, '게시글 조회' 이벤트를 발행합니다.
     * @param articleId 게시글 ID
     * @param viewCount 현재 Redis에 있는 최신 조회수
     */
    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        // 1. 데이터베이스의 조회수를 업데이트합니다.
        //    이때, 새로운 viewCount가 기존 DB 값보다 클 경우에만 업데이트를 수행합니다.
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);

        // 2. 만약 업데이트된 행이 0이고, 해당 articleId의 레코드가 DB에 없다면 새로 생성합니다.
        //    (result == 0은 업데이트가 안 되었거나, 레코드가 없거나 둘 중 하나)
        if (result == 0) {
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> { }, // 레코드가 이미 있으면 아무것도 안 함
                        () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount)) // 없으면 새로 저장
                    );
        }

        // 3. '게시글 조회' 이벤트를 Outbox를 통해 발행합니다.
        //    이 이벤트는 다른 서비스(예: hot-article)에서 소비하여 조회수 변경에 반응할 수 있도록 합니다.
        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}