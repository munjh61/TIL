package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleIdListRepository;
import kuke.board.articleread.repository.ArticleQueryModel;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.articleread.repository.BoardArticleCountRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * '게시글 생성' 이벤트를 처리하는 핸들러입니다.
 * 이 핸들러는 article-service에서 게시글이 생성되었을 때,
 * article-read 서비스의 데이터(읽기 모델)를 동기화하는 역할을 합니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();

        // 1. 읽기 전용 데이터 모델(ArticleQueryModel)을 생성하여 저장합니다.
        //    조회 성능을 위해 Redis와 같은 캐시 저장소에 저장될 수 있으며, TTL(Time-To-Live)이 설정됩니다.
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1) // 1일 동안 캐시
        );

        // 2. 게시판별 게시글 ID 목록(페이징에 사용)에 새 게시글 ID를 추가합니다.
        //    최신 1000개의 ID만 유지하여 메모리 사용량을 관리합니다.
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);

        // 3. 게시판별 총 게시글 수(비정규화된 카운터)를 업데이트합니다.
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        // 이 핸들러는 ARTICLE_CREATED 타입의 이벤트만 지원합니다.
        return EventType.ARTICLE_CREATED == event.getType();
    }
}