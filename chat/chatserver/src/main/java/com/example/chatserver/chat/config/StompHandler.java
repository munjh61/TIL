package com.example.chatserver.chat.config;

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
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class StompHandler implements ChannelInterceptor {

    private final SecretKey SECRET_KEY;

    public StompHandler(@Value("${jwt.secretKey}") String secretKey) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if(StompCommand.CONNECT == accessor.getCommand()){
            System.out.println("connect 요청시 토큰 유효성 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String jwtToken = bearerToken.substring(7);
            // 토큰 검증 및 Payload 추출
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            System.out.println("토큰 검증 완료");
        }

        return message;
    }
}
