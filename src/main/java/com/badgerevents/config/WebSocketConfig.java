package com.badgerevents.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
//Spring에서 WebSocket 위의 STOMP message broker 기능을 활성화함.
//WebSocket/STOMP infrastructure 들 준비 - (clientInboundChannel, clientOutboundChannel, brokerChannel
// @MessageMapping handler, Simple Broker를 실행할 기반 등)
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final EventSubscriptionInterceptor
            eventSubscriptionInterceptor;

    private final String frontendOrigin;

    public WebSocketConfig(
            EventSubscriptionInterceptor eventSubscriptionInterceptor,
            @Value("${badgerevents.frontend-origin}")
            String frontendOrigin
    ) {
        this.eventSubscriptionInterceptor =
                eventSubscriptionInterceptor;

        this.frontendOrigin = frontendOrigin;
    }

    // /ws 연결 입구 등록
    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin); // origin 인지 확인하여 허용된것만 제한
    }

    // /app 과 /topic 규칙 설정
    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {
        registry.setApplicationDestinationPrefixes("/app"); // /app으로 시작하는 STOMP SEND는 우리 애플리케이션 코드가 처리, /app 은 application prefix로 인식
        registry.enableSimpleBroker("/topic"); // /topic으로 시작하는 destination은 Simple Broker가 담당. /topic은 prefix 인식.
    }

    // interceptor을 inboundChannel에 설치
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(
                eventSubscriptionInterceptor
        );
    }
}