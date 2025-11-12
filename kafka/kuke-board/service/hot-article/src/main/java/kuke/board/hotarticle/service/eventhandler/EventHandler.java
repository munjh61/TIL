package kuke.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

/**
 * 수신된 이벤트를 처리하기 위한 핸들러의 공통 인터페이스입니다.
 * article-read 서비스의 EventHandler와 유사하지만, articleId를 추출하는 메서드가 추가되었습니다.
 *
 * @param <T> 핸들러가 처리할 이벤트 페이로드의 타입
 */
public interface EventHandler<T extends EventPayload> {
    /**
     * 주어진 이벤트를 처리하는 비즈니스 로직을 수행합니다.
     * @param event 처리할 이벤트 객체
     */
    void handle(Event<T> event);

    /**
     * 현재 핸들러가 주어진 이벤트를 처리할 수 있는지 여부를 반환합니다.
     * @param event 검사할 이벤트 객체
     * @return 처리할 수 있으면 true, 그렇지 않으면 false
     */
    boolean supports(Event<T> event);

    /**
     * 이벤트 페이로드에서 관련 게시글의 ID(articleId)를 추출합니다.
     * 다양한 종류의 페이로드로부터 일관된 방식으로 articleId를 얻기 위한 추상 메서드입니다.
     * @param event 게시글 ID를 추출할 이벤트 객체
     * @return 추출된 articleId
     */
    Long findArticleId(Event<T> event);
}