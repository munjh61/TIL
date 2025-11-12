package kuke.board.articleread.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 캐싱 로직을 적용할 메서드에 사용하는 어노테이션입니다.
 * 이 어노테이션이 붙은 메서드는 OptimizedCacheAspect에 의해 가로채져(intercept) 캐싱이 처리됩니다.
 */
@Retention(RetentionPolicy.RUNTIME) // 런타임에 이 어노테이션 정보가 유지되도록 설정 (AOP에서 필요)
@Target(ElementType.METHOD) // 메서드에만 적용 가능하도록 설정
public @interface OptimizedCacheable {
    /**
     * 캐시의 종류(이름)를 지정합니다. 캐시 키를 생성하는 데 사용됩니다.
     * @return 캐시 타입 문자열
     */
    String type();

    /**
     * 캐시의 만료 시간(Time-To-Live)을 초 단위로 지정합니다.
     * @return TTL (초)
     */
    long ttlSeconds();
}