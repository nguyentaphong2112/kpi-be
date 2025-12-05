package vn.hbtplus.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:4200").withSockJS();
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:4200");
//        registry.addEndpoint("/ws-service/ws")
//                .setAllowedOriginPatterns("http://localhost:4200")
//                .addInterceptors(jwtHandshakeInterceptor)
//                .setHandshakeHandler(new DefaultHandshakeHandler() {
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request,
//                                                      WebSocketHandler wsHandler,
//                                                      Map<String, Object> attributes) {
//                        String username = (String) attributes.get("user");
//                        return (username != null) ? new UsernamePasswordAuthenticationToken(username, null) : null;
//                    }
//                });
//        registry.addEndpoint("/ws-service/ws")
//                .setAllowedOriginPatterns("http://localhost:4200")
//                .addInterceptors(jwtHandshakeInterceptor)
//                .setHandshakeHandler(new DefaultHandshakeHandler() {
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request,
//                                                      WebSocketHandler wsHandler,
//                                                      Map<String, Object> attributes) {
//                        String username = (String) attributes.get("user");
//                        return (username != null) ? new UsernamePasswordAuthenticationToken(username, null) : null;
//                    }
//                }).withSockJS();
    }
}
