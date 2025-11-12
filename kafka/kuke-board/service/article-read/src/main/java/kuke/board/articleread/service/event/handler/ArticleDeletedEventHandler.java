package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleIdListRepository;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.articleread.repository.BoardArticleCountRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleDeletedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * '게시글 삭제' 이벤트를 처리하는 핸들러입니다.
 * article-service에서 게시글이 삭제되었을 때, article-read 서비스의 데이터를 동기화합니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();

        // 1. 게시판별 게시글 ID 목록에서 삭제된 게시글의 ID를 제거합니다.
        articleIdListRepository.delete(payload.getBoardId(), payload.getArticleId());

        // 2. 캐시/읽기 저장소에서 게시글 조회 모델을 삭제합니다.
        articleQueryModelRepository.delete(payload.getArticleId());

        // 3. 게시판별 총 게시글 수를 새로운 값으로 업데이트합니다.
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        // 이 핸들러는 ARTICLE_DELETED 타입의 이벤트만 지원합니다.
        return EventType.ARTICLE_DELETED == event.getType();
    }
}