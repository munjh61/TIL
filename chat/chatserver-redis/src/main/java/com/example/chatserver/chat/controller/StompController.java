package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.service.ChatService;
import com.example.chatserver.chat.service.RedisPubSubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import tools.jackson.databind.ObjectMapper;

@Controller
public class StompController {

//    // 방법1 MessageMapping(수신)과 SendTo(topic에 메시지 전달) 한꺼번에 처리)
//    @MessageMapping("/{roomId}") // 클라이언트에서 publish/roomId 형태로 메시지를 발행시 MessageMapping 수신
//    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독 중인 클라이언트에게 전송
//    // DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller와 짝꿍
//    public String sendMessage(@DestinationVariable Long roomId, String message){
//        System.out.println(message);
//        return message;
//    }

    // 방법 2 MessageMapping 어노테이션만 사용

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService pubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
        this.pubSubService = pubSubService;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
        System.out.println(chatMessageDto.getMessage());
        chatService.saveMessage(roomId, chatMessageDto);

        // [단일 서버]
        // messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto); //@sendTo 어노테이션 역할과 같음

        // [멀티 서버]
        // 멀티 서버 환경에서는 현재 서버에만 WebSocket 메시지를 보내면
        // 다른 서버에 연결된 사용자는 메시지를 받지 못할 수 있다.
        // 그래서 Redis Pub/Sub의 "chat" 채널에 메시지를 발행하고,
        // 각 서버가 해당 채널을 구독하여 자기 서버의 WebSocket 구독자에게 다시 전달한다.

        chatMessageDto.setRoomId(roomId);
        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(chatMessageDto);
        pubSubService.publish("chat", message);
    }
}
