package kuke.board.articleread.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Redis에 저장될 캐시 객체입니다.
 * 실제 데이터와 함께 논리적 만료 시간을 포함하여 'stale-while-revalidate' 전략을 지원합니다.
 */
@Getter
@ToString
public class OptimizedCache {
    /**
     * 원본 데이터를 JSON 문자열 형태로 저장합니다.
     * 이렇게 하면 이 캐시 객체가 어떤 타입의 데이터든 제네릭하게 담을 수 있습니다.
     */
    private String data;

    /**
     * 캐시의 논리적 만료 시간입니다.
     * 이 시간이 지나면 캐시는 'stale' 상태가 되어 갱신 대상이 됩니다.
     */
    private LocalDateTime expiredAt;

    /**
     * 데이터와 TTL(Time-To-Live)을 받아 새로운 OptimizedCache 객체를 생성합니다.
     * @param data 캐시할 원본 데이터 객체
     * @param ttl 논리적 만료 시간
     * @return 생성된 OptimizedCache 객체
     */
    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache optimizedCache = new OptimizedCache();
        optimizedCache.data = DataSerializer.serialize(data); // 데이터를 JSON 문자열로 직렬화
        optimizedCache.expiredAt = LocalDateTime.now().plus(ttl); // 현재 시간에 TTL을 더해 만료 시간 계산
        return optimizedCache;
    }

    /**
     * 이 캐시가 논리적으로 만료되었는지 확인합니다.
     * @JsonIgnore: 이 필드는 Redis에 저장될 JSON에 포함되지 않습니다.
     * @return 만료되었으면 true, 아니면 false
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * 저장된 JSON 문자열 데이터를 원래의 타입으로 역직렬화합니다.
     * @param dataType 역직렬화할 클래스 타입
     * @param <T> 데이터의 제네릭 타입
     * @return 역직렬화된 데이터 객체
     */
    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}