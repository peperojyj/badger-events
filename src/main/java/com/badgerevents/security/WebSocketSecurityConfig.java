package com.badgerevents.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

//WebSocket 전체에 적용되는 공통 보안 규칙
//로그인 사용자인가?, 허용된 SEND 주소인가?, 허용된 SUBSCRIBE 주소인가? 그 밖의 메시지는 거절해야 하는가? 등
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>>
    messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder
                    messages
    ) {
        messages
                .nullDestMatcher()
                .authenticated()

                .simpDestMatchers(
                        "/app/events/*/messages"
                )
                .authenticated()

                .simpSubscribeDestMatchers(
                        "/topic/events/*"
                )
                .authenticated()

                .simpTypeMatchers(MESSAGE, SUBSCRIBE)
                .denyAll()

                .anyMessage()
                .denyAll();

        return messages.build();
    }
}