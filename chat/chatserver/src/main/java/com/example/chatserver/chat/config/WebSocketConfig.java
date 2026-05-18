//package com.example.chatserver.chat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final SimpleWebSocketHandler simpleWebSocketHandler;
//
//    public WebSocketConfig(SimpleWebSocketHandler simpleWebSocketHandler) {
//        this.simpleWebSocketHandler = simpleWebSocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        // /connect url로 websocket 연결이 들어오면 핸들러 클래스가 처리
//        registry.addHandler(simpleWebSocketHandler,"/connect")
//                // http가 아니기 때문에 웹 소켓 프로토콜용 cors 설정이 필요하다
//                .setAllowedOrigins("http://localhost:3000");
//    }
//}
