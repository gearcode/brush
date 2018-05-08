package com.gearcode.brush.server.websocket;

import com.gearcode.brush.server.boot.NettyBrushServerSpringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by jason on 2018/4/16.
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    @Qualifier("brushServerHolder")
    NettyBrushServerSpringHolder brushServer;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ConsoleWebSocketHandler(brushServer.getServer()), "/ws")
                .setAllowedOrigins("*").addInterceptors(new ConsoleWebSocketHandshakeInterceptor(brushServer.getServer()));
        logger.info("Regist WebSocket handler: {}", "ConsoleWebSocketHandler");
    }
}
