package kuke.board.hotarticle.service;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.hotarticle.client.ArticleClient;
import kuke.board.hotarticle.repository.HotArticleListRepository;
import kuke.board.hotarticle.service.eventhandler.EventHandler;
import kuke.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 인기 게시글 관련 비즈니스 로직을 처리하는 최상위 서비스 클래스입니다.
 * 이벤트 수신, 처리 위임, 최종 목록 조회를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    private final ArticleClient articleClient;
    private final List<EventHandler> eventHandlers; // Spring이 주입해주는 모든 EventHandler 구현체 리스트
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    /**
     * Kafka 컨슈머로부터 받은 이벤트를 처리하는 메인 진입점입니다.
     * @param event 처리할 이벤트
     */
    public void handleEvent(Event<EventPayload> event) {
        // 1. Strategy Pattern: 이벤트 타입에 맞는 핸들러를 찾습니다.
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        // 2. 이벤트 종류에 따라 처리 로직을 분기합니다.
        if (isArticleCreatedOrDeleted(event)) {
            // 2-1. 게시글 생성/삭제 이벤트는 점수 계산 없이 단순 데이터 처리만 수행합니다.
            // (생성 시 점수는 0, 삭제 시 데이터 정리)
            eventHandler.handle(event);
        } else {
            // 2-2. 그 외 이벤트(좋아요, 댓글, 조회수)는 점수 계산 및 리더보드 업데이트를 수행합니다.
            hotArticleScoreUpdater.update(event, eventHandler);
        }
    }

    /**
     * 이벤트 핸들러 리스트에서 이벤트를 지원하는 핸들러를 찾습니다.
     */
    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    /**
     * 특정 날짜의 인기 게시글 목록을 조회합니다.
     * @param dateStr 조회할 날짜 (yyyyMMdd 형식)
     * @return 인기 게시글 목록
     */
    public List<HotArticleResponse> readAll(String dateStr) {
        // 1. Redis 리더보드에서 해당 날짜의 인기 게시글 ID 목록을 점수 순으로 가져옵니다.
        return hotArticleListRepository.readAll(dateStr).stream()
                // 2. 각 ID에 대해 article-read 서비스에 게시글 정보를 요청합니다.
                //    (N+1 쿼리 문제가 발생할 수 있으나, 목록이 10개로 작고 article-read가 캐시되므로 허용 가능)
                .map(articleClient::read)
                .filter(Objects::nonNull)
                // 3. 조회된 정보를 HotArticleResponse DTO로 변환합니다.
                .map(HotArticleResponse::from)
                .toList();
    }
}