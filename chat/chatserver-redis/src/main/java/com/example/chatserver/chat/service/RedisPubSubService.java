package com.example.chatserver.chat.service;

import com.example.chatserver.chat.dto.ChatMessageDto;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class RedisPubSubService implements MessageListener {

    // pattern을 String으로 바꾸면 topic이다.
    // 이를 기반으로 topic 마다 다르게 처리한다.

    // [예시]
    // if(topicPattern.equals("chat.*")) {
    //        // 채팅 관련 처리
    //    }
    //
    //    if(topicPattern.equals("alarm.*")) {
    //        // 알림 관련 처리
    //    }

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    public RedisPubSubService(@Qualifier("chatPubSub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String payload = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
        messageTemplate.convertAndSend("/topic/"+chatMessageDto.getRoomId(), chatMessageDto);
    }

    public void publish(String channel, String message){
        // 특정 채널에 메시지를 발송하겠다.
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
