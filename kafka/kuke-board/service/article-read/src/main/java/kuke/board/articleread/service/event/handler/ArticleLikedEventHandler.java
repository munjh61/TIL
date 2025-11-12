package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleLikedEventPayload;
import kuke.board.common.event.payload.CommentCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * '게시글 좋아요' 이벤트를 처리하는 핸들러입니다.
 * like-service에서 좋아요가 추가되었을 때, article-read 서비스의 게시글 데이터(좋아요 수)를 동기화합니다.
 */
@Component
@RequiredArgsConstructor
public class ArticleLikedEventHandler implements EventHandler<ArticleLikedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleLikedEventPayload> event) {
        // 1. 좋아요가 추가된 게시글 ID로 읽기 모델(ArticleQueryModel)을 조회합니다.
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    // 2. 조회된 모델이 존재하면, 이벤트 페이로드의 정보로 모델을 업데이트합니다.
                    //    (이 경우, 좋아요 수가 업데이트됩니다)
                    articleQueryModel.updateBy(event.getPayload());
                    // 3. 업데이트된 모델을 다시 저장소에 저장합니다.
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleLikedEventPayload> event) {
        // 이 핸들러는 ARTICLE_LIKED 타입의 이벤트만 지원합니다.
        return EventType.ARTICLE_LIKED == event.getType();
    }
}