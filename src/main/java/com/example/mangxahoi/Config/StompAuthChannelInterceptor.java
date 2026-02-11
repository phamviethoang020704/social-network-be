package com.example.mangxahoi.Config;

import com.example.mangxahoi.Security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object tokenObj = accessor.getSessionAttributes().get(JwtHandshakeInterceptor.TOKEN_ATTR);

            if (tokenObj instanceof String token && jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractClaims(token).getSubject();

                accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));
            }
        }

        return message;
    }
}