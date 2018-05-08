package com.gearcode.brush.server.websocket;

import com.gearcode.brush.server.client.bean.BrushClient;
import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.util.Constants;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jason on 2018/4/16.
 */
public class ConsoleWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleWebSocketHandler.class);

    BrushServer brushServer;
    AtomicInteger counter = new AtomicInteger();

    ConcurrentMap<String, BrushClient> consoleClientMap = new ConcurrentHashMap<>();

    public ConsoleWebSocketHandler(BrushServer brushServer) {
        this.brushServer = brushServer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established from: {}", NetUtil.toAddressString(session.getRemoteAddress().getAddress()));
        logger.info("Connection counter: {}", counter.incrementAndGet());

        // Console 连接建立, 与Client配对
        String id = session.getAttributes().get(Constants.WS_HS_KEY_CLIENT).toString();
        Integer width = (Integer) session.getAttributes().get(Constants.WS_HS_KEY_WIDTH);
        Integer height = (Integer) session.getAttributes().get(Constants.WS_HS_KEY_HEIGHT);
        BrushClient client = brushServer.findClient(id);
        client.setConsole(new BrushConsole(session, width, height));
        consoleClientMap.put(session.getId(), client);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed from: {}", NetUtil.toAddressString(session.getRemoteAddress().getAddress()));
        logger.info("Connection counter: {}", counter.decrementAndGet());

        // 断开连接, 删除Client与Console配对关系
        consoleClientMap.remove(session.getAttributes().get(Constants.WS_HS_KEY_CLIENT).toString());
        BrushClient client = brushServer.findClient(session.getAttributes().get(Constants.WS_HS_KEY_CLIENT).toString());
        if(null != client) {
            client.setConsole(null);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String text = new String(message.asBytes(), CharsetUtil.UTF_8);
            logger.info("WebSocket handle text message: {}", text);

            // 获取对应的client, 直接转发消息
            BrushClient brushClient = consoleClientMap.get(session.getId());

            // 检测client是否存在
            if (null == brushClient) {
                logger.warn("BrushClient not exist, close session!");
                session.close(CloseStatus.GOING_AWAY);
                return;
            }

            SocketChannel clientChannel = brushClient.getSocketChannel();
            //TODO ByteBufAllocator
            clientChannel.writeAndFlush(text).sync();
            logger.info("Send message from console[{}] to client[{}], message: {}",
                    NetUtil.toAddressString(session.getRemoteAddress().getAddress()),
                    NetUtil.toAddressString(clientChannel.remoteAddress().getAddress()),
                    new String(message.asBytes(), CharsetUtil.UTF_8));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }
}
