package com.gearcode.brush.server.client.handler;

import com.gearcode.brush.server.client.BrushServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public class HeartbeatHandler implements ClientMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    @Override
    public void handle(BrushServer server, ChannelHandlerContext ctx, ByteBuf byteBuf) {
        logger.info("HEARTBEAT from Client");
    }
}
