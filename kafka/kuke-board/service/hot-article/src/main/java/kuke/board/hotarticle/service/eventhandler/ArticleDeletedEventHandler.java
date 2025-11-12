package kuke.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleDeletedEventPayload;
import kuke.board.hotarticle.repository.ArticleCreatedTimeRepository;
import kuke.board.hotarticle.repository.HotArticleListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * '게시글 삭제' 이벤트를 처리하는 핸들러입니다.
 * 삭제된 게시글을 인기 게시글 목록 및 관련 데이터에서 제거하는 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final HotArticleListRepository hotArticleListRepository;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        // 1. Redis에 저장된 게시글 생성 시간 정보를 삭제합니다.
        articleCreatedTimeRepository.delete(payload.getArticleId());
        // 2. Redis의 인기 게시글 목록(Sorted Set)에서 해당 게시글을 제거합니다.
        hotArticleListRepository.remove(payload.getArticleId(), payload.getCreatedAt());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return EventType.ARTICLE_DELETED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleDeletedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}