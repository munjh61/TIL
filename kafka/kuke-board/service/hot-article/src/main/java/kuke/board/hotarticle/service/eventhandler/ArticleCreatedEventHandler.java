package kuke.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import kuke.board.hotarticle.repository.ArticleCreatedTimeRepository;
import kuke.board.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * '게시글 생성' 이벤트를 처리하는 핸들러입니다.
 * 인기 게시글 점수 계산에 필요한 게시글의 생성 시간을 Redis에 저장합니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload>{
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        // 게시글의 생성 시간을 Redis에 저장합니다.
        articleCreatedTimeRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getCreatedAt(),
                // TTL(만료 시간)을 오늘 자정까지로 설정합니다.
                // 오래된 게시글은 인기 게시글이 될 가능성이 낮으므로, 자정이 지나면
                // 관련 데이터를 자동으로 삭제하여 Redis 메모리를 효율적으로 관리합니다.
                TimeCalculatorUtils.calculateDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleCreatedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}