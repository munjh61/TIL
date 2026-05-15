package com.example.chatserver.chat.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// /connect로 웹소켓 연결 요청되었을 때 처리할 핸들러
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    // 연결된 세션 관리. thread safe
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // 처음 연결되었을 때
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Connected : "+session.getId());
    }

    // 메세지가 들어왔을 때
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received :" + payload);
        for(WebSocketSession s : sessions){
            if(s.isOpen()){
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    // 연결 종료되었을 때
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
