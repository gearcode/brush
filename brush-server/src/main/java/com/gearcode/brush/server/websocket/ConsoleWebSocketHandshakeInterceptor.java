package com.gearcode.brush.server.websocket;

import com.gearcode.brush.server.BrushClient;
import com.gearcode.brush.server.BrushServer;
import com.gearcode.brush.server.Constants;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by liteng3 on 2018/4/17.
 */
public class ConsoleWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleWebSocketHandshakeInterceptor.class);

    BrushServer brushServer;

    public ConsoleWebSocketHandshakeInterceptor(BrushServer brushServer) {
        this.brushServer = brushServer;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        logger.info("BeforeHandshake URI: {}", uri);

        String client;
        String token;
        Integer width;
        Integer height;
        try {
            // 解析URI中的参数: client, token, width, height
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> parameters = queryStringDecoder.parameters();
            client = parameters.get(Constants.WS_HS_KEY_CLIENT).get(0);
            token = parameters.get(Constants.WS_HS_KEY_TOKEN).get(0);
            width = Integer.parseInt(parameters.get(Constants.WS_HS_KEY_WIDTH).get(0));
            height = Integer.parseInt(parameters.get(Constants.WS_HS_KEY_HEIGHT).get(0));
        } catch (Exception e) {
            logger.error("Decode handshake URI error, URI: {}", uri);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        // 获取对应的Client
        BrushClient brushClient = brushServer.findClient(client);
        if(brushClient == null) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        }

        // 校验密码
        if(!brushClient.getPass().equals(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(Constants.WS_HS_KEY_CLIENT, client);
        attributes.put(Constants.WS_HS_KEY_TOKEN, token);
        attributes.put(Constants.WS_HS_KEY_WIDTH, width);
        attributes.put(Constants.WS_HS_KEY_HEIGHT, height);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        logger.info("WebSocket handshake success!");
    }
}
