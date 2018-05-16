package com.gearcode.brush.server.client.codec;

import com.gearcode.brush.server.client.BrushServer;
import com.gearcode.brush.server.client.bean.BrushClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * 初始化ServerSocket
 * @author jason
 * @date 2018/4/10
 */
@ChannelHandler.Sharable
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(ServerChannelInitializer.class);

    BrushServer server;

    /**
     * 数据帧最大8M
     */
    final static int PAYLOAD = 8 * 1024 * 1024;

    public ServerChannelInitializer(BrushServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.info("Connected from {}", NetUtil.toAddressString(ch.remoteAddress().getAddress()));

        // 初始化ServerSocket的Pipeline
        ch.pipeline()
        // Idle 60s trigger IdleStateEvent
        .addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS))
        // Encoder
        .addLast(new LengthFieldBasedFrameEncoder())
        .addLast(new StringEncoder(CharsetUtil.UTF_8))
        // Decoder
        .addLast(new LengthFieldBasedFrameDecoder(PAYLOAD, 2, 4, 0, 6))
        // Logic
        .addLast(new ServerLogicHandler(server));


    }
}
