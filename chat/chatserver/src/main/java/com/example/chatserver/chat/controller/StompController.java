package com.example.chatserver.chat.controller;

import com.example.chatserver.chat.dto.ChatMessageReqDto;
import com.example.chatserver.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

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

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService) {
        this.messageTemplate = messageTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageReqDto chatMessageReqDto){
        System.out.println(chatMessageReqDto.getMessage());
        chatService.saveMessage(roomId, chatMessageReqDto);
        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageReqDto); //@sendTo 어노테이션 역할과 같음
    }
}
