package kuke.board.articleread.service.event.handler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

/**
 * 수신된 이벤트를 처리하기 위한 핸들러의 공통 인터페이스입니다.
 * 전략 패턴(Strategy Pattern)을 사용하여 이벤트 유형에 따라 적절한 핸들러가 선택되도록 합니다.
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
}