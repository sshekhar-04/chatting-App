package com.ssd.testing.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // For development; switch to external broker for prod
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")  // For web clients with SockJS fallback
                .setAllowedOrigins("http://localhost:3000")  // Secure: Replace with your frontend origin
                .withSockJS();
        registry.addEndpoint("/ws-chat")  // For testing tools like Postman
                .setAllowedOriginPatterns("http://localhost:*");  // Secure: Restrict patterns
    }
}