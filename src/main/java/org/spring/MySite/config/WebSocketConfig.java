package org.spring.MySite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.security.Principal;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setTimeToFirstMessage(30_000)
                .setSendTimeLimit(15_000)
                .setSendBufferSizeLimit(512 * 1024);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Префикс для отправки сообщений клиентам
        config.enableSimpleBroker("/topic");
        // Префикс для сообщений от клиента к серверу
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint для подключения клиентов
        registry.addEndpoint("/ws/messages")
                .setAllowedOrigins("https://192.168.0.60:8443")
                .withSockJS();
    }


    //настраивает канал входящих сообщений (от клиента к серверу). Команды CONNECT,SUBSCRIBE,SEND,DISCONNECT
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Principal user = accessor.getUser();
                    if (user == null) {
                        throw new SecurityException("Требуется аутентификация");
                    }
                }
                return message;
            }
        });
    }
}