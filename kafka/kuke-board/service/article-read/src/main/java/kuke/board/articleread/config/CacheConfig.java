package kuke.board.articleread.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Spring의 표준 캐싱 기능(@Cacheable 등)을 위한 설정을 구성하는 클래스입니다.
 */
@Configuration
@EnableCaching // Spring의 캐싱 기능을 활성화합니다.
public class CacheConfig {

    /**
     * Redis를 캐시 저장소로 사용하는 CacheManager 빈을 생성합니다.
     *
     * 참고: 현재 이 프로젝트의 캐싱은 @OptimizedCacheable이라는 커스텀 어노테이션과 AOP를 통해
     * 직접 구현되어 있으며, 해당 로직은 이 RedisCacheManager를 사용하지 않습니다.
     * 이 설정은 표준 @Cacheable 어노테이션을 사용할 경우를 대비한 구성으로 보입니다.
     *
     * @param connectionFactory Spring Boot가 자동 설정한 Redis 연결 팩토리
     * @return 설정이 적용된 RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                // 특정 캐시("articleViewCount")에 대한 초기 설정을 정의합니다.
                .withInitialCacheConfigurations(
                        Map.of(
                                "articleViewCount", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1))
                        )
                )
                .build();
    }
}