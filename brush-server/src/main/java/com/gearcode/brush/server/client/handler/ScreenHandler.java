package com.gearcode.brush.server.client.handler;

import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.client.bean.BrushClient;
import com.gearcode.brush.server.websocket.BrushConsole;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;

import java.io.IOException;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class ScreenHandler implements ClientMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScreenHandler.class);

    @Override
    public void handle(BrushServer server, ChannelHandlerContext ctx, ByteBuf byteBuf) {
        // 读取bytes
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        // 收到client的数据, 直接发送给console
        SocketChannel channel = (SocketChannel) ctx.channel();
        String shortId = channel.id().asShortText();
        BrushClient brushClient = server.findClient(shortId);
        if (brushClient == null) {
            logger.error("Can not find Client: {}", shortId);
        }

        BrushConsole console = brushClient.getConsole();
        if (console != null) {
            try {
                console.getSession().sendMessage(new BinaryMessage(bytes));
                logger.info("Send message from client[{}] to console[{}], message: IMG[{}]",
                        NetUtil.toAddressString(console.getSession().getRemoteAddress().getAddress()),
                        NetUtil.toAddressString(channel.remoteAddress().getAddress()),
                        bytes.length);
            } catch (IOException e) {
                logger.error("Send message to console error!", e);
                //TODO Remove Console
            }
        }
    }
}
