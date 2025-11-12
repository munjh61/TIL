package kuke.board.hotarticle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Kafka 컨슈머 관련 설정을 구성하는 클래스입니다.
 */
@Configuration
public class KafkaConfig {
    /**
     * Kafka 리스너 컨테이너 팩토리를 커스터마이징하여 빈으로 등록합니다.
     * @param consumerFactory Spring Boot가 자동 설정한 기본 ConsumerFactory
     * @return 커스터마이징된 ConcurrentKafkaListenerContainerFactory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // AckMode를 MANUAL로 설정합니다.
        // 이를 통해 메시지 처리가 완료된 후 개발자가 코드 내에서 명시적으로 Acknowledgment.acknowledge()를 호출해야 합니다.
        // 이는 메시지 처리 시점을 정확하게 제어하고, 처리 도중 예외 발생 시 메시지를 재처리하도록 보장합니다.
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}