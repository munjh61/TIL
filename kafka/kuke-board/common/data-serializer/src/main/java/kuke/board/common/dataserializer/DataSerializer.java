package kuke.board.common.dataserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터를 직렬화하거나 역직렬화하는 유틸리티 클래스입니다.
 * Jackson ObjectMapper를 사용하여 JSON 변환을 처리합니다.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // 유틸리티 클래스이므로 private 생성자를 통해 인스턴스 생성을 막습니다.
public final class DataSerializer {
    // JSON 직렬화/역직렬화를 위한 ObjectMapper 인스턴스
    private static final ObjectMapper objectMapper = initialize();

    /**
     * ObjectMapper를 초기화하는 메서드입니다.
     * JavaTimeModule을 등록하여 Java 8의 날짜/시간 API(JSR-310)를 지원하고,
     * 알 수 없는 속성이 있어도 역직렬화에 실패하지 않도록 설정합니다.
     * @return 구성이 완료된 ObjectMapper 인스턴스
     */
    private static ObjectMapper initialize() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Java 8 날짜/시간 타입(LocalDate, LocalDateTime 등)을 지원합니다.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // JSON에 알 수 없는 필드가 있어도 오류를 발생시키지 않습니다.
    }

    /**
     * JSON 형식의 문자열 데이터를 주어진 클래스 타입의 객체로 역직렬화합니다.
     * @param data 역직렬화할 JSON 문자열
     * @param clazz 변환할 클래스 타입
     * @param <T> 변환될 객체의 타입
     * @return 역직렬화된 객체. 실패 시 null을 반환합니다.
     */
    public static <T> T deserialize(String data, Class<T> clazz) {
        try {
            // objectMapper를 사용하여 JSON 문자열을 지정된 클래스 객체로 읽습니다.
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            // 역직렬화 중 오류 발생 시 로그를 남깁니다.
            log.error("[DataSerializer.deserialize] data={}, clazz={}", data, clazz, e);
            return null;
        }
    }

    /**
     * 한 객체를 다른 클래스 타입의 객체로 변환합니다. (예: Map -> DTO)
     * @param data 변환할 소스 객체
     * @param clazz 변환할 클래스 타입
     * @param <T> 변환될 객체의 타입
     * @return 변환된 객체
     */
    public static <T> T deserialize(Object data, Class<T> clazz) {
        // objectMapper를 사용하여 한 객체를 다른 타입의 객체로 변환합니다.
        return objectMapper.convertValue(data, clazz);
    }

    /**
     * 주어진 객체를 JSON 형식의 문자열로 직렬화합니다.
     * @param object 직렬화할 객체
     * @return JSON 문자열. 실패 시 null을 반환합니다.
     */
    public static String serialize(Object object) {
        try {
            // objectMapper를 사용하여 객체를 JSON 문자열로 씁니다.
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // 직렬화 중 오류 발생 시 로그를 남깁니다.
            log.error("[DataSerializer.serialize] object={}", object, e);
            return null;
        }
    }
}