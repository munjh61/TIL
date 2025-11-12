package kuke.board.common.event;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;

/**
 * 시스템 내에서 발생하는 이벤트를 나타내는 제네릭 클래스입니다.
 * 모든 이벤트는 ID, 타입, 그리고 구체적인 데이터를 담는 페이로드(payload)를 가집니다.
 * @param <T> 이벤트 페이로드의 타입을 나타내며, EventPayload를 상속해야 합니다.
 */
@Getter
public class Event<T extends EventPayload> {
    private Long eventId; // 이벤트의 고유 ID
    private EventType type; // 이벤트의 종류
    private T payload; // 이벤트와 관련된 실제 데이터

    /**
     * 주어진 정보로 새로운 Event 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param eventId 이벤트 ID
     * @param type 이벤트 타입
     * @param payload 이벤트 페이로드
     * @return 생성된 Event 객체
     */
    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    /**
     * 현재 Event 객체를 JSON 문자열로 직렬화합니다.
     * @return JSON 형식의 문자열
     */
    public String toJson() {
        return DataSerializer.serialize(this);
    }

    /**
     * JSON 문자열로부터 Event 객체를 역직렬화합니다.
     * 이 메서드는 먼저 원시(raw) 이벤트 객체로 변환한 후,
     * 실제 페이로드 타입에 맞게 다시 변환하는 과정을 거칩니다.
     * @param json 역직렬화할 JSON 문자열
     * @return 역직렬화된 Event 객체. 실패 시 null을 반환합니다.
     */
    public static Event<EventPayload> fromJson(String json) {
        // 1. JSON을 중간 단계인 EventRaw 객체로 역직렬화합니다.
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        // 2. 최종 Event<EventPayload> 객체를 생성합니다.
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType()); // 문자열 타입 정보를 EventType enum으로 변환합니다.
        // 3. payload 부분을 실제 이벤트 타입에 맞는 클래스로 역직렬화합니다.
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    /**
     * JSON 역직렬화를 위한 중간 단계의 내부 정적 클래스입니다.
     * payload가 아직 구체적인 타입으로 변환되기 전의 Object 형태를 가집니다.
     */
    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}