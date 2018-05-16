package com.gearcode.brush.server.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 *
 * @author jason
 * @date 2018/5/15
 */
public class NetUtils {

    public static String toAddressString(ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        return io.netty.util.NetUtil.toAddressString(channel.remoteAddress().getAddress());
    }
}
