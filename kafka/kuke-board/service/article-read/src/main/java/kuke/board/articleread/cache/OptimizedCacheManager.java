package kuke.board.articleread.cache;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * 커스텀 캐싱 전략의 핵심 로직을 처리하는 클래스입니다.
 * 'stale-while-revalidate' 패턴과 분산 락을 사용하여 캐시 스탬피드(cache stampede)를 방지합니다.
 */
@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    /**
     * 캐싱 로직을 처리하는 메인 메서드입니다.
     * @param type 캐시 타입
     * @param ttlSeconds 캐시 만료 시간 (초)
     * @param args 원본 메서드의 인자 (캐시 키 생성용)
     * @param returnType 원본 메서드의 반환 타입 (역직렬화용)
     * @param originDataSupplier 원본 데이터를 가져오는 함수 (캐시 미스 시 호출)
     * @return 캐시된 데이터 또는 원본 데이터
     * @throws Throwable 원본 데이터 조회 중 발생 가능 예외
     */
    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args);

        // 1. Redis에서 데이터를 조회합니다.
        String cachedData = redisTemplate.opsForValue().get(key);
        // 2. 캐시가 아예 없는 경우 (Hard Miss), 원본 데이터를 조회하고 캐시에 저장합니다.
        if (cachedData == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);
        if (optimizedCache == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 3. 캐시가 논리적으로 만료되지 않은 경우 (Fresh), 캐시된 데이터를 즉시 반환합니다.
        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        // 4. 캐시가 논리적으로 만료된 경우 (Stale), 캐시 스탬피드를 막기 위해 분산 락을 시도합니다.
        if (!optimizedCacheLockProvider.lock(key)) {
            // 5. 락 획득에 실패하면, 다른 스레드가 캐시를 갱신 중이므로, 오래된(stale) 데이터를 우선 반환합니다.
            //    (stale-while-revalidate)
            return optimizedCache.parseData(returnType);
        }

        try {
            // 6. 락 획득에 성공하면, 현재 스레드가 책임지고 캐시를 갱신합니다.
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            // 7. 캐시 갱신 후에는 반드시 락을 해제합니다.
            optimizedCacheLockProvider.unlock(key);
        }
    }

    /**
     * 원본 데이터를 조회하고, 그 결과를 새로운 캐시 객체로 만들어 Redis에 저장합니다.
     * @param originDataSupplier 원본 데이터를 가져오는 함수
     * @param key Redis 키
     * @param ttlSeconds 캐시 만료 시간
     * @return 원본 데이터 조회 결과
     * @throws Throwable 원본 데이터 조회 중 발생 가능 예외
     */
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        // 원본 메서드 실행
        Object result = originDataSupplier.get();

        // 논리적 TTL과 물리적 TTL을 계산합니다.
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        // 데이터와 논리적 만료 시간을 포함하는 캐시 객체를 생성합니다.
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());

        // 캐시 객체를 직렬화하여 Redis에 저장하고, 물리적 만료 시간을 설정합니다.
        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizedCache),
                        optimizedCacheTTL.getPhysicalTTL()
                );

        return result;
    }

    /**
     * 캐시 타입과 메서드 인자를 조합하여 고유한 Redis 키를 생성합니다.
     * @param prefix 캐시 타입
     * @param args 메서드 인자
     * @return 생성된 Redis 키
     */
    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(joining(DELIMITER));
    }

}