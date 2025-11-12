package kuke.board.articleread.cache;

/**
 * 캐시 미스(cache miss) 시 원본 데이터를 조회하는 로직을 캡슐화하기 위한 함수형 인터페이스입니다.
 * java.util.function.Supplier와 유사하지만, 원본 메서드에서 발생할 수 있는 모든 종류의 예외를
 * 처리하기 위해 'throws Throwable'을 명시하고 있습니다.
 *
 * @param <T> 원본 데이터의 타입
 */
@FunctionalInterface
public interface OptimizedCacheOriginDataSupplier<T> {
    /**
     * 원본 데이터를 조회합니다.
     * AOP Aspect에서는 이 메서드 구현으로 'joinPoint.proceed()'를 람다로 전달합니다.
     * @return 조회된 원본 데이터
     * @throws Throwable 원본 메서드 실행 중 발생할 수 있는 모든 예외
     */
    T get() throws Throwable;
}