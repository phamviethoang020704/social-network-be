package com.example.mangxahoi.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor JwtHandshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfig(
            JwtHandshakeInterceptor jwtHandshakeInterceptor,
            StompAuthChannelInterceptor stompAuthChannelInterceptor
    ) {
        this.JwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client subscribe: /user/queue/... cho 1-1
        config.enableSimpleBroker("/queue", "/topic");

        // Prefix user-specific destination
        config.setUserDestinationPrefix("/user");

        // Client send lên server: /app/...
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(JwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}