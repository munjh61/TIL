package kuke.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @OptimizedCacheable 어노테이션을 처리하는 AOP Aspect 클래스입니다.
 * Spring AOP를 사용하여 어노테이션이 붙은 메서드를 가로채고, 캐싱 로직을 적용합니다.
 */
@Aspect // 이 클래스가 Aspect임을 나타냅니다.
@Component
@RequiredArgsConstructor
public class OptimizedCacheAspect {
    private final OptimizedCacheManager optimizedCacheManager;

    /**
     * @OptimizedCacheable 어노테이션이 붙은 모든 메서드 실행 주위에 적용되는 Advice입니다.
     * 이 메서드는 원본 메서드 실행을 제어하며, 캐싱 로직의 진입점 역할을 합니다.
     *
     * @param joinPoint 가로챈 메서드(join point)에 대한 정보를 담고 있는 객체
     * @return 캐시된 데이터 또는 원본 메서드의 실행 결과
     * @throws Throwable 원본 메서드 실행 중 발생할 수 있는 예외
     */
    @Around("@annotation(OptimizedCacheable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 메서드에서 @OptimizedCacheable 어노테이션 정보를 추출합니다.
        OptimizedCacheable cacheable = findAnnotation(joinPoint);

        // 2. 실제 캐싱 처리는 OptimizedCacheManager에 위임합니다.
        return optimizedCacheManager.process(
                cacheable.type(), // 캐시 타입
                cacheable.ttlSeconds(), // 캐시 만료 시간
                joinPoint.getArgs(), // 원본 메서드의 인자 (캐시 키 생성에 사용)
                findReturnType(joinPoint), // 원본 메서드의 반환 타입 (역직렬화에 사용)
                () -> joinPoint.proceed() // 원본 메서드를 실행하는 람다 (캐시 미스 시 호출)
        );
    }

    /**
     * JoinPoint에서 메서드 시그니처를 통해 @OptimizedCacheable 어노테이션을 찾습니다.
     */
    private OptimizedCacheable findAnnotation(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod().getAnnotation(OptimizedCacheable.class);
    }

    /**
     * JoinPoint에서 메서드 시그니처를 통해 반환 타입을 찾습니다.
     */
    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }
}