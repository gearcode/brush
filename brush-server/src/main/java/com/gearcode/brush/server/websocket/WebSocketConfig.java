package com.gearcode.brush.server.websocket;

import com.alibaba.fastjson.JSON;
import com.gearcode.brush.server.BrushClient;
import com.gearcode.brush.server.boot.NettyBrushServerSpringHolder;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by liteng3 on 2018/4/16.
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
