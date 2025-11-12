package kuke.board.hotarticle.service;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.hotarticle.repository.ArticleCreatedTimeRepository;
import kuke.board.hotarticle.repository.HotArticleListRepository;
import kuke.board.hotarticle.service.eventhandler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 수신된 이벤트를 바탕으로 게시글의 인기 점수를 계산하고,
 * 인기 게시글 리더보드(Sorted Set)를 갱신하는 역할을 담당하는 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private final HotArticleListRepository hotArticleListRepository;
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    private static final long HOT_ARTICLE_COUNT = 10; // 리더보드에 유지할 인기 게시글의 수
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10); // 리더보드 데이터의 만료 시간

    /**
     * 이벤트를 처리하여 인기 게시글 점수 및 리더보드를 업데이트합니다.
     * @param event 처리할 이벤트
     * @param eventHandler 이벤트에 맞는 핸들러
     */
    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
        Long articleId = eventHandler.findArticleId(event);
        LocalDateTime createdTime = articleCreatedTimeRepository.read(articleId);

        // 비즈니스 규칙: 오늘 생성된 게시글만 인기 게시글 후보가 됩니다.
        if (!isArticleCreatedToday(createdTime)) {
            return;
        }

        // 1. 이벤트 핸들러를 호출하여 각 지표(좋아요, 댓글, 조회수)의 최신 카운트를 Redis에 저장합니다.
        eventHandler.handle(event);

        // 2. 업데이트된 카운트를 바탕으로 새로운 인기 점수를 계산합니다.
        long score = hotArticleScoreCalculator.calculate(articleId);

        // 3. 계산된 점수로 리더보드(Sorted Set)를 업데이트합니다.
        hotArticleListRepository.add(
                articleId,
                createdTime,
                score,
                HOT_ARTICLE_COUNT,
                HOT_ARTICLE_TTL
        );
    }

    /**
     * 게시글이 오늘 생성되었는지 확인합니다.
     * @param createdTime 게시글 생성 시간
     * @return 오늘 생성되었으면 true, 아니면 false
     */
    private boolean isArticleCreatedToday(LocalDateTime createdTime) {
        return createdTime != null && createdTime.toLocalDate().equals(LocalDate.now());
    }
}