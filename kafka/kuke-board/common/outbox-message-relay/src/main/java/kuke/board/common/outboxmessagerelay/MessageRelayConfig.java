package kuke.board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 메시지 릴레이 기능에 필요한 Spring 설정을 구성하는 클래스입니다.
 * 비동기 처리, 스케줄링, 관련 빈(Bean) 생성을 담당합니다.
 */
@EnableAsync // @Async 어노테이션을 사용한 비동기 메서드 실행을 활성화합니다.
@Configuration
@ComponentScan("kuke.board.common.outboxmessagerelay") // 이 패키지 내의 컴포넌트들을 스캔하여 빈으로 등록합니다.
@EnableScheduling // @Scheduled 어노테이션을 사용한 스케줄링 작업을 활성화합니다.
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 메시지 릴레이용 KafkaTemplate 빈을 생성합니다.
     * @return 구성된 KafkaTemplate 인스턴스
     */
    @Bean
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka 브로커 주소
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 메시지 키 직렬화기
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 메시지 값 직렬화기
        // ACKS_CONFIG를 "all"로 설정하여, 리더와 모든 ISR(In-Sync Replicas)이 메시지를 받았는지 확인합니다.
        // 이는 메시지 유실 가능성을 최소화하는 가장 안전한 설정입니다.
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /**
     * 'AFTER_COMMIT' 단계에서 이벤트를 즉시 발행하는 데 사용될 스레드 풀을 생성합니다.
     * @return ThreadPoolTaskExecutor 인스턴스
     */
    @Bean
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // 기본 스레드 수
        executor.setMaxPoolSize(50); // 최대 스레드 수
        executor.setQueueCapacity(100); // 대기 큐 크기
        executor.setThreadNamePrefix("mr-pub-event-"); // 스레드 이름 접두사
        return executor;
    }

    /**
     * 주기적으로 미처리(pending) 이벤트를 발행하는 스케줄링 작업에 사용될 스레드 풀을 생성합니다.
     * 단일 스레드로 동작하여 순차적인 실행을 보장합니다.
     * @return 단일 스레드 스케줄링 실행자
     */
    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}