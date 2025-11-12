package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.CommentCreatedEventPayload;
import kuke.board.common.event.payload.CommentDeletedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * '댓글 삭제' 이벤트를 처리하는 핸들러입니다.
 * comment-service에서 댓글이 삭제되었을 때, article-read 서비스의 게시글 데이터(댓글 수)를 동기화합니다.
 */
@Component
@RequiredArgsConstructor
public class CommentDeletedEventHandler implements EventHandler<CommentDeletedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<CommentDeletedEventPayload> event) {
        // 1. 댓글이 삭제된 게시글 ID로 읽기 모델(ArticleQueryModel)을 조회합니다.
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    // 2. 조회된 모델이 존재하면, 이벤트 페이로드의 정보로 모델을 업데이트합니다.
                    //    (이 경우, 댓글 수가 감소됩니다)
                    articleQueryModel.updateBy(event.getPayload());
                    // 3. 업데이트된 모델을 다시 저장소에 저장합니다.
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<CommentDeletedEventPayload> event) {
        // 이 핸들러는 COMMENT_DELETED 타입의 이벤트만 지원합니다.
        return EventType.COMMENT_DELETED == event.getType();
    }
}