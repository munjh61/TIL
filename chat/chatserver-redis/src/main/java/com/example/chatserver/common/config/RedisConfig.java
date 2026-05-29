package com.example.chatserver.common.config;

import com.example.chatserver.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    // 연결 기본 객체
    @Bean
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // 0 ~ 9 connection 객체를 목적에 따라 숫자로 구분.
        // 하지만 redis pub/sub은 특정 데이터베이스에 의존적이지 않아 의미 없음
        // configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    // publish 객체
    @Bean
    @Qualifier("chatPubSub")
    // 일반적으로는 RedisTemplate<key, value>를 사용하지만 메세지 특성상 String을 사용했다.
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory // 연결 객체
    ){
        return new StringRedisTemplate(redisConnectionFactory);
    }

    // subscribe 객체
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory, // 연결 객체
            MessageListenerAdapter messageListenerAdapter // 처리 방식 지정
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat")); // 특정 토픽에 관하여 어떤 ListenAdapter로 넘기겠다
        return container;
    }

    // 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService){ // RedisPubSubService 객체는 직접 만든 객체임
        // redisPubSubService의 특정 메시지가 수신된 메시지를 처리할 수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage"); // 클래스명, 메서드명
    }

}
