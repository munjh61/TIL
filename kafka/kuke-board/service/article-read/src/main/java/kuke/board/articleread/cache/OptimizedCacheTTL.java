package kuke.board.articleread.cache;

import lombok.Getter;

import java.time.Duration;

/**
 * 캐시의 논리적 TTL(Logical TTL)과 물리적 TTL(Physical TTL)을 관리하는 값 객체(Value Object)입니다.
 * 'stale-while-revalidate' 전략을 구현하는 데 핵심적인 역할을 합니다.
 */
@Getter
public class OptimizedCacheTTL {
    /**
     * 논리적 TTL: 애플리케이션 코드에서 캐시 데이터가 신선한지(fresh) 오래되었는지(stale) 판단하는 기준 시간.
     * 이 시간이 지나면 데이터는 'stale' 상태가 되어 갱신 대상으로 간주됩니다.
     */
    private Duration logicalTTL;

    /**
     * 물리적 TTL: Redis에 실제로 설정되는 데이터의 만료 시간.
     * 논리적 TTL보다 길게 설정하여, 논리적으로 만료된 후에도 잠시 동안 오래된 데이터를 서빙할 수 있도록 합니다.
     */
    private Duration physicalTTL;

    /**
     * 물리적 TTL을 논리적 TTL보다 얼마나 더 길게 설정할지에 대한 시간 (초).
     * 이 시간 동안 'stale' 데이터가 서빙될 수 있습니다.
     */
    public static final long PHYSICAL_TTL_DELAY_SECONDS = 5;

    /**
     * 주어진 TTL(초)로부터 OptimizedCacheTTL 객체를 생성합니다.
     * @param ttlSeconds 논리적 TTL (초)
     * @return 생성된 OptimizedCacheTTL 객체
     */
    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTL = Duration.ofSeconds(ttlSeconds);
        // 물리적 TTL = 논리적 TTL + 지연 시간
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.logicalTTL.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS);
        return optimizedCacheTTL;
    }
}