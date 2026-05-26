package kr.or.ddit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompSubscriptionInterceptor stompSubscriptionInterceptor;

    WebSocketConfig(StompSubscriptionInterceptor stompSubscriptionInterceptor) {
        this.stompSubscriptionInterceptor = stompSubscriptionInterceptor;
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 메시지를 보낼 때 사용할 prefix
        registry.enableSimpleBroker("/topic"); // 메시지를 구독할 때 사용할 prefix

        WebSocketMessageBrokerConfigurer.super.configureMessageBroker(registry);
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS(); // SockJS fallback 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSubscriptionInterceptor);
    }

}
