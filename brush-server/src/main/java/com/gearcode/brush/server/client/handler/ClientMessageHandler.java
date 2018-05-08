package com.gearcode.brush.server.client.handler;

import com.gearcode.brush.server.client.BrushServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author jason
 * @date 2018/5/8
 */
public interface ClientMessageHandler {
    void handle(BrushServer server, ChannelHandlerContext ctx, ByteBuf byteBuf);
}
