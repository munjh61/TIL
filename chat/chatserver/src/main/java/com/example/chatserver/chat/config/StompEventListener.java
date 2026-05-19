package com.example.chatserver.chat.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 로깅, 디버깅 목적
// 스프링과 stomp는 기본적으로 세션관리를 자동으로 처리한다
// 연결, 해제 이벤트를 기록하고 연결된 세션 수 확인 목적
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    // SessionConnectedEvent 가 발생했을 때
    @EventListener
    public void connectHandler(SessionConnectedEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        System.out.println("connect session ID"+accessor.getSessionId());
        System.out.println("total session : "+sessions.size());
    }

    @EventListener
    public void disconnectHandler(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());
        System.out.println("disconnect session ID"+accessor.getSessionId());
        System.out.println("total session : "+sessions.size());
    }
}
