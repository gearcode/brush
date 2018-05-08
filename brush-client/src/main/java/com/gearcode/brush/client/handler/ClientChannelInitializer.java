package com.gearcode.brush.client.handler;

import com.gearcode.brush.client.codec.LengthFieldBasedFrameEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author jason
 * @date 2018/5/7
 */
@ChannelHandler.Sharable
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(ClientChannelInitializer.class);

    /**
     * 数据帧最大8M
     */
    final static int PAYLOAD = 8 * 1024 * 1024;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
        // Idle 60s trigger IdleStateEvent
        .addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
        // Decoder
        .addLast(new LengthFieldBasedFrameDecoder(PAYLOAD, 2, 4, 0, 6))
        .addLast(new StringDecoder(CharsetUtil.UTF_8))
        // Encoder
        .addLast(new LengthFieldBasedFrameEncoder())
        // Logic
        .addLast(new ClientLogicHandler());
    }
}
