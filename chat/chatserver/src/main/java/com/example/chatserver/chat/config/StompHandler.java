package com.example.chatserver.chat.config;

import com.example.chatserver.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class StompHandler implements ChannelInterceptor {

    private final SecretKey SECRET_KEY;
    private final ChatService chatService;

    public StompHandler(@Value("${jwt.secretKey}") String secretKey, ChatService chatService) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        this.chatService = chatService;
    }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(StompCommand.CONNECT == accessor.getCommand()){
            System.out.println("connect 요청시 토큰 유효성 검증");
            parseToken(accessor);
            System.out.println("토큰 검증 완료");
        }

        // 채팅방에 권한 없는 유저의 토큰일 경우, subscribe 못하도록
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            System.out.println("subscribe 요청시 토큰 유효성 검증");
            Claims claims = parseToken(accessor);
            System.out.println("토큰 검증 완료");

            String email = claims.getSubject();
            // getDestination() 는 connect, disconnect 에서 null 이다, 항상 사용 가능한 것은 아니다.
            // ["", "topic", "roomId"]
            String roomId = accessor.getDestination().split("/")[2];

            if(!chatService.isRoomParticipant(email, Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 room 에 권한이 없습니다");
            }
        }
        return message;
    }

    private Claims parseToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new AuthenticationServiceException("Authorization header가 올바르지 않습니다.");
        }

        String jwtToken = bearerToken.substring(7);

        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }
}
